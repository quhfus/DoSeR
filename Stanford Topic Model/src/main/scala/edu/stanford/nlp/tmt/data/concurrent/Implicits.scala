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

object Implicits {
  implicit def richArray[A](items : Array[A])
  (implicit pool : Executor = Concurrent.pool) =
    new RichSeq[A](items)(pool);

  implicit def richSeq[A](items : scala.collection.Seq[A])
  (implicit pool : Executor = Concurrent.pool) =
    new RichSeq[A](items)(pool);

  implicit def richIterator[A](items : Iterator[A])
  (implicit pool : Executor = Concurrent.pool) =
    new RichIterator[A](items)(pool);
}

import Implicits._;

class RichIterator[A](val iterator : Iterator[A])(implicit pool : Executor) {
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
  def pmap[B](fn : (A=>B), queueSize : Int = concurrent.cores * 4) : Iterator[B] = 
    Concurrent.map(iterator, fn, queueSize)(pool);
}

class RichSeq[C](val seq : scala.collection.Seq[C])(implicit pool : Executor) {
  /** Parallel reduce on a set of values. */
  def preduce(reduce : (C,C) => C) : C =
    Concurrent.reduce(seq, reduce)(pool);
}

