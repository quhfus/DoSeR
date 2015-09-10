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
package plda;

import java.io.File;

import scalala.collection.sparse.SparseArray;

import scalanlp.ra.Signature;
import scalanlp.text.tokenize.Tokenizer;
import scalanlp.serialization.{DataSerialization, TextSerialization, FileSerialization};
import scalanlp.util.Index;

import llda.{LabeledLDADataset,LabeledLDADocumentParams};
import learn.Stateful;

/**
 * Parameters describing PLDA models.
 *
 * @param numLatentTopics Number of latent topics applied to each document.
 * @param numTopicsPerLabel For each label, how many topics.
 * @param numTerms Total number of terms in the model.
 * @param topicSmoothing per-topic Dirichlet smoothing
 * @param termSmoothing per-term Dirichlet smoothing
 * @param labelIndex Names for each label
 * @param topicIndex Names for each topic (with numTopicsPerLabel for each label)
 * @param termIndex Names for each term
 * @param tokenizer Tokenizer used during construction
 */
@serializable @SerialVersionUID(1)
case class PLDAModelParams(
  numLatentTopics : Int,
  numTopicsPerLabel : Array[Int],
  numTerms : Int,
  topicSmoothing : DirichletParams,
  termSmoothing : DirichletParams,
  labelIndex : Option[Index[String]],
  topicIndex : Option[Index[String]],
  termIndex : Option[Index[String]],
  tokenizer : Option[Tokenizer]) {

  def numLabels = numTopicsPerLabel.length;

  def numTopics = numLatentTopics + numTopicsPerLabel.sum;

  require(!termIndex.isDefined || termIndex.get.size == numTerms,
          "numTerms and termIndex size do not match");
  require(!labelIndex.isDefined || labelIndex.get.size == numLabels,
          "numLabels and labelIndex size do not match");
  require(!topicIndex.isDefined || topicIndex.get.size == numTopics,
          "numTopics and topicIndex size do not match");

  def signature =
    numLatentTopics + "-" + Signature(numTopicsPerLabel, labelIndex) + "-" + Signature(numTerms, topicSmoothing, termSmoothing);
}

/**
 * Parameters describing how many topics per label.
 *
 * @author dramage
 */
@serializable @SerialVersionUID(1)
sealed trait NumTopicsPerLabelParams;

/**
 * Specifies extactly k topics should be available on each label.
 *
 * @author dramage
 */
case class SharedKTopicsPerLabel(k : Int) extends NumTopicsPerLabelParams;

/**
 * Sepcifies the specific number of topics per label via a map.
 *
 * @author dramage
 */
case class CustomKTopicsPerLabel(map : Map[String,Int]) extends NumTopicsPerLabelParams;

/** More static constructors for ModelParams class. */
object PLDAModelParams {
  def apply(
    dataset : LabeledLDADataset[_],
    numLatentTopics : Int,
    numTopicsPerLabel : NumTopicsPerLabelParams,
    topicSmoothing : DirichletParams = SymmetricDirichletParams(0.01),
    termSmoothing : DirichletParams = SymmetricDirichletParams(0.01)
  ) : PLDAModelParams = {
    val ntpl = numTopicsPerLabel match {
      case SharedKTopicsPerLabel(k) => Array.fill(dataset.labelIndex.get.size)(k);
      case CustomKTopicsPerLabel(m) => dataset.labelIndex.get.map(lbl => m(lbl)).toArray;
    };
    new PLDAModelParams(
      numLatentTopics = numLatentTopics,
      numTopicsPerLabel = ntpl,
      numTerms = dataset.termIndex.get.size,
      topicSmoothing = topicSmoothing,
      termSmoothing = termSmoothing,
      labelIndex = dataset.labelIndex,
      topicIndex = mkTopicIndex(numLatentTopics, ntpl, dataset.labelIndex),
      termIndex = dataset.termIndex,
      tokenizer = dataset.tokenizer
    );
  }

  protected def mkTopicIndex(numLatentTopics : Int, numTopicsPerLabel : Array[Int], labelIndex : Option[Index[String]]) = {
    labelIndex match {
      case Some(index) =>
        require(index.size == numTopicsPerLabel.length, "topic index does not match numTopicsPerLabel");
        Some(Index(List.tabulate(numLatentTopics)(k => "*Latent* - "+k) ++
                   ( for (j <- 0 until index.size;
                         k <- 0 until numTopicsPerLabel(j))
                    yield index.get(j)+" - "+k)));
      case None => None;
    }
  }

  /** Text serialization format. */
  implicit object TextReadWritable extends TextSerialization.Constructible[PLDAModelParams,((Int,Array[Int]),Int,DirichletParams,DirichletParams)] {
    override def name = "PLDAModelParams";
    override def pack(m : PLDAModelParams) =
      ((m.numLatentTopics, m.numTopicsPerLabel), m.numTerms, m.topicSmoothing, m.termSmoothing);
    override def unpack(r : ((Int,Array[Int]),Int,DirichletParams,DirichletParams)) =
      PLDAModelParams(
        numLatentTopics = r._1._1,
        numTopicsPerLabel = r._1._2,
        numTerms = r._2,
        topicSmoothing = r._3, termSmoothing = r._4,
        labelIndex = None, topicIndex = None, termIndex = None, tokenizer = None);
  }

  /** File serialization format. */
  implicit object FileReadWritable extends FileSerialization.ReadWritable[PLDAModelParams] {
    override def read(path : File) = {
      var params = FileSerialization.readText[PLDAModelParams](new File(path, "params.txt"));

      val labelIndexPath = new File(path, "label-index.txt");
      if (labelIndexPath.exists) {
        params = params.copy(labelIndex = Some(FileSerialization.read[Index[String]](labelIndexPath)));
      }

      val topicIndexPath = new File(path, "topic-index.txt");
      if (topicIndexPath.exists) {
        params = params.copy(topicIndex = Some(FileSerialization.read[Index[String]](topicIndexPath)));
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

    override def write(path : File, params : PLDAModelParams) = {
      path.mkdirs;

      FileSerialization.writeText(new File(path, "params.txt"), params);

      if (params.labelIndex.isDefined) {
        FileSerialization.write(new File(path, "label-index.txt"), params.labelIndex.get);
      }

      if (params.topicIndex.isDefined) {
        FileSerialization.write(new File(path, "topic-index.txt"), params.topicIndex.get);
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
 * PLDA models.
 *
 * @author dramage
 */
trait PLDA[ModelState,Doc<:Stateful[DocState],DocState]
extends TopicModel[PLDAModelParams,ModelState,LabeledLDADocumentParams,Doc,DocState]
with ClosedTopicSet with DirichletTermSmoothing with DirichletTopicSmoothing {
  val params : PLDAModelParams;

  override val numTerms = params.numTerms;

  override val numTopics = params.numTopics;

  this.termSmoothing = params.termSmoothing match {
    case SymmetricDirichletParams(s) => Array.fill(numTerms)(s);
    case AsymmetricDirichletParams(arr) => Array.tabulate(numTerms)(arr);
  }

  this.topicSmoothing = params.topicSmoothing match {
    case SymmetricDirichletParams(s) => Array.fill(numTopics)(s);
    case AsymmetricDirichletParams(arr) => Array.tabulate(numTopics)(arr);
  }

  this.termIndex = params.termIndex;

  this.topicIndex = params.topicIndex;

  this.tokenizer = params.tokenizer;

  /** Starting offset within the topic array for topics with the given label. */
  protected val labelTopicOffset : Array[Int] = {
    val rv = new Array[Int](params.numLabels);
    rv(0) = params.numLatentTopics;
    var i = 1;
    while (i < rv.length) {
      rv(i) = rv(i-1) + params.numTopicsPerLabel(i-1);
      i += 1;
    }
    rv;
  }

  /** Returns the topic number for the given topic within the given label. */
  def labelTopicIndex(label : Int, topic : Int) : Int = {
    if (label < 0) {
      require(topic < params.numLatentTopics, "Latent topic out of bounds");
      topic;
    } else {
      require(topic < params.numTopicsPerLabel(label), "Topic out of bounds for label");
      labelTopicOffset(label) + topic;
    }
  }
  
  /** Does inference on the given document until convergence. */
  def infer(doc : Doc) : SparseArray[Double];

  /** Does inference on the given document until convergence. */
  def infer(doc : PLDADocumentParams) : SparseArray[Double] =
    infer(create(doc));

  /** Does inference on the given document until convergence. */
  def infer(doc : String, labels : Array[String]) : SparseArray[Double] = {
    require(topicIndex.isDefined, "Topic index must be defined.");
    infer(create(LabeledLDADocumentParams("(dynamic)",
                 labels.map(topicIndex.get),tokenize(doc).toArray)));
  }
}

