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
 * Asynchronous math.
 *
 * @author dramage
 */
object AsyncMath {
  /** Separate thread pool for mathematical operations. */
  lazy val pool = ThreadPool(concurrent.cores);

  private final def addIntoRange(a : Array[Double], b : Array[Double], offset : Int, length : Int) {
    val end = (offset + length) min a.length;
    var i = offset;
    while (i < end) {
      a(i) += b(i);
      i += 1;
    }
  }

  /** a += b in chunks of 10000 */
  def addInto(a : Array[Double], b : Array[Double], pool : Executor = pool)
  : () => Array[Double] = {
    require(a.length == b.length);
    val batch = 10000;
    val jobs = (a.length + batch - 1) / batch;  // round up
    
    if (jobs == 1) {
      () => { addIntoRange(a, b, 0, a.length); a; }
    } else {
      val latch = new java.util.concurrent.CountDownLatch(jobs);
      for (job <- 0 until jobs) {
        pool.execute(new Runnable() {
          override def run = {
            addIntoRange(a, b, job * batch, batch);
            latch.countDown;
          }
        });
      }
      () => { latch.await(); a; }
    }
  }
}

