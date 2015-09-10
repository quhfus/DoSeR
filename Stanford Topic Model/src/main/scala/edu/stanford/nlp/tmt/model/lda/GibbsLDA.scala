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

import scalala.library.random.MersenneTwisterFast;
import scalala.operators.Implicits._;

import learn.{GibbsInferParams,GibbsInfer};

import scalanlp.util.TopK;

/**
 * Holds a bag of observations and an assignment for each observation.
 *
 * @param observed observed terms
 * @param assigned assigned topic for each term
 * @param theta count of how many times each topic has been assigned
 *
 * @author dramage
 */
class GibbsLDADocument
(override val id : String,
 override val observed : Array[Int],
 override val assigned : Array[Int],
 override val theta : Array[Int])
extends LDADocument[(String,Array[Int])]
   with HardAssignmentDocument {

  override def signature =
    theta :/ theta.sum.toDouble;
}

object GibbsLDADocument {
  def apply(id : String, terms : Array[Int], numTerms : Int, numTopics : Int) =
    new GibbsLDADocument(id, terms, Array.fill(terms.length)(-1), new Array[Int](numTopics));
}

/**
 * Collapsed Gibbs sampler for LDA learning and inference.  This
 * class is not threadsafe for learning.  However, it is threadsafe for
 * inerence, but no guarantees are provided about repeatability
 * in a threaded environment if the number of threads is different between
 * runs.  This is because each thread is given its own random number
 * generator to avoid synchronization overhead, so the sequence of random
 * numbers seen on an particular document may be a function of the number
 * of threads.
 *
 * @author dramage
 */
class GibbsLDA(
 override val params : LDAModelParams,
 val seed : Long = 0l,
 val inferParams : GibbsInferParams = GibbsInferParams(),
 override val log : (String=>Unit) = System.err.println)
extends LDA[HardAssignmentModelState,GibbsLDADocument,(String,Array[Int])]
with HardAssignmentModel[LDAModelParams,LDADocumentParams,GibbsLDADocument] {

  checkrep();

  override def create(dp : LDADocumentParams) =
    GibbsLDADocument(dp.id, dp.terms, numTerms = this.numTerms, numTopics = this.numTopics);

  protected val inferSamplerTL = new ThreadLocal[GibbsLDA.InferSampler] {
    override protected def initialValue() =
      new GibbsLDA.InferSampler(
        countTopicTerm, countTopic, termSmoothing, topicSmoothing,
        new MersenneTwisterFast(seed));
  }

  /** Gets a thread-local inference sampler. */
  def inferSampler = inferSamplerTL.get;

  val learnSampler = new GibbsLDA.LearnSampler(
    countTopicTerm, countTopic, termSmoothing, topicSmoothing, new MersenneTwisterFast(seed));

  def sampleInfer(doc : GibbsLDADocument) =
    inferSampler.sample(doc);

  def sampleLearn(doc : GibbsLDADocument) =
    learnSampler.sample(doc);

  def infer(doc : GibbsLDADocument) = {
    GibbsInfer(
      result = new Array[Double](numTopics),
      reset = () => doc.reset,
      sample = () => sampleInfer(doc),
      state = () => doc.signature,
      params = inferParams
    );
  }
}


/**
 * Static helper methods for interacting with the standard GibbsLDA
 * implementation set, with limited support for customization.
 *
 * @author dramage
 */
object GibbsLDA extends LDACompanion
[GibbsLDA,HardAssignmentModelState,
 GibbsLDADocument,(String,Array[Int])] {

  override def name = "GibbsLDA v0 [Stanford Topic Modeling Toolbox]";

  override def apply(mp : LDAModelParams) : GibbsLDA =
    new GibbsLDA(mp);

  override def doLearn(model : GibbsLDA, data : Iterable[GibbsLDADocument]) = {
    // model.checkrep();
    data.foreach(model.sampleLearn);
  }

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
  extends HardAssignmentGibbsSampler[GibbsLDADocument] {

    val termSmoothDenom = termSmoothing.sum;

    val numTopics = topicSmoothing.length;
    val numTerms = termSmoothing.length;

    /**
     * Temporary array populated by sampling (not safe to use the same
     * sampler in more than one thread).
     */
    val dist = new Array[Double](numTopics);

    /** Creates and samples from a probability distribution over topics for a given term. */
    override def sampleZ(doc : GibbsLDADocument, term : Int) : Int = {
      val termSmooth = termSmoothing(term);
      var sumP = 0.0;
      var z = 0;

      while (z < numTopics) {
        // P(z | rest) is proportional to probability of topic in document
        // (for which no normalization is needed) times probability of word
        // in topic (for which normalization is needed).
        val pZ = (doc.theta(z)+topicSmoothing(z)) *
                 (countTopicTerm(z)(term)+termSmooth) /
                 (countTopic(z)+termSmoothDenom);

//        if (pZ <= 0) {
//          println((doc.theta(z),topicSmoothing(z),countTopicTerm(z)(term),termSmooth,countTopic(z),termSmoothDenom));
//          throw new AssertionError("pZ < 0");
//        }

        dist(z) = pZ;
        sumP += pZ;
        z += 1;
      }

//      if (sumP <= 0) {
//        throw new AssertionError("Invalid probability mass: \n"+dist.mkString("\n"));
//      }

      return GibbsModelMath.sampleFromIllDistribution(dist, sumP, dist.length, random);
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
    override def incModelTopicTerm(doc : GibbsLDADocument, topic : Int, term : Int) { /* do nothing */ }
    override def decModelTopicTerm(doc : GibbsLDADocument, topic : Int, term : Int) { /* do nothing */ }
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
    override def incModelTopicTerm(doc : GibbsLDADocument, topic : Int, term : Int) {
      countTopicTerm(topic)(term) += 1;
      countTopic(topic) += 1;
    }
    override def decModelTopicTerm(doc : GibbsLDADocument, topic : Int, term : Int) {
      countTopicTerm(topic)(term) -= 1;
      countTopic(topic) -= 1;
    }
  }
}

/**
 * LDA Model utilities.
 *
 * @author dramage
 */
object GibbsModelMath {
  /**
   * Returns a sample from the given ill-conditioned probability distribution
   * (doesn't sum to 1) whose sum has been precomputed.  Returns a value from
   * between 0 (inclusive) to end (exclusive)
   */
  @inline final def sampleFromIllDistribution
  (values : Array[Double], sum : Double, end : Int, random : MersenneTwisterFast) : Int = {
    var i = end;
    var threshold = random.nextDouble * sum;
    while (i > 0 && threshold > 0) {
      i -= 1;
      threshold -= values(i);
    }
    return i;
  }
}
