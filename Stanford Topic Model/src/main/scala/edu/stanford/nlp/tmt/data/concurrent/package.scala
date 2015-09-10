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

import java.util.concurrent.{ThreadFactory,Executor,Executors};

package object concurrent {
  /**
   * Default number of cores to be used by code in this package as specified
   * in the system property concurrent.cores or as the Runtime.availableProcessors.
   */
  val cores : Int = {
    val prop = System.getProperty("concurrent.cores");
    if (prop != null) prop.toInt else Runtime.getRuntime.availableProcessors;
  }
}

package concurrent {

/**
 * A ThreadFactory for creating daemon threads.
 * 
 * @author dramage
 */
object DaemonThreadFactory extends ThreadFactory {
  override def newThread(r : Runnable) = {
    val thread = new Thread(r);
    thread.setDaemon(true);
    thread;
  }
}

/**
 * Constructor for a thread pool of the requested size.  If 0,
 * returns a new cached thread pool.  If positive, returns a 
 * fixed thread pool of that size.  Both use dameon threads only.
 */
object ThreadPool {
  def apply(cores : Int) : Executor = {
    if (cores > 0) {
      Executors.newFixedThreadPool(cores, DaemonThreadFactory)
    } else {
      Executors.newCachedThreadPool(DaemonThreadFactory)
    }
  }
}

/**
 * Exception thrown when a parallel computation dies.
 *
 * @author dramage
 */
class ConcurrentException(cause : Throwable)
extends RuntimeException(cause);
}

