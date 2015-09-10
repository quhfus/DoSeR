/*
 *  Copyright (C) 2010 dramage
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package edu.stanford.nlp.tmt;
package data;

import scala.concurrent.ops.future;

object Pivot {
  def pivot[T,K,V](
    items : TraversableOnce[T], groupBy : (T=>K), map : (T=>V), reduce : ((V,V) => V),
    numCores : Int = concurrent.cores)
  : scala.collection.Map[K,V] = {
    val maps = Array.fill(numCores)(scala.collection.mutable.Map[K,V]());

    val threads = new scala.collection.mutable.ArrayBuffer[Thread] with
      scala.collection.mutable.SynchronizedBuffer[Thread];

    val exceptions = new scala.collection.mutable.ArrayBuffer[Throwable] with
      scala.collection.mutable.SynchronizedBuffer[Throwable];

    val queue = new java.util.concurrent.ArrayBlockingQueue[T](numCores * 2);

    val atomic = new java.util.concurrent.atomic.AtomicInteger(0);

    // spawn processor threads
    (0 until numCores).foreach(core => future {
      threads += Thread.currentThread;
      val rv = maps(core);
      try {
        while (true) {
          val item = queue.take();
          val key = groupBy(item);
          if (rv.contains(key)) {
            rv(key) = reduce(rv(key),map(item));
          } else {
            rv(key) = map(item);
          }
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

    val rv = maps(0);
    var i = 1;
    while (i < maps.length) {
      for ((k,v) <- maps(i)) {
        if (rv.contains(k)) {
          rv(k) = reduce(rv(k),v);
        } else {
          rv(k) = v;
        }
      }
      i += 1;
    }

    rv;
  }

  def reduce[T,K,V](
    items : TraversableOnce[T], groupBy : (T=>K), initial : (()=>V),
    reduceLeft : ((V,T) => V),
    reduce : ((V,V) => V),
    numCores : Int = concurrent.cores)
  : scala.collection.Map[K,V] = {
    val maps = Array.fill(numCores)(scala.collection.mutable.Map[K,V]());

    val threads = new scala.collection.mutable.ArrayBuffer[Thread] with
      scala.collection.mutable.SynchronizedBuffer[Thread];

    val exceptions = new scala.collection.mutable.ArrayBuffer[Throwable] with
      scala.collection.mutable.SynchronizedBuffer[Throwable];

    val queue = new java.util.concurrent.ArrayBlockingQueue[T](numCores * 2);

    val atomic = new java.util.concurrent.atomic.AtomicInteger(0);

    // spawn processor threads
    (0 until numCores).foreach(core => future {
      threads += Thread.currentThread;
      val rv = maps(core);
      try {
        while (true) {
          val item = queue.take();
          val key = groupBy(item);
          rv(key) = reduceLeft(rv.getOrElseUpdate(key, initial()), item);
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

    val rv = maps(0);
    var i = 1;
    while (i < maps.length) {
      for ((k,v) <- maps(i)) {
        if (rv.contains(k)) {
          rv(k) = reduce(rv(k),v);
        } else {
          rv(k) = v;
        }
      }
      i += 1;
    }

    rv;
  }
}
