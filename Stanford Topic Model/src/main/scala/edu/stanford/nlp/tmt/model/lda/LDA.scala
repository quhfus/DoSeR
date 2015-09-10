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
package lda;

import java.io.File;

import scalanlp.ra.Signature;
import scalanlp.text.tokenize.Tokenizer;
import scalanlp.serialization.{DataSerialization, TextSerialization, FileSerialization};
import scalanlp.util.Index;

import data.concurrent.Concurrent;
import learn.{Stateful,LocalUpdateModelCompanion,LogProbabilityEstimateModelCompanion};

/**
 * Parameters describing LDA models.
 *
 * @author dramage
 */
@serializable @SerialVersionUID(1)
case class LDAModelParams
(numTopics : Int,  numTerms : Int,
 topicSmoothing : DirichletParams,
 termSmoothing : DirichletParams,
 termIndex : Option[Index[String]],
 tokenizer : Option[Tokenizer]) {

  require(!termIndex.isDefined || termIndex.get.size == numTerms,
          "numTerms and termIndex size do not match");

  termSmoothing match {
    case SymmetricDirichletParams(s) =>
      /* ok */
    case AsymmetricDirichletParams(arr) =>
      require(arr.length == numTerms, "AsymmetricDirichletParams must provide same number of terms as model's numTerms");
  }

  topicSmoothing match {
    case SymmetricDirichletParams(s) =>
      /* ok */
    case AsymmetricDirichletParams(arr) =>
      require(arr.length == numTopics, "AsymmetricDirichletParams must provide same number of topics as model's numTopics");
  }


  def signature =
    numTopics + "-" + Signature((numTerms, topicSmoothing, termSmoothing));
}

object LDAModelParams {
  def apply(
    numTopics : Int, dataset : LDADataset[_],
    topicSmoothing : DirichletParams = SymmetricDirichletParams(0.01),
    termSmoothing : DirichletParams = SymmetricDirichletParams(0.01)
  ) : LDAModelParams =
    new LDAModelParams(
      numTopics = numTopics,
      numTerms = dataset.termIndex.get.size,
      topicSmoothing = topicSmoothing,
      termSmoothing = termSmoothing,
      termIndex = dataset.termIndex,
      tokenizer = dataset.tokenizer
    );

  implicit object TextReadWritable extends TextSerialization.Constructible[LDAModelParams,(Int,Int,DirichletParams,DirichletParams)] {
    override def name = "LDAModelParams";
    override def pack(m : LDAModelParams) =
      (m.numTopics, m.numTerms, m.topicSmoothing, m.termSmoothing);
    override def unpack(r : (Int,Int,DirichletParams,DirichletParams)) =
      LDAModelParams(numTopics = r._1, numTerms = r._2, topicSmoothing = r._3, termSmoothing = r._4, termIndex = None, tokenizer = None);
  }

  implicit object FileReadWritable extends FileSerialization.ReadWritable[LDAModelParams] {
    override def read(path : File) = {
      var params = FileSerialization.readText[LDAModelParams](new File(path, "params.txt"));

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

    override def write(path : File, params : LDAModelParams) = {
      path.mkdirs;
      
      FileSerialization.writeText(new File(path, "params.txt"), params);

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
 * Parameters describing a single LDA document
 *
 * @author dramage
 */
@serializable @SerialVersionUID(1)
case class LDADocumentParams(id : String, terms : Array[Int]);

object LDADocumentParams {
  implicit object DataFormat extends DataSerialization.Constructible[LDADocumentParams,(String,Array[Int])] {
    override def name = "LDADocumentParams";

    override def pack(dp : LDADocumentParams) =
      (dp.id, dp.terms);

    override def unpack(tup : (String,Array[Int])) =
      LDADocumentParams(tup._1, tup._2);
  }
}

/**
 * LDA documents contain a set of word observations and a distribution
 * over topics.
 *
 * @author dramage
 */
trait LDADocument[DocState] extends Stateful[DocState] {
  /** Observed terms in the document. */
  def observed : Array[Int];

  /** Distribution of topic->probability. */
  def signature : Array[Double];
}

/**
 * LDA models are a TopicModel on a fixed set of k topics with dirichlet
 * term and topic smoothing.
 *
 * @author dramage
 */
trait LDA[ModelState,Doc<:LDADocument[DocState],DocState]
extends TopicModel[LDAModelParams,ModelState,LDADocumentParams,Doc,DocState]
with ClosedTopicSet with DirichletTermSmoothing with DirichletTopicSmoothing {
  val params : LDAModelParams;

  override val numTerms = params.numTerms;

  override val numTopics = params.numTopics;
  
  super.termSmoothing = params.termSmoothing match {
    case SymmetricDirichletParams(s) => Array.fill(numTerms)(s);
    case AsymmetricDirichletParams(arr) => arr;
  }

  super.topicSmoothing = params.topicSmoothing match {
    case SymmetricDirichletParams(s) => Array.fill(numTopics)(s);
    case AsymmetricDirichletParams(arr) => arr;
  }

  super.termIndex = params.termIndex;

  super.tokenizer = params.tokenizer;

  /** Does inference on the given document until convergence. */
  def infer(doc : Doc) : Array[Double];

  /** Does inference on the given document until convergence. */
  def infer(doc : LDADocumentParams) : Array[Double] =
    infer(create(doc));

  /** Does inference on the given document until convergence. */
  def infer(doc : String) : Array[Double] =
    infer(create(LDADocumentParams("(dynamic)",tokenize(doc).toArray)))

  /**
   * Computes the log probability for the current document.  This measure treats
   * the assignment to theta and the model counts as observed.  Returns
   * sum_i P(w_i | theta*, beta*).  Beta maps from (topic,term) to probability.
   */
  def computeLogPW(doc : Doc) : Double = {
    // P(w_i | theta*, beta*)
    //  = \sum_z P(w_i,z_i | theta*, beta*)
    //  = \sum_z P(w_i | beta*, z_i) \cdot P(z_i | theta*)

    var sumLogPW = 0.0;
    val theta = doc.signature;
    for (term <- doc.observed) {
      var pW = 0.0;
      var topic = 0;
      while (topic < theta.length) {
        pW += pTopicTerm(topic,term) * theta(topic);
        topic += 1;
      }
      sumLogPW += math.log(pW);
    }

    return sumLogPW;
  }

  /**
   * Computes the average per-word perplexity of the given dataset.
   */
  def computePerplexity(docs : Traversable[LDADocumentParams]) : Double = {
    val (crossEntropy,numTerms) =
      Concurrent.mapreduce(docs,
        map = ((dp : LDADocumentParams) => computeCrossEntropy(dp)),
        reduce = ((a : (Double,Int), b : (Double,Int)) => (a._1+b._1,a._2+b._2))
      );

    math.pow(2, -crossEntropy / (math.log(2) * numTerms));
  }

  /**
   * Computes the total cross-entropy of the terms in the second
   * half of the document based on an estimate of theta from the
   * terms in the fisrt half of the doucment.  Returns
   * (sum crossEntropy, numTerms).  This is used as the basis of
   * computePerplexity.
   */
  def computeCrossEntropy(doc : LDADocumentParams) : (Double,Int) = {
    if (doc.terms.length < 2) {
      (0.0,0);
    } else {
      val learnDoc = create(
        LDADocumentParams(doc.id, terms = doc.terms.take(doc.terms.length / 2)));
      val inferDoc = create(
        LDADocumentParams(doc.id, terms = doc.terms.drop(doc.terms.length / 2)));

      val theta = infer(learnDoc);

      var sumLogPW = 0.0;
      for (term <- inferDoc.observed) {
        var pW = 0.0;
        var topic = 0;
        while (topic < theta.length) {
          pW += pTopicTerm(topic,term) * theta(topic);
          topic += 1;
        }
        sumLogPW += math.log(pW);
      }

      // return math.pow(2, -crossEntropy / (math.log(2) * wordCount));
      (sumLogPW, inferDoc.observed.length);
    }
  }
}

/**
 * LDA model companion - LDA models are all local-update models.
 *
 * @author dramage
 */
trait LDACompanion
[Model<:LDA[ModelState,Doc,DocState],ModelState,Doc<:LDADocument[DocState],DocState]
extends TopicModelCompanion[LDAModelParams,Model,ModelState,LDADocumentParams,Doc,DocState]
with LocalUpdateModelCompanion[LDAModelParams,Model,ModelState,LDADocumentParams,Doc,DocState]
with LogProbabilityEstimateModelCompanion[Model,Doc] {

  def getLogProbabilityEstimate(model : Model, doc : Doc) : Double =
    model.computeLogPW(doc);
}
