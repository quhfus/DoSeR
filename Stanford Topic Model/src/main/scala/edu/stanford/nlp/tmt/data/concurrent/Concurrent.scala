/*
 * Distributed as part of the Stanford Topic Modeling Toolbox.
 * Copyright (c) 2009- The Board of Trustees of the Leland
 * Stanford Junior University.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA.
 */
package edu.stanford.nlp.tmt;
package data;
package concurrent;

import scala.concurrent.FutureTaskRunner;
import scala.concurrent.ops.future;
import scalala.operators.{BinaryUpdateOp,OpAdd};

import java.util.concurrent.{ArrayBlockingQueue,CountDownLatch,Semaphore};
import java.util.concurrent.{ThreadFactory,Executor,Executors};
import java.util.concurrent.atomic.{AtomicLong,AtomicBoolean};

/**
 * Concurrent operations.
 * 
 * @author dramage
 */
object Concurrent {
   /** Shared thread pool for all concurrent ops. */
  lazy val pool = ThreadPool(cores);

  /** Runs the given function in a daemon thread. */
  def daemon(fn : =>Unit) = {
    val thread = DaemonThreadFactory.newThread(new Runnable {
      def run = fn;
    });
    thread.start;
    thread;
  }
  
  /**
   * Maps the values of the given iterator in parallel using workers in
   * the given thread pool.  These workers are started in the order of
   * the original iterator, but might not finish in that order.  To keep
   * the other cores busy, up to queueSize values can processed at at time,
   * with results being queued up and returned in the original iteration order.
   * By default, queueSize is four times the number of cores on the machine to
   * keep things humming, but this number can be raised or lowered to change
   * the relative preference of memory versus throughput.
   */
  def map[A,B](iterator : Iterator[A], fn : (A=>B),
    queueSize : Int = concurrent.cores * 4)(implicit pool : Executor = pool)
  : Iterator[B] = {
    require(queueSize > 0, "queueSize must be positive");
 
    // special case empty iterator
    if (!iterator.hasNext) {
      return Iterator[B]();
    }
    
    /** make sure we don't read beyond the queue size limit. */
    val semaphore = new Semaphore(queueSize);
   
    /** signal for when enqueued something and lock for iterHasNext and readN */
    val lock = new Object;
    
     /** are we done reading */
    @volatile var iterHasNext = iterator.hasNext;
    
    /** how many values have we read overall */
    @volatile var readN = 0l;
    
    /** the queue for processed values */
    val doneQ = new java.util.concurrent.ConcurrentSkipListMap[Long,Any];
    
    // thread for consuming from incoming iterator.  synchronize on semaphore
    // while creating daemon thread to ensure semaphore fully init'd (unnecessary?)
    semaphore.synchronized { daemon {
      System.err.println("[Concurrent] " + semaphore.availablePermits + " permits");
      while (iterHasNext) {
        // wait for the read queue to not be full
        semaphore.acquire;

        // read the next item
        val itemN : Long = readN;
        val itemV : A = iterator.next;
        
        lock.synchronized {
          readN += 1;
          iterHasNext = iterator.hasNext;
        }
        
        pool.execute(new Runnable {
          def run = {
            try {
              doneQ.put(itemN, fn(itemV));
            } catch {
              case ex : Throwable =>
                doneQ.put(itemN, new ConcurrentException(ex));
            }
            
            // notify iterator thread next return value might be ready
            lock.synchronized { lock.notifyAll; }
          }
        });
      }
      System.err.println("[Concurrent] done reading " + readN + " items");
    } }
    
    new Iterator[B] {
      var awaiting = 0l;
  
      override def hasNext =
        lock.synchronized { iterHasNext || awaiting < readN };
      
      override def next = {
        require(hasNext, "Next called on empty iterator");
        
        // wait for our value to appear at the front of the map
        lock.synchronized {
          while (doneQ.isEmpty || awaiting != doneQ.firstKey) {
            lock.wait;
          }
        }

        // get processed value        
        val rv = doneQ.remove(awaiting);
        awaiting += 1;

        // let the next read happen, if any
        semaphore.release;
        
        if (rv.isInstanceOf[ConcurrentException])
          // re-create wrapper exception to capture the caller's stack trace
          throw new ConcurrentException(rv.asInstanceOf[ConcurrentException].getCause);
        else
          rv.asInstanceOf[B];
      }
    }
  }
  
  /** Parallel reduce on a set of values. */
  def reduce[C](seq : scala.collection.Seq[C], reduce : (C,C) => C)
  (implicit pool : Executor = pool) : C = {
    trait Node {
      /** The value of this node.  May not be ready initially. */
      def value : C;
    }

    case class InnerNode(left : Node, right : Node) extends Node {
      @volatile var _value : Option[C] = None;
      @volatile var _error : Option[Throwable] = None;
      
      private val latch = new CountDownLatch(1);

      // Both children's computation thread will already be in the queue
      // by the order of construction.  So we can just call .value and
      // wait for it to return
      pool.execute(new Runnable {
        def run = {
          try {
            _value = Some(reduce(left.value, right.value));
          } catch {
            case ex : Throwable =>
              _error = Some(ex);
          }
          assert(_value.isDefined ^ _error.isDefined)
          latch.countDown;
        }
      });
    
      def value : C = {
        latch.await;
        if (_error.isDefined) {
          throw new ConcurrentException(_error.get);
        } else {
          _value.get;
        }
      }
    }
    
    case class LeafNode(override val value : C) extends Node;
    
    def build(values : Seq[C]) : Node = {
      require(values.size > 0, "Cannot reduce an empty list");
      values.size match {
        case 1 => LeafNode(values(0));
        case 2 => InnerNode(LeafNode(values(0)),LeafNode(values(1)));
        case _ =>
          InnerNode(build(values.slice(0, values.length / 2)),
                    build(values.slice(1 + (values.length / 2), values.size)));
      }
    }
    
    build(seq).value;
  }
  
  def mapreduce[T,V:ClassManifest](items : TraversableOnce[T],
    map : (T=>V), reduce : ((V,V) => V), numCores : Int = concurrent.cores)
  : V = {
    val partials = new Array[V](numCores);

    val threads = new scala.collection.mutable.ArrayBuffer[Thread] with
      scala.collection.mutable.SynchronizedBuffer[Thread];

    val exceptions = new scala.collection.mutable.ArrayBuffer[Throwable] with
      scala.collection.mutable.SynchronizedBuffer[Throwable];

    val queue = new java.util.concurrent.ArrayBlockingQueue[T](numCores * 2);

    val atomic = new java.util.concurrent.atomic.AtomicInteger(0);

    // spawn processor threads
    (0 until numCores).foreach(core => future {
      threads += Thread.currentThread;
      try {
        partials(core) = map(queue.take());
        atomic.decrementAndGet;

        while (true) {
          val item = queue.take();
          partials(core) = reduce(partials(core),map(item));
          atomic.decrementAndGet;
        }
      } catch {
        case ex : Throwable =>
          exceptions += ex;
      }
    });

    // supply values
    for (item <- items) {
      if (!exceptions.isEmpty) {
        threads.foreach(_.interrupt());
        throw exceptions.head;
      }

      atomic.incrementAndGet;
      queue.put(item);
    }

    while (atomic.get > 0) {
      Thread.`yield`;
    }
    threads.foreach(_.interrupt());

    Concurrent.reduce(partials, reduce);
  }
}

