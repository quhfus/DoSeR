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

import scalala.operators.Implicits._;
import scalala.library.random.MersenneTwisterFast;
import scalala.collection.sparse.SparseArray;

import data.concurrent.AsyncMath;
import llda.LabeledLDADocumentParams;
import learn.{SerializableModelCompanion,DataParallelModelCompanion};



/**
 * CVB0PLDA document.  The assignments in assigned and the totals in
 * theta must be kept in sync by the caller.
 *
 * @author dramage
 */
class CVB0PLDADocument(
  override val id : String,
  override val observed : Array[Int],
  val topics : Array[Int],
  override val termIndex : Array[Int],
  override val termCount : Array[Short],
  override val termTheta : Array[Array[Double]],
  override val theta : Array[Double],
  val model : CVB0PLDA
) extends SoftAssignmentDocument {

  override def checkrep() {
    super.checkrep();
    require(theta.length == topics.length);
    var tI = 0;
    while (tI < termTheta.length) {
      require(termTheta(tI).length == topics.length);
      tI += 1;
    }
  }

  /** Clear all assignments */
  def reset = {
    theta := 0;

    var wI = 0;
    while (wI < termTheta.length) {
      var zI = 0;
      var z = topics(zI);
      while (zI < termTheta(wI).length) {
        termTheta(wI)(zI) = model.topicSmoothing(z);
        theta(zI) += termTheta(wI)(zI);
        zI += 1;
      }
      wI += 1;
    }
  }

  /** Returns the per-topic probability. */
  def signature : SparseArray[Double] = {
    val avg = new SparseArray[Double](model.numTopics, topics.length);
    var tI = 0;
    while (tI < termTheta.length) {
      var zI = 0;
      while (zI < topics.length) {
        avg(topics(zI)) += termTheta(tI)(zI);
        zI += 1;
      }
      tI += 1;
    }
    avg :/= termTheta.length;
    avg;
  }
}


object CVB0PLDADocument {
  /**
   * Creates a new CVB0PLDADocument from the given array of labels and
   * terms.  If any term occurs more than Short.MaxValue times, all counts
   * are downscaled by (Short.MaxValue - 1.0) / maxTermCount, rounding up to
   * the nearest whole Short.
   */
  def apply(id : String, labels : Array[Int], terms : Array[Int], model : CVB0PLDA) = {
    var i = 0;
    while (i < terms.length) {
      if (!(terms(i) >= 0 && terms(i) < model.numTerms)) {
        throw new IllegalArgumentException("Observation "+i+" ("+terms(i)+") out of bounds ["+0+","+model.numTerms+")");
      }
      i += 1;
    }
    val (termIndex, termCount) = SoftAssignmentDocument.createTermIndexAndCountFromObserved(terms);

    val topics = (
      Iterator.range(0, model.params.numLatentTopics) ++
      ( for (label <- labels.iterator; topic <- Iterator.range(0, model.params.numTopicsPerLabel(label)))
        yield model.labelTopicIndex(label,topic)
      )
    ).toArray;

    new CVB0PLDADocument(
      id = id, observed = terms, topics = topics, termIndex = termIndex, termCount = termCount,
      termTheta = Array.tabulate(termIndex.size)(t => new Array[Double](topics.length)),
      theta = new Array[Double](topics.length), model = model);
  }
}

/**
 * CVB0 inference for PLDA.
 *
 * @author dramage
 */
class CVB0PLDA
(override val params : PLDAModelParams,
 val seed : Long = 0l,
 override val log : (String=>Unit) = System.err.println)
extends PLDA[SoftAssignmentModelState,CVB0PLDADocument,(String,Array[Array[Double]])]
with SoftAssignmentModel[PLDAModelParams,PLDADocumentParams,CVB0PLDADocument] {

  checkrep();

  override def create(dp : PLDADocumentParams) =
    CVB0PLDADocument(dp.id, terms = dp.terms, labels = dp.labels, model = this);

  override def reset = {
    val random = new MersenneTwisterFast(seed);
    for (topic <- 0 until numTopics) {
      countTopic(topic) = 0;
      for (term <- 0 until numTerms) {
        countTopicTerm(topic)(term) =
          termSmoothing(term) + (random.nextDouble * termSmoothing(term) / 10);
        countTopic(topic) += countTopicTerm(topic)(term);
      }
    }
  }

  /**
   * Returns an array of per-label probabilities.  Loops while the largest
   * difference between iterations in probabilities for any given topic is
   * greater than delta (default 1e-5).
   */
  def infer(doc : CVB0PLDADocument, delta : Double) : SparseArray[Double] = {
    doAssignments(doc, learn = false);

    // the reference distribution to compare to doc.theta
    var last = new Array[Double](doc.theta.length);
    last := doc.theta;
    
    var maxgap = Double.MinValue;
    do {
      doAssignments(doc, learn = false);

      // set last := doc.theta and figure out largest gap
      var i = 0;
      maxgap = Double.MinValue;
      while (i < last.length) {
        val normedThetaI = doc.theta(i) / doc.observed.length;
        val gap = math.abs(normedThetaI - last(i));
        last(i) = normedThetaI;
        if (gap > maxgap) maxgap = gap;
        i += 1;
      }
    } while (maxgap > delta);

    doc.signature;
  }
  
  /**
   * Returns an array of per-label probabilities.  Loops while the largest
   * difference between iterations in probabilities for any given topic is
   * greater than delta (default 1e-5).
   */
  override def infer(doc : CVB0PLDADocument) : SparseArray[Double] =
    infer(doc, 1e-5);

  def doAssignments(doc : CVB0PLDADocument, learn : Boolean = true) {
    { // compute new assignments
      var tI = 0;
      while (tI < doc.termIndex.length) {
        val term = doc.termIndex(tI);
        var norm  = 0.0;

        {
          var zI = 0;
          while (zI < doc.topics.length) {
            val z = doc.topics(zI);
            val oldThetaAssignment = doc.termTheta(tI)(zI);
            val oldCountAssignment = if (learn) oldThetaAssignment else 0.0;
            val newAssignment =
              ((countTopicTerm(z)(term) - oldCountAssignment + termSmoothing(term))
               / (countTopic(z) - oldCountAssignment + termSmoothDenom)
               * (doc.theta(zI) - oldThetaAssignment + topicSmoothing(z)));
            doc.termTheta(tI)(zI) = newAssignment;
            norm += newAssignment;

            zI += 1;
          }
        }

        {
          var zI = 0;
          while (zI < doc.topics.length) {
            doc.termTheta(tI)(zI) /= norm;
            zI += 1;
          }
        }

        tI += 1;
      }
    }

    { // update topic totals
      java.util.Arrays.fill(doc.theta, 0.0);
      var tI = 0;
      while (tI < doc.termIndex.length) {
        var zI = 0;
        while (zI < doc.topics.length) {
          doc.theta(zI) += doc.termCount(tI) * doc.termTheta(tI)(zI);
          zI += 1;
        }
        tI += 1;
      }
    }
  }

  def doCounts(doc : CVB0PLDADocument) {
    var zI = 0;
    while (zI < doc.topics.length) {
      val z = doc.topics(zI);
      var tI = 0;
      while (tI < doc.termIndex.length) {
        val t = doc.termIndex(tI);
        countTopicTerm(z)(t) += doc.termCount(tI) * doc.termTheta(tI)(zI);
        tI += 1;
      }
      countTopic(z) += doc.theta(zI);
      zI += 1;
    }
  }
  
  
  /**
   * Creates a view of this model as a standard CVB0LDA model for doing
   * inference unconstrained by an observed set of topics on each document.
   */
  def asCVB0LDA : lda.CVB0LDA = {
    val mp = lda.LDAModelParams(
        numTopics = params.numTopics,
        numTerms = params.numTerms,
        topicSmoothing = params.topicSmoothing,
        termSmoothing = params.termSmoothing,
        termIndex = params.termIndex,
        tokenizer = params.tokenizer
      );  
    val model = new lda.CVB0LDA(
      params = mp, seed = seed, log = log);
    model.state = this.state;
    model;
  }
}

@serializable
object CVB0PLDA extends TopicModelCompanion
[PLDAModelParams,CVB0PLDA,SoftAssignmentModelState,
 LabeledLDADocumentParams,CVB0PLDADocument,(String,Array[Array[Double]])]
with DataParallelModelCompanion
[PLDAModelParams,CVB0PLDA,SoftAssignmentModelState,
 LabeledLDADocumentParams,CVB0PLDADocument,(String,Array[Array[Double]])]
with SerializableModelCompanion
[PLDAModelParams,CVB0PLDA,SoftAssignmentModelState] {

  override def name = "CVB0PLDA v0 [Stanford Topic Modeling Toolbox]";

  override def apply(mp : PLDAModelParams) =
    new CVB0PLDA(mp);

  override def createDatum(model : CVB0PLDA, mp : LabeledLDADocumentParams) =
    CVB0PLDADocument(mp.id, labels = mp.labels, terms = mp.terms, model = model);

  override def doLearn(model : CVB0PLDA, data : Iterable[CVB0PLDADocument]) = {
    data.foreach(doc => model.doAssignments(doc, true));
    model.countTopicTerm.foreach(_ := 0);
    model.countTopic := 0;
    data.foreach(doc => model.doCounts(doc));
  }

  override def doCollect(a : SoftAssignmentModelState, b : SoftAssignmentModelState) = {
    require(a.numTopics == b.numTopics);
    require(a.numTerms == b.numTerms);

    // start adding each corresponding countTopicTerm
    val futures = (0 until a.numTopics).map(topic =>
      AsyncMath.addInto(a.countTopicTerm(topic),b.countTopicTerm(topic)));
    
    // add and wait for countTopic
    AsyncMath.addInto(a.countTopic, b.countTopic)();
    
    // wait for remaining countTopicTerm
    futures.map(_());
    
    // return a
    a;
    
    // a.countTopicTerm :+= b.countTopicTerm;
    // a.countTopic :+= b.countTopic;
    // a;
  }

  /**
   * Function that returns the id and the per-document labels, as well
   * as the per-co-indexed-label's probability.
   */
  val thetaFn : (CVB0PLDADocument => (String,Option[(Array[Int],Array[Double])])) = {
    (doc : CVB0PLDADocument) => {
      if (doc.termTheta.length == 0) {
        (doc.id, None)
      } else {
        val avg = new Array[Double](doc.topics.length);
        { var tI = 0;
          while (tI < doc.termTheta.length) {
            avg :+= doc.termTheta(tI);
            tI += 1;
          }
        }
        avg :/= doc.termTheta.length;
        (doc.id, Some(doc.topics, avg));
      }
    }
  }
}
