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

import scalala.operators.Implicits._;
import scalala.library.random.MersenneTwisterFast;

import scalanlp.pipes.Pipes.global._;
import scalanlp.stage._;
import scalanlp.util.TopK;

import data.concurrent.AsyncMath;
import learn.{SerializableModelCompanion,DataParallelModelCompanion};


/**
 * CVB0LDA document.  The assignments in assigned and the totals in
 * theta must be kept in sync by the caller.
 *
 * @param termIndex Term types that appear in the document
 * @param termCount How often each term type appears in the document
 * @param termTheta Distribution over topics for each term type
 * @param theta Document level distribution over topics
 *
 * @author dramage
 */
class CVB0LDADocument(
  override val id : String,
  override val observed : Array[Int],
  override val termIndex : Array[Int],
  override val termCount : Array[Short],
  override val termTheta : Array[Array[Double]],
  override val theta     : Array[Double],
  val model : CVB0LDA
) extends LDADocument[(String,Array[Array[Double]])] with SoftAssignmentDocument {

  /** Clear all assignments */
  override def reset = {
    termTheta.foreach(d => d := model.topicSmoothing);
    theta := model.topicSmoothing :* termCount.sum.toDouble;
  }

  override def signature =
    theta :/ theta.sum;
}

object CVB0LDADocument {
  def apply(id : String, observed : Array[Int], model : CVB0LDA) = {
    val sa = new scalala.collection.sparse.SparseArray[Int](model.numTerms);
    var i = 0; while (i < observed.length) { sa(observed(i)) += 1; i += 1; }
    val keys = sa.indexArray;
    val counts = sa.valueArray.map(_.toShort);
    new CVB0LDADocument(id, observed, keys, counts,
      Array.tabulate(keys.size)(t => new Array[Double](model.numTopics)),
      new Array[Double](model.numTopics), model);
  }
}


/**
 * CVB0 learning and inference model for vanilla LDA.  This algorithm is like
 * the collapsed Gibbs sampler implemented in GibbsLDA except that instead of
 * sampling a hard topic assignment for each z as it iterates through the data,
 * the model keeps the soft assignment distribution.  Consequently, the
 * algorithm converges in fewer iterations (and it is easier to determine
 * convergence) but requires more memory during training.
 *
 * See "On Smoothing and Inference for Topic Models" in UAI 2009 for more on
 * CVB0.
 *
 * @author dramage
 */
class CVB0LDA
(override val params : LDAModelParams,
 val seed : Long = 0l,
 override val log : (String=>Unit) = System.err.println)
extends LDA[SoftAssignmentModelState,CVB0LDADocument,(String,Array[Array[Double]])]
with SoftAssignmentModel[LDAModelParams,LDADocumentParams,CVB0LDADocument] {

  checkrep();

  override def create(dp : LDADocumentParams) =
    CVB0LDADocument(dp.id, dp.terms, this);

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
   * Returns an array of per-topic probabilities.  Loops while the largest
   * difference between iterations in probabilities for any given topic is
   * greater than delta (default 1e-5).
   */
  def infer(doc : CVB0LDADocument, delta : Double) : Array[Double] = {
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

    last;
  }

  /**
   * Returns an array of per-topic probabilities.  Loops while the largest
   * difference between iterations in probabilities for any given topic is
   * greater than delta (default 1e-5).
   */
  override def infer(doc : CVB0LDADocument) : Array[Double] =
    infer(doc, 1e-5);
  
  def doAssignments(doc : CVB0LDADocument, learn : Boolean) = {
    { // compute new assignments
      var tI = 0;
      while (tI < doc.termIndex.length) {
        val t = doc.termIndex(tI);
        var norm  = 0.0;

        {
          var z = 0;
          while (z < numTopics) {
            val oldThetaAssignment = doc.termTheta(tI)(z);
            val oldCountAssignment = if (learn) oldThetaAssignment else 0.0;

            val newAssignment =
              ((countTopicTerm(z)(t) - oldCountAssignment + termSmoothing(t))
               / (countTopic(z) - oldCountAssignment + termSmoothDenom)
               * (doc.theta(z) - oldThetaAssignment + topicSmoothing(z)));

//            if (newAssignment < 0) {
//              println((z,t));
//              println((countTopicTerm(z)(t), oldCountAssignment, termSmoothing(t), countTopic(z), oldCountAssignment, termSmoothDenom, doc.theta(z), oldThetaAssignment, topicSmoothing(z)));
//              throw new AssertionError("newAssignment = " + newAssignment + " < 0");
//            }

            doc.termTheta(tI)(z) = newAssignment;
            norm += newAssignment;

            z += 1;
          }
        }

        {
          var z = 0;
          while (z < numTopics) {
            doc.termTheta(tI)(z) /= norm;
            z += 1;
          }
        }

        tI += 1;
      }
    }

    { // update topic totals
      java.util.Arrays.fill(doc.theta, 0.0);
      var tI = 0;
      while (tI < doc.termIndex.length) {
        var z = 0;
        while (z < numTopics) {
          doc.theta(z) += doc.termCount(tI) * doc.termTheta(tI)(z);
          z += 1;
        }
        tI += 1;
      }
    }
  }

  def doCounts(doc : CVB0LDADocument) {
    var z = 0;
    while (z < numTopics) {
      var tI = 0;
      while (tI < doc.termIndex.length) {
        val t = doc.termIndex(tI);
        countTopicTerm(z)(t) += doc.termCount(tI) * doc.termTheta(tI)(z);
        tI += 1;
      }
      countTopic(z) += doc.theta(z);
      z += 1;
    }
  }
}

@serializable
object CVB0LDA
extends LDACompanion[CVB0LDA,SoftAssignmentModelState,CVB0LDADocument,(String,Array[Array[Double]])]
with DataParallelModelCompanion[LDAModelParams,CVB0LDA,SoftAssignmentModelState,LDADocumentParams,CVB0LDADocument,(String,Array[Array[Double]])] {

  override def name = "CVB0LDA v0 [Stanford Topic Modeling Toolbox]";

  override def apply(mp : LDAModelParams) : CVB0LDA =
    new CVB0LDA(mp);

  override def doLearn(model : CVB0LDA, data : Iterable[CVB0LDADocument]) = {
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
    AsyncMath.addInto(a.countTopic,b.countTopic)();
    
    // wait for remaining countTopicTerm
    futures.map(_());
    
    // return a
    a;
    
    // a.countTopicTerm :+= b.countTopicTerm;
    // a.countTopic :+= b.countTopic;
    // a;
  }

  //
  // slicing
  //

  import scalala.collection.sparse.SparseArray;

  def createTopicTermMatrix(doc : CVB0LDADocument) = {
    val rv = Array.tabulate(doc.model.numTopics)(z => new SparseArray[Double](doc.model.numTerms));
    var i = 0;
    while (i < doc.termIndex.length) {
      val term = doc.termIndex(i);
      val count = doc.termCount(i);
      var z = 0;
      while (z < doc.termTheta.length) {
        rv(z)(term) += count * doc.termTheta(i)(z);
        z += 1;
      }
      i += 1;
    }
    rv;
  }

  def addTopicTermMatrixInto(a : Array[SparseArray[Double]], b : Array[SparseArray[Double]]) = {
    require(a.length == b.length);
    var i = 0;
    while (i < a.length) {
      a(i) :+= b(i);
      i += 1;
    }
    a;
  }
}
