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
package model.lda;

import scalanlp.collection.LazyIterable;
import scalanlp.stage.Parcel;
import scalanlp.stage.Item;
import scalanlp.util.Index;
import scalanlp.text.tokenize.Tokenizer;

import data.concurrent.Concurrent;
import data.{Dataset,TermDataset}

/**
 * An LDA dataset is a collection of items; each item has an ID
 * (retrieved with the getID function) and a sequence of terms
 * (retrieved with the getTerms function).  See static constructors
 * in companion object.
 *
 * @author dramage
 */
trait LDADataset[Item] extends LazyIterable[LDADocumentParams]
with Dataset[Item,String,LDADataset[Item]] with TermDataset[Item] {
  override def size =
    items.size;

  override def iterator =
    Concurrent.map(items.iterator, (item : Item) => {
      LDADocumentParams(
        id = getID(item),
        terms = getTerms(item).view.map(termIndex.get).filter(_ >= 0).toArray)
    });;
}

/** More static constructors for Dataset. */
object LDADataset {
  /** Constructs from a batch that contains text. */
  def apply[ID](text : Parcel[LazyIterable[Item[ID,Iterable[String]]]])
  : LDADataset[Item[ID,Iterable[String]]] = {
    new LDADataset[Item[ID,Iterable[String]]] {
      override def name = text.toString;
      override def items = text.data;
      override def getID(x : Item[ID,Iterable[String]]) = x.id.toString;
      override def getTerms(x : Item[ID,Iterable[String]]) = x.value;
      override val termIndex = {
        if (text.meta.contains[scalanlp.stage.text.TermCounts]) {
          Some(text.meta[scalanlp.stage.text.TermCounts].index);
        } else {
          None;
        }
      }
      override val tokenizer = {
        if (text.meta.contains[scalanlp.text.tokenize.Tokenizer]) {
          Some(text.meta[scalanlp.text.tokenize.Tokenizer]);
        } else {
          None;
        }
      }
    }
  }

  /** Constructs from a batch that contains text. */
  def apply[ID](text : Parcel[LazyIterable[Item[ID,Iterable[String]]]], termIndex : Option[Index[String]])
  : LDADataset[Item[ID,Iterable[String]]] = {
    val _termIndex = termIndex;
    new LDADataset[Item[ID,Iterable[String]]] {
      override def name = text.toString;
      override def items = text.data;
      override def getID(x : Item[ID,Iterable[String]]) = x.id.toString;
      override def getTerms(x : Item[ID,Iterable[String]]) = x.value;
      override val termIndex = _termIndex;
      override val tokenizer = {
        if (text.meta.contains[scalanlp.text.tokenize.Tokenizer]) {
          Some(text.meta[scalanlp.text.tokenize.Tokenizer]);
        } else {
          None;
        }
      }
    }
  }

  /** Constructs from raw documents (iterable of Iterables of String). */
  def apply(name : String, terms : Iterable[Iterable[String]],
            termIndex : Option[Index[String]] = None,
            tokenizer : Option[Tokenizer] = None)
  : LDADataset[(Iterable[String],Int)] = {
    val inName = name;
    val inTerms = terms;
    val inTermIndex = termIndex;
    val inTokenizer = tokenizer;

    type X = (Iterable[String],Int);

    new LDADataset[X] {
      override def name = inName;
      override def items = inTerms.zipWithIndex;
      override def getID(x : X) = x._2.toString;
      override def getTerms(x : X) = x._1;
      override val termIndex = inTermIndex;
      override val tokenizer = inTokenizer;
    }
  }
}
