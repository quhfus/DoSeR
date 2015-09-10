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
import scalala.library.random.MersenneTwisterFast;
import scalala.operators.Implicits._;

import learn.{LocalUpdateModelCompanion,SerializableModelCompanion};
import learn.{GibbsInferParams,GibbsInfer};

import scalanlp.util.TopK;

/**
 * Holds a set of active labels, a bag of observations, and an assignment
 * for each observation.
 *
 * @param observed observed terms
 * @param labels Active labels on this document
 * @param assigned assigned topic for each term
 * @param theta count of how many times each topic has been assigned
 *
 * @author dramage
 */
class GibbsLabeledLDADocument
(override val id : String,
 val labels : Array[Int],
 override val observed : Array[Int],
 override val assigned : Array[Int],
 override val theta : Array[Int],
 val model : GibbsLabeledLDA)
extends LabeledLDADocument[(String,Array[Int])] with HardAssignmentDocument {

  override def checkrep() {
    super.checkrep();
    require(theta.length == labels.length);
  }

  /** Returns the per-label probability. */
  override def signature = {
    val avg = new SparseArray[Double](model.numTopics,labels.length);
    var i = 0;
    while (i < assigned.length) {
      avg(labels(assigned(i))) += 1;
      i += 1;
    }
    avg :/= assigned.length;
    avg;
  }
}

object GibbsLabeledLDADocument {
  def apply(id : String, labels : Array[Int], terms : Array[Int], model : GibbsLabeledLDA) = {
    new GibbsLabeledLDADocument(
      id = id, labels = labels.toSet.toArray, observed = terms,
      assigned = Array.fill(terms.length)(-1),
      theta = new Array[Int](labels.length),
      model = model
    );
  }
}


/**
 * Labeled LDA with learning and inference via collapsed gibbs sampling.
 *
 * @author dramage
 */
class GibbsLabeledLDA
(override val params : LabeledLDAModelParams,
 val seed : Long = 0l,
 val inferParams : GibbsInferParams = GibbsInferParams(),
 override val log : (String=>Unit) = System.err.println)
extends LabeledLDA[HardAssignmentModelState,GibbsLabeledLDADocument,(String,Array[Int])]
with HardAssignmentModel[LabeledLDAModelParams,LabeledLDADocumentParams,GibbsLabeledLDADocument] {

  checkrep();

  override def create(dp : LabeledLDADocumentParams) =
    GibbsLabeledLDADocument(dp.id, labels = dp.labels, terms=dp.terms, model=this);

  protected val inferSamplerTL = new ThreadLocal[GibbsLabeledLDA.InferSampler] {
    override protected def initialValue() =
      new GibbsLabeledLDA.InferSampler(
        countTopicTerm, countTopic, termSmoothing, topicSmoothing,
        new MersenneTwisterFast(seed));
  }

  /** Gets a thread-local inference sampler. */
  def inferSampler = inferSamplerTL.get;

  val learnSampler = new GibbsLabeledLDA.LearnSampler(
    countTopicTerm, countTopic, termSmoothing, topicSmoothing, new MersenneTwisterFast(seed));

  def sampleInfer(doc : GibbsLabeledLDADocument) =
    inferSampler.sample(doc);

  def sampleLearn(doc : GibbsLabeledLDADocument) =
    learnSampler.sample(doc);

  override def infer(doc : GibbsLabeledLDADocument) : SparseArray[Double] = {
    GibbsInfer(
      result = new SparseArray[Double](numTopics),
      reset = () => doc.reset,
      sample = () => sampleInfer(doc),
      state = () => doc.signature,
      params = inferParams
    );
  }

  def asGibbsLDA : lda.GibbsLDA = {
    val mp = lda.LDAModelParams(
        numTopics = params.numLabels,
        numTerms = params.numTerms,
        topicSmoothing = params.labelSmoothing,
        termSmoothing = params.termSmoothing,
        termIndex = params.termIndex,
        tokenizer = params.tokenizer
      );  
    val model = new lda.GibbsLDA(
      params = mp, seed = seed, log = log);
    model.state = this.state;
    model;
  }

//  def computeLogPW(doc : GibbsLDADocument) =
//    LDAMath.computeLogPW(doc.observed.iterator, doc.theta :/ doc.theta.sum.toDouble, pTopicTerm);
}

/**
 * Support methods for GibbsLabeledLDA.
 *
 * @author dramage
 */
object GibbsLabeledLDA
extends LabeledLDACompanion
[GibbsLabeledLDA,HardAssignmentModelState,GibbsLabeledLDADocument,(String,Array[Int])] {

  override def name = "GibbsLabeledLDA v0 [Stanford Topic Modeling Toolbox]";

  override def apply(mp : LabeledLDAModelParams) : GibbsLabeledLDA =
    new GibbsLabeledLDA(mp);

  override def doLearn(model : GibbsLabeledLDA, data : Iterable[GibbsLabeledLDADocument]) = {
    // model.checkrep();
    data.foreach(model.sampleLearn);
  }

  override def createDatum(model : GibbsLabeledLDA, dp : LabeledLDADocumentParams) =
    GibbsLabeledLDADocument(dp.id, labels = dp.labels, terms = dp.terms, model = model);


  /**
   * Abstract base class of a Gibbs sampler for LDA.  Not threadsafe.
   *
   * @author dramage
   */
  abstract class Sampler
  (countTopicTerm : Array[Array[Int]],
   countTopic : Array[Int],
   termSmoothing : Array[Double],
   topicSmoothing : Array[Double],
   random : MersenneTwisterFast)
  extends HardAssignmentGibbsSampler[GibbsLabeledLDADocument] {

    val termSmoothDenom = termSmoothing.sum;

    val numTopics = topicSmoothing.length;
    val numTerms = termSmoothing.length;

    /**
     * Temporary array populated by sampling (not safe to use the same
     * sampler in more than one thread).
     */
    val dist = new Array[Double](numTopics);

    /** Creates and samples from a probability distribution over topics for a given term. */
    override def sampleZ(doc : GibbsLabeledLDADocument, term : Int) : Int = {
      val termSmooth = termSmoothing(term);
      var sumP = 0.0;
      var zI = 0;

      while (zI < doc.labels.length) {
        val z = doc.labels(zI);

        val pZ = (doc.theta(z)+topicSmoothing(z)) *
                 (countTopicTerm(z)(term)+termSmooth) /
                 (countTopic(z)+termSmoothDenom);

        dist(zI) = pZ;
        sumP = pZ;

        zI += 1;
      }

      return lda.GibbsModelMath.sampleFromIllDistribution(dist, sumP, doc.labels.length, random);
    }
  }

  /**
   * Does inference on a single document with no updates to the model.
   *
   * @author dramage
   */
  class InferSampler
  (countTopicTerm : Array[Array[Int]],
   countTopic : Array[Int],
   termSmoothing : Array[Double],
   topicSmoothing : Array[Double],
   random : MersenneTwisterFast)
  extends Sampler(countTopicTerm, countTopic, termSmoothing, topicSmoothing, random) {
    override def incModelTopicTerm(doc : GibbsLabeledLDADocument, topic : Int, term : Int) { /* do nothing */ }
    override def decModelTopicTerm(doc : GibbsLabeledLDADocument, topic : Int, term : Int) { /* do nothing */ }
  }

  /**
   * A sampler that updates the model (normal gibbs sampling).
   *
   * @author dramage
   */
  class LearnSampler
  (countTopicTerm : Array[Array[Int]],
   countTopic : Array[Int],
   termSmoothing : Array[Double],
   topicSmoothing : Array[Double],
   random : MersenneTwisterFast)
  extends Sampler(countTopicTerm, countTopic, termSmoothing, topicSmoothing, random) {
    override def incModelTopicTerm(doc : GibbsLabeledLDADocument, topic : Int, term : Int) {
      val z = doc.labels(topic);
      countTopicTerm(z)(term) += 1;
      countTopic(z) += 1;
    }
    override def decModelTopicTerm(doc : GibbsLabeledLDADocument, topic : Int, term : Int) {
      val z = doc.labels(topic);
      countTopicTerm(z)(term) -= 1;
      countTopic(z) -= 1;
    }
  }
}
