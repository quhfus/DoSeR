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

import scalala.collection.sparse.SparseArray;
import scalala.operators.Implicits._;
import scalala.library.random.MersenneTwisterFast;

import scalanlp.util.TopK;

import data.concurrent.AsyncMath;
import learn.{SerializableModelCompanion,DataParallelModelCompanion};


/**
 * CVB0LabeledLDA document.  The assignments in assigned and the totals in
 * theta must be kept in sync by the caller.
 *
 * @param id Identifier for this document
 * @param observed List of terms, in order
 * @param labels Set of active labels on this document.  Should not
 *   contain duplicates.
 * @param termIndex Term types that appear in the document
 * @param termCount How often each term type appears in the document
 * @param termTheta Distribution over topics for each term type
 * @param theta Document level distribution over topics
 * @param model Model associated with this document
 *
 * @author dramage
 */
class CVB0LabeledLDADocument(
  override val id : String,
  val labels : Array[Int],
  override val observed : Array[Int],
  override val termIndex : Array[Int],
  override val termCount : Array[Short],
  override val termTheta : Array[Array[Double]],
  override val theta : Array[Double],
  val model : CVB0LabeledLDA)
extends LabeledLDADocument[(String,Array[Array[Double]])] with SoftAssignmentDocument {

  override def checkrep() {
    super.checkrep();
    require(theta.length == labels.length);
    var tI = 0;
    while (tI < termTheta.length) {
      require(termTheta(tI).length == labels.length);
      tI += 1;
    }
  }

  /** Clear all assignments */
  override def reset = {
    theta := 0;

    var wI = 0;
    while (wI < termTheta.length) {
      var zI = 0;
      while (zI < termTheta(wI).length) {
        var z = labels(zI);
        termTheta(wI)(zI) = model.topicSmoothing(z);
        theta(zI) += termTheta(wI)(zI);
        zI += 1;
      }
      wI += 1;
    }
  }

  /** Returns the per-label probability. */
  override def signature : SparseArray[Double] = {
    val avg = new SparseArray[Double](model.numTopics,labels.length);
    var tI = 0;
    while (tI < termTheta.length) {
      var zI = 0;
      while (zI < labels.length) {
        avg(labels(zI)) += termTheta(tI)(zI);
        zI += 1;
      }
      tI += 1;
    }
    avg :/= termTheta.length;
    avg;
  }
}


object CVB0LabeledLDADocument {
  /**
   * Creates a new CVB0LabeledLDADocument from the given array of labels and
   * terms.  If any term occurs more than Short.MaxValue times, all counts
   * are downscaled by (Short.MaxValue - 1.0) / maxTermCount, rounding up to
   * the nearest whole Short.
   */
  def apply(id : String, labels : Array[Int], terms : Array[Int], model : CVB0LabeledLDA) = {
    var i = 0;
    while (i < terms.length) {
      if (!(terms(i) >= 0 && terms(i) < model.numTerms)) {
        throw new IllegalArgumentException("Observation "+i+" ("+terms(i)+") out of bounds ["+0+","+model.numTerms+")");
      }
      i += 1;
    }
    
    val (termIndex, termCount) = SoftAssignmentDocument.createTermIndexAndCountFromObserved(terms);
    new CVB0LabeledLDADocument(
      id = id, labels = labels, observed = terms, termIndex = termIndex, termCount = termCount,
      termTheta = Array.tabulate(termIndex.size)(t => new Array[Double](labels.length)),
      theta = new Array[Double](labels.length), model = model);
  }
}

/**
 * CVB0 learning and inference for LabeledLDA.
 *
 * @author dramage
 */
class CVB0LabeledLDA
(override val params : LabeledLDAModelParams,
 val seed : Long = 0l,
 override val log : (String=>Unit) = System.err.println)
extends LabeledLDA[SoftAssignmentModelState,CVB0LabeledLDADocument,(String,Array[Array[Double]])]
with SoftAssignmentModel[LabeledLDAModelParams,LabeledLDADocumentParams,CVB0LabeledLDADocument] {

  checkrep();

  override def create(dp : LabeledLDADocumentParams) =
    CVB0LabeledLDADocument(dp.id, labels = dp.labels, terms = dp.terms, model = this);

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
  def infer(doc : CVB0LabeledLDADocument, delta : Double) : SparseArray[Double] = {
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
  override def infer(doc : CVB0LabeledLDADocument) : SparseArray[Double] =
    infer(doc, 1e-5);

  def doAssignments(doc : CVB0LabeledLDADocument, learn : Boolean = true) {
    { // compute new assignments
      var tI = 0;
      while (tI < doc.termIndex.length) {
        val t = doc.termIndex(tI);
        var norm  = 0.0;

        {
          var zI = 0;
          while (zI < doc.labels.length) {
            val z = doc.labels(zI);
            val oldThetaAssignment = doc.termTheta(tI)(zI);
            val oldCountAssignment = if (learn) oldThetaAssignment else 0.0;
            val newAssignment =
              ((countTopicTerm(z)(t) - oldCountAssignment + termSmoothing(t))
               / (countTopic(z) - oldCountAssignment + termSmoothDenom)
               * (doc.theta(zI) - oldThetaAssignment + topicSmoothing(z)));
            doc.termTheta(tI)(zI) = newAssignment;
            norm += newAssignment;

            zI += 1;
          }
        }

        {
          var zI = 0;
          while (zI < doc.labels.length) {
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
        while (zI < doc.labels.length) {
          doc.theta(zI) += doc.termCount(tI) * doc.termTheta(tI)(zI);
          zI += 1;
        }
        tI += 1;
      }
    }
  }

  def doCounts(doc : CVB0LabeledLDADocument) {
    var zI = 0;
    while (zI < doc.labels.length) {
      val z = doc.labels(zI);
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
        numTopics = params.numLabels,
        numTerms = params.numTerms,
        topicSmoothing = params.labelSmoothing,
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
object CVB0LabeledLDA extends LabeledLDACompanion
[CVB0LabeledLDA,SoftAssignmentModelState,
 CVB0LabeledLDADocument,(String,Array[Array[Double]])]
with DataParallelModelCompanion
[LabeledLDAModelParams,CVB0LabeledLDA,SoftAssignmentModelState,
 LabeledLDADocumentParams,CVB0LabeledLDADocument,(String,Array[Array[Double]])] {

  override def name = "CVB0LabeledLDA v0 [Stanford Topic Modeling Toolbox]";

  override def apply(mp : LabeledLDAModelParams) =
    new CVB0LabeledLDA(mp);

  override def createDatum(model : CVB0LabeledLDA, mp : LabeledLDADocumentParams) =
    CVB0LabeledLDADocument(mp.id, labels = mp.labels, terms = mp.terms, model = model);

  override def doLearn(model : CVB0LabeledLDA, data : Iterable[CVB0LabeledLDADocument]) = {
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
}

