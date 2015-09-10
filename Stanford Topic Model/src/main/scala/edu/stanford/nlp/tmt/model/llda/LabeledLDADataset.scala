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
package model;
package llda;

import scalanlp.collection.LazyIterable;
import scalanlp.serialization.DataSerialization;
import scalanlp.stage.Parcel;
import scalanlp.stage.Item;
import scalanlp.util.Index;
import scalanlp.text.tokenize.Tokenizer;

import data.concurrent.Concurrent;
import data.{Dataset,TermDataset,MultiLabelDataset};

/**
 * An LabeledLDA dataset is a collection of items; each item has an ID
 * (retrieved with the getID function), a sequence of terms
 * (retrieved with the getTerms function), and a set of labels
 * (retrieved with the getLabels function).  See static constructors
 * in companion object.
 *
 * @author dramage
 */
trait LabeledLDADataset[Item] extends LazyIterable[LabeledLDADocumentParams]
with Dataset[Item,String,LabeledLDADataset[Item]] with TermDataset[Item] with MultiLabelDataset[Item,String] {

  override def size =
    items.size;

  override def iterator =
    Concurrent.map(items.iterator,
      (item : Item) => LabeledLDADocumentParams(
        id = getID(item),
        terms = getTerms(item).view.map(termIndex.get).filter(_ >= 0).toArray,
        labels = getLabels(item).view.map(labelIndex.get).filter(_ >= 0).toArray));
}

/** More static constructors for Dataset. */
object LabeledLDADataset {
  /** Constructs from a batch that contains text and another that contains IDs. */
  def apply[ID](text : Parcel[LazyIterable[Item[ID,Iterable[String]]]],
                labels : Parcel[LazyIterable[Item[ID,Iterable[String]]]])
  : LabeledLDADataset[(ID,Iterable[String],Iterable[String])] = {
    type X=(ID,Iterable[String],Iterable[String]);

    new LabeledLDADataset[(ID,Iterable[String],Iterable[String])] {
      override def name = text.toString;
      override def items = Dataset.join(
        text.data, (item : Item[ID,_]) => item.id,
        labels.data, (item : Item[ID,_]) => item.id,
        (a : Item[ID,Iterable[String]], b : Item[ID,Iterable[String]]) => (a.id, a.value, b.value)
      );
      override def getID(x : X) = x._1.toString;
      override def getTerms(x : X) = x._2;
      override def getLabels(x : X) = x._3;
      override val termIndex = {
        if (text.meta.contains[scalanlp.stage.text.TermCounts]) {
          Some(text.meta[scalanlp.stage.text.TermCounts].index);
        } else {
          None;
        }
      }
      override val labelIndex = {
        if (labels.meta.contains[scalanlp.stage.text.TermCounts]) {
          Some(labels.meta[scalanlp.stage.text.TermCounts].index);
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

  /** Constructs from a batch that contains text and another that contains IDs. */
  def apply[ID](text : Parcel[LazyIterable[Item[ID,Iterable[String]]]],
                labels : Parcel[LazyIterable[Item[ID,Iterable[String]]]],
                termIndex : Option[Index[String]],
                labelIndex : Option[Index[String]])
  : LabeledLDADataset[(ID,Iterable[String],Iterable[String])] = {
    type X=(ID,Iterable[String],Iterable[String]);
    val _termIndex = termIndex;
    val _labelIndex = labelIndex;

    new LabeledLDADataset[(ID,Iterable[String],Iterable[String])] {
      override def name = text.toString;
      override def items = Dataset.join(
        text.data, (item : Item[ID,_]) => item.id,
        labels.data, (item : Item[ID,_]) => item.id,
        (a : Item[ID,Iterable[String]], b : Item[ID,Iterable[String]]) => (a.id, a.value, b.value)
      );
      override def getID(x : X) = x._1.toString;
      override def getTerms(x : X) = x._2;
      override def getLabels(x : X) = x._3;
      override val termIndex = _termIndex;
      override val labelIndex = _labelIndex;
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
  def apply(name : String,
            terms : Iterable[Iterable[String]],
            labels : Iterable[Iterable[String]],
            termIndex : Option[Index[String]] = None,
            labelIndex : Option[Index[String]] = None,
            tokenizer : Option[Tokenizer] = None)
  : LabeledLDADataset[((Iterable[String],Iterable[String]),Int)] = {
    val inName = name;
    val inTerms = terms;
    val inLabels = labels;
    val inTermIndex = termIndex;
    val inLabelIndex = labelIndex;
    val inTokenizer = tokenizer;

    type X = ((Iterable[String],Iterable[String]),Int);

    new LabeledLDADataset[X] {
      override def name = inName;
      override def items = (inTerms zip inLabels).zipWithIndex;
      override def getID(x : X) = x._2.toString;
      override def getTerms(x : X) = x._1._1;
      override def getLabels(x : X) = x._1._2;
      override val termIndex = inTermIndex;
      override val labelIndex = inLabelIndex;
      override val tokenizer = inTokenizer;
    }
  }

  // class ReadWritable[Item,ID] extends DataSerialization.Constructible[LabeledLDADataset[Item,ID],]
}
