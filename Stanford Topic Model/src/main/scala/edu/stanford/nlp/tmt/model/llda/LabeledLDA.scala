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

import java.io.File;

import scalanlp.ra.Signature;
import scalanlp.text.tokenize.Tokenizer;
import scalanlp.serialization.{DataSerialization, TextSerialization, FileSerialization};
import scalanlp.util.Index;

import scalala.collection.sparse.SparseArray;

import learn.{Stateful,LocalUpdateModelCompanion};

/**
 * Parameters describing LabeledLDA models.
 *
 * @author dramage
 */
@serializable @SerialVersionUID(1)
case class LabeledLDAModelParams(
  numLabels : Int, numTerms : Int,
  labelSmoothing : DirichletParams,
  termSmoothing : DirichletParams,
  labelIndex : Option[Index[String]],
  termIndex : Option[Index[String]],
  tokenizer : Option[Tokenizer]) {

  require(!termIndex.isDefined || termIndex.get.size == numTerms,
          "numTerms and termIndex size do not match");

  require(!labelIndex.isDefined || labelIndex.get.size == numLabels,
          "numLabels and labelIndex size do not match");

  termSmoothing match {
    case SymmetricDirichletParams(s) =>
      /* ok */
    case AsymmetricDirichletParams(arr) =>
      require(arr.length == numTerms, "AsymmetricDirichletParams must provide same number of terms as model's numTerms")
  }

  labelSmoothing match {
    case SymmetricDirichletParams(s) =>
      /* ok */
    case AsymmetricDirichletParams(arr) =>
      require(arr.length == numLabels, "AsymmetricDirichletParams must provide same number of topics as model's numTopics")
  }

  def signature =
    numLabels + "-" + Signature(labelIndex) + "-" + Signature(numTerms, labelSmoothing, termSmoothing);
}

object LabeledLDAModelParams {
  def apply(
    dataset : LabeledLDADataset[_],
    labelSmoothing : DirichletParams = SymmetricDirichletParams(0.01),
    termSmoothing : DirichletParams = SymmetricDirichletParams(0.01)
  ) : LabeledLDAModelParams =
    new LabeledLDAModelParams(
      numLabels = dataset.labelIndex.get.size,
      numTerms = dataset.termIndex.get.size,
      labelSmoothing = labelSmoothing,
      termSmoothing = termSmoothing,
      labelIndex = dataset.labelIndex,
      termIndex = dataset.termIndex,
      tokenizer = dataset.tokenizer
    );

  /** Text serialization format. */
  implicit object TextReadWritable extends TextSerialization.Constructible[LabeledLDAModelParams,(Int,Int,DirichletParams,DirichletParams)] {
    override def name = "LabeledLDAModelParams";
    override def pack(m : LabeledLDAModelParams) =
      (m.numLabels, m.numTerms, m.labelSmoothing, m.termSmoothing);
    override def unpack(r : (Int,Int,DirichletParams,DirichletParams)) =
      LabeledLDAModelParams(
        numLabels = r._1, numTerms = r._2,
        labelSmoothing = r._3, termSmoothing = r._4,
        labelIndex = None, termIndex = None,
        tokenizer = None);
  }

  /** File serialization format. */
  implicit object FileReadWritable extends FileSerialization.ReadWritable[LabeledLDAModelParams] {
    override def read(path : File) = {
      var params = FileSerialization.readText[LabeledLDAModelParams](new File(path, "params.txt"));

      val labelIndexPath = new File(path, "label-index.txt");
      if (labelIndexPath.exists) {
        params = params.copy(labelIndex = Some(FileSerialization.read[Index[String]](labelIndexPath)));
      }

      val termIndexPath = new File(path, "term-index.txt");
      if (termIndexPath.exists) {
        params = params.copy(termIndex = Some(FileSerialization.read[Index[String]](termIndexPath)));
      }

      val tokenizerPath = new File(path, "tokenizer.txt")
      if (tokenizerPath.exists) {
        params = params.copy(tokenizer = Some(FileSerialization.readText[Tokenizer](tokenizerPath)));
      }

      params;
    }

    override def write(path : File, params : LabeledLDAModelParams) = {
      path.mkdirs;

      FileSerialization.writeText(new File(path, "params.txt"), params);

      if (params.labelIndex.isDefined) {
        FileSerialization.write(new File(path, "label-index.txt"), params.labelIndex.get);
      }

      if (params.termIndex.isDefined) {
        FileSerialization.write(new File(path, "term-index.txt"), params.termIndex.get);
      }

      if (params.tokenizer.isDefined) {
        FileSerialization.writeText(new File(path, "tokenizer.txt"), params.tokenizer.get);
      }
    }
  }
}


/**
 * Parameters describing a single Labeled LDA document.
 *
 * @author dramage
 */
@serializable @SerialVersionUID(1)
case class LabeledLDADocumentParams(id : String, labels : Array[Int], terms : Array[Int]);

object LabeledLDADocumentParams {
  implicit object DataFormat extends DataSerialization.Constructible[LabeledLDADocumentParams,(String,Array[Int],Array[Int])] {
    override def name = "LabeledLDADocumentParams";

    override def pack(dp : LabeledLDADocumentParams) =
      (dp.id, dp.labels, dp.terms);

    override def unpack(tup : (String,Array[Int],Array[Int])) =
      LabeledLDADocumentParams(tup._1, tup._2, tup._3);
  }
}


/**
 * Labeled LDA documents contain a set of word observations, a set of
 * labels, and a distribution over model labels.
 *
 * @author dramage
 */
trait LabeledLDADocument[DocState] extends Stateful[DocState] {
  /** Observed terms in the document. */
  def observed : Array[Int];

  /** Active labels in this document. */
  def labels : Array[Int];

  /** Distribution from label -> probability. */
  def signature : SparseArray[Double];
}


/**
 * LabeledLDA models are supervised topic models as described in
 * Ramage, Hall, Nallapati, and Manning EMNLP 2009.
 *
 * @author dramage
 */
trait LabeledLDA[ModelState,Doc<:Stateful[DocState],DocState]
extends TopicModel[LabeledLDAModelParams,ModelState,LabeledLDADocumentParams,Doc,DocState]
with ClosedTopicSet with DirichletTermSmoothing with DirichletTopicSmoothing {
  val params : LabeledLDAModelParams;

  override val numTerms = params.numTerms;

  override val numTopics = params.numLabels;

  super.termSmoothing = params.termSmoothing match {
    case SymmetricDirichletParams(s) => Array.fill(numTerms)(s);
    case AsymmetricDirichletParams(arr) => arr;
  }

  super.topicSmoothing = params.labelSmoothing match {
    case SymmetricDirichletParams(s) => Array.fill(numTopics)(s);
    case AsymmetricDirichletParams(arr) => arr;
  }

  super.termIndex = params.termIndex;

  topicIndex = params.labelIndex;

  super.tokenizer = params.tokenizer;
  
  /** Does inference on the given document until convergence. */
  def infer(doc : Doc) : SparseArray[Double];

  /** Does inference on the given document until convergence. */
  def infer(doc : LabeledLDADocumentParams) : SparseArray[Double] =
    infer(create(doc));

  /** Does inference on the given document until convergence. */
  def infer(doc : String, labels : Array[String]) : SparseArray[Double] = {
    require(topicIndex.isDefined, "Topic index must be defined.");
    infer(create(LabeledLDADocumentParams("(dynamic)",
                 labels.map(topicIndex.get),tokenize(doc).toArray)));
  }
}

/**
 * Labeled LDA model companion - LDA models are all local-update models.
 *
 * @author dramage
 */
trait LabeledLDACompanion
[Model<:LabeledLDA[ModelState,Doc,DocState],ModelState,Doc<:LabeledLDADocument[DocState],DocState]
extends TopicModelCompanion[LabeledLDAModelParams,Model,ModelState,LabeledLDADocumentParams,Doc,DocState]
with LocalUpdateModelCompanion[LabeledLDAModelParams,Model,ModelState,LabeledLDADocumentParams,Doc,DocState];
