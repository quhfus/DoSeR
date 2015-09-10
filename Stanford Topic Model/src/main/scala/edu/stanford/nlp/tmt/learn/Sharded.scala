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
package learn;

import java.util.concurrent.CountDownLatch;

import scala.collection.IterableLike;
import scala.collection.TraversableView.NoBuilder;
import scala.collection.generic.CanBuildFrom;
import scala.concurrent.FutureTaskRunner;
import scala.concurrent.ops._;

import scalanlp.collection.LazyIterable;


/**
 * A unified collection-like view of a set of shards, each of which is
 * IterableLike.  Calls to map, filter, reduce, foreach, etc., are evaluated
 * with the help of a FutureTaskRunner.  Note: callers should use the
 * provided <code>reduce</code> method for a parallel reduction instead of
 * <code>reduceLeft</code> and <code>reduceRight</code>.
 *
 * @author dramage
 */
class Sharded[A](val shards : List[List[A]])(implicit val ft : FutureTaskRunner)
extends Iterable[A] with IterableLike[A,Sharded[A]] {

  type Repr = Sharded[A];

  override def newBuilder =
    new NoBuilder[A];

  def iterator : Iterator[A] = new Iterator[A] {
    val iterators = shards.map(_.iterator).toArray;
    var which = 0;

    override def hasNext =
      iterators(which).hasNext;

    override def next = {
      val rv = iterators(which).next;
      which = (which + 1) % iterators.length;
      rv;
    }
  }

  /** Applies the given function to each shard. */
  def foreachShard[B](f : (List[A],Int) => B) : Unit = {
    val latch = new CountDownLatch(shards.length);
    for ((shard,index) <- shards.zipWithIndex) {
      future({f(shard,index); latch.countDown;})(ft)
    }
    latch.await;
  }

  override def map[B,That](f : A=>B)(implicit bf : CanBuildFrom[Repr, B, That]) : That =
    new Sharded[B](shards.map(s => future(s.map(f))(ft)).map(_.apply)).asInstanceOf[That];

  override def filter(f : A=>Boolean) : Repr =
    new Sharded[A](shards.map(s => future(s.filter(f))(ft)).map(_.apply)).asInstanceOf[Repr];

  override def foreach[B](f: A => B) : Unit =
    foreachShard((s,i) => s.foreach(f));

  override def reduce[A1 >: A](op: (A1, A1) => A1) : A1 =
    shards.filter(!_.isEmpty).map(s => future(s.reduceLeft(op))(ft)).map(_.apply).reduceLeft(op);

  override def reduceLeft[B >: A] (op: (B, A) ⇒ B) : B =
    throw new ShardedException("reduceLeft called on Sharded.  Use reduce instead.");

  override def reduceRight[B >: A] (op: (A, B) ⇒ B) : B =
    throw new ShardedException("reduceRight called on Sharded.  Use reduce instead.");

  override def max [B >: A] (implicit cmp: Ordering[B]) : A =
    shards.map(_.max(cmp)).max(cmp);

  override def min [B >: A] (implicit cmp: Ordering[B]) : A =
    shards.map(_.min(cmp)).min(cmp);

  override def sum [B >: A] (implicit num: Numeric[B]) : B =
    shards.map(_.sum(num)).sum(num);

  override def product [B >: A] (implicit num: Numeric[B]) : B =
    shards.map(_.product(num)).product(num);

  override def size =
    shards.map(_.size).sum;

  override def toList =
    iterator.toList;
}

object Sharded {
  def apply[A](coll : Iterable[A], numShards : Int = Runtime.getRuntime.availableProcessors)
  (implicit ft : FutureTaskRunner = new scala.concurrent.ThreadRunner) : Sharded[A] =
    toLists(coll, numShards, (a : A) => a);

  def toLists[A,B]
  (coll : Iterable[A], numShards : Int = Runtime.getRuntime.availableProcessors, mapper : (A=>B))
  (implicit ft : FutureTaskRunner = new scala.concurrent.ThreadRunner) = {
    val sizeHint = if (coll.isInstanceOf[Seq[_]]) Some(coll.size / numShards) else None;

    val channels = Array.fill(numShards)(new scala.concurrent.SyncVar[Option[A]]());
    val results = new Array[List[B]](numShards);
    val futures = Array.tabulate(numShards)(i => future({
      var builder = List.newBuilder[B];
      if (sizeHint.isDefined) {
        builder.sizeHint(sizeHint.get);
      }

      var done = false;
      while (!done) {
        channels(i).get match {
          case Some(value) => builder += mapper(value);
          case None => done = true;
        }
      }
      results(i) = builder.result;
    })(ft));


    var i =  0;
    for (item <- coll) {
      channels(i % numShards).put(Some(item));
      i += 1;
    }
    channels.foreach(_.put(None));
    futures.foreach(_.apply());

    new Sharded[B](results.toList);
  }

  def shard[V](iterable : Iterable[V], numShards : Int) : List[LazyIterable[V]] =
    shard(iterable.iterator, numShards).map(iter => LazyIterable[V](iter));

  def shard[V](iterator : Iterator[V], numShards : Int) : List[Iterator[V]] = {
    val cHasNext = Array.fill(numShards)(new scala.concurrent.SyncVar[Boolean]);
    val cNext    = Array.fill(numShards)(new scala.concurrent.SyncVar[V]);
    
    scala.concurrent.ops.spawn {
      var i = 0;
      for (item <- iterator) {
        cHasNext(i % numShards).put(true);
        cNext(i % numShards).put(item);
        i += 1;
      }
      cHasNext.foreach(_.put(false));
    }

    List.tabulate(numShards)(i => {
      new Iterator[V] {
        var knowsHasNext = false;
        var valofHasNext = false;

        override def hasNext = {
          if (!knowsHasNext) {
            knowsHasNext = true;
            valofHasNext = cHasNext(i).get;
          }
          valofHasNext;
        }

        override def next = {
          if (!hasNext) {
            throw new NoSuchElementException();
          }
          knowsHasNext = false;
          cNext(i).get;
        }
      }
    });
  }

  implicit def canBuildFromIterables[B] : CanBuildFrom[Sharded[_],B,Sharded[B]] =
  new CanBuildFrom[Sharded[_],B,Sharded[B]] {
    override def apply(from: Sharded[_]) =
      new NoBuilder[B];

    override def apply() =
      new NoBuilder[B];
  }
}

class ShardedException(msg : String) extends RuntimeException(msg);
