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

import scalanlp.collection.LazyIterable;
import scalanlp.text.tokenize.Tokenizer;
import scalanlp.util.Index;

/**
 * A Dataset is a collection of Items with a function that can return
 * a unique identifier for each item.
 *
 * @author dramage
 */
trait Dataset[Item,ID,This] {
  /** Name of this dataset. */
  def name : String;

  /** Collection of items. */
  def items : Iterable[Item];

  /** Returns a unique identifier for each item. */
  def getID(item : Item) : ID;

  /** A view of this dataset as its underlying terms. */
  def asIDs =
    items.map(item => getID(item));

  def size =
    items.size;

  /** Signature is the signature of the name. */
  def signature =
    scalanlp.ra.Signature(name);

  /** Returns this.name. */
  override def toString = name;
}



object Dataset {
  protected class QueuedIterator[T,ID](iter : Iterator[T], id : T=>ID) extends Iterator[T] {
    val queue = new scala.collection.mutable.Queue[T]();
    val idSet = new scala.collection.mutable.HashSet[ID]();

    override def hasNext =
      !queue.isEmpty || iter.hasNext;

    override def next = {
      if (!queue.isEmpty) {
        val item = queue.dequeue;
        idSet -= id(item);
        item;
      } else {
        iter.next;
      }
    }

    def enqueue : ID = {
      val item = iter.next;
      val itemId = id(item);
      queue.enqueue(item);
      require(idSet.add(itemId), "Duplicate id "+itemId+" in iterator");
      itemId;
    }

    /** Drops elements from the queue until the given id. */
    def discardUntil(to : ID) = {
      require(idSet.contains(to), "Can only discard up to items that have been queued.");
      var itemId = id(queue.head);
      while (itemId != to) {
        idSet.remove(itemId);
        queue.dequeue;
        itemId = id(queue.head);
      }
    }

    /** Returns true if the given id has already been queued. */
    def hasQueuedID(id : ID) =
      idSet.contains(id);

    /** Returns true if underlying iterator still has remaining items. */
    def hasUnqueued =
      iter.hasNext;
  }

  /**
   * Performs an inner join on the two iterables using the given id
   * functions to get their join id's.
   */
  def join[A,B,ID]
  (a : Iterable[A], idA : A=>ID, b : Iterable[B], idB : B => ID)
  : LazyIterable[(A,B)] =
    join(a,idA,b,idB,(ia:A,ib:B) => (ia,ib));

  /**
   * Performs an inner join on the two iterables using the given id
   * functions to get their join id's.
   */
  def join[A,B,C,ID]
  (a : Iterable[A], idA : A=>ID, b : Iterable[B], idB : B=> ID, fn : (A,B) => C)
  : LazyIterable[C] = new LazyIterable[C] {
    override def iterator = new Iterator[C] {
      val aIter = new QueuedIterator(a.iterator, idA);
      val bIter = new QueuedIterator(b.iterator, idB);

      private val LEFT = true;
      private val RIGHT = false;

      // initial state depends on next status
      var current : Option[C] = prepare();

      var pull = LEFT;

      /** Move until the next matching pair. */
      def prepare() : Option[C] = {
        while (aIter.hasUnqueued || bIter.hasUnqueued) {
          if (pull == LEFT) {
            if (aIter.hasUnqueued) {
              val id = aIter.enqueue;
              if (bIter.hasQueuedID(id)) {
                aIter.discardUntil(id);
                bIter.discardUntil(id);
                return Some(fn(aIter.next, bIter.next));
              }
            }
            pull = RIGHT;
          } else if (pull == RIGHT) {
            if (bIter.hasUnqueued) {
              val id = bIter.enqueue;
              if (aIter.hasQueuedID(id)) {
                bIter.discardUntil(id);
                aIter.discardUntil(id);
                return Some(fn(aIter.next, bIter.next));
              }
            }
            pull = LEFT;
          }
        }
        return None;
      }

      override def hasNext =
        current.isDefined;

      override def next = {
        val rv = current.get;
        current = prepare();
        rv;
      }
    }
  }
}


/**
 * A TermDataset has a set of terms associated with each item, and, optionally
 * an index of terms and a tokenizer describing how those terms were created.
 *
 * @author dramage
 */
trait TermDataset[Item] {
  this : Dataset[Item,_,_] =>

  /** The tokenizer used to tokenize this dataset. */
  def getTerms(item : Item) : Iterable[String];

  /** A view of this dataset as its underlying terms. */
  def asTerms(termIndex : Index[String] = this.termIndex.get) =
    items.map(item => getTerms(item).view.map(termIndex).filter(_ >= 0).toArray);

  //
  // Optional attributes
  //

  /** The index of terms in this dataset. */
  val termIndex : Option[Index[String]] = None;

  /** The base tokenizer used when generating this dataset. */
  val tokenizer : Option[Tokenizer] = None;
}


/**
 * A MultiLabelDataset has a set of labels associated with each item, and,
 * optionally an index of labels.
 *
 * @author dramage
 */
trait MultiLabelDataset[Item,Label] {
  this : Dataset[Item,_,_] =>

  /** The tokenizer used to tokenize this dataset. */
  def getLabels(item : Item) : Iterable[Label];

  /** A view of this dataset as an Iterable of Labels. */
  def asLabels(labelIndex : Index[Label] = this.labelIndex.get) =
    items.map(item => getLabels(item).view.map(labelIndex).filter(_ >= 0).toArray);

  /** Optional index of labels in this dataset. */
  val labelIndex : Option[Index[Label]] = None;
}

