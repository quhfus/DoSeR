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

import java.io.File;

import scalanlp.pipes.Pipes.global._;
import scalanlp.io.CSVFile;
import scalanlp.serialization.FileSerialization;
import scalanlp.util.TopK;

import scalala.operators.Implicits._;

import learn.Stateful;


/**
 * Counts as hard assignment model state.
 *
 * @author dramage
 */
@serializable @SerialVersionUID(1)
case class HardAssignmentModelState
(countTopicTerm : Array[Array[Int]], countTopic : Array[Int]);

object HardAssignmentModelState {
  implicit object FileReadWritable extends FileSerialization.ReadWritable[HardAssignmentModelState] {
    override def read(path : File) = {
      val countTopicTerm = CSVFile(path, "topic-term-distributions.csv.gz").read[Array[Array[Int]]];
      val countTopic = countTopicTerm.map(_.sum);

      HardAssignmentModelState(countTopicTerm, countTopic);
    }

    override def write(path : File, state : HardAssignmentModelState) = {
      path.mkdirs;
      CSVFile(path, "topic-term-distributions.csv.gz").write(state.countTopicTerm);
    }
  }
}

/**
 * A document with a single hard assignment (Int) for each observation
 * in a set of observations. This is used as the base trait of, e.g.
 * GibbsLDA.Document.
 *
 * @author dramage
 */
trait HardAssignmentDocument extends Stateful[(String,Array[Int])] {
  val id : String;
  val observed : Array[Int];
  val assigned : Array[Int];
  val theta : Array[Int];

  checkrep();

  /** Clear all assignments */
  override def reset() {
    java.util.Arrays.fill(assigned, -1);
    java.util.Arrays.fill(theta, 0);
  }

  override def state =
    (id, assigned);
  
  override def state_=(state : (String,Array[Int])) = {
    if (this.id != state._1)
      throw new IllegalArgumentException("Could not restore state for "+id+": got state for "+state._1);
    this.assigned := state._2;
    this.theta := 0
    var i = 0;
    while (i < assigned.length) {
      theta(assigned(i)) += 1;
      i += 1;
    }
    checkrep();
  }

  /** Returns true if this document has assingments */
  def hasAssignments : Boolean =
    assigned.length > 0 && assigned(0) != -1;

  /** Returns true or throws a RuntimeException */
  def checkrep() {
    require(observed.length == assigned.length, "Wrong lengths");
    for (i <- 0 until observed.length) {
      if (observed(i) < 0)
        throw new IllegalArgumentException("Weird observation "+observed(i)+" at position "+i);
    }

    if (hasAssignments) {
      val counts = new Array[Int](theta.length);
      for (i <- 0 until assigned.length) {
        if (assigned(i) < 0 || assigned(i) >= theta.length)
          throw new IllegalArgumentException("Weird observation "+assigned(i)+" at position "+i);
        counts(assigned(i)) += 1;
      }
      for (i <- 0 until theta.length) {
        require(counts(i) == theta(i), "totals out of sync with observations");
      }
    }
  }
}


/**
 * Hard assignments-based model keeps int counts; base trait of Gibbs
 * sampler based topic models.
 *
 * @author dramage
 */
trait HardAssignmentModel[ModelParams,DocParams,Doc<:Stateful[(String,Array[Int])]]
extends TopicModel[ModelParams,HardAssignmentModelState,DocParams,Doc,(String,Array[Int])]
with ClosedTopicSet with DirichletTermSmoothing {

  /** How many times each term is seen in each topic. */
  val countTopicTerm = Array.tabulate(numTopics)(z => new Array[Int](numTerms));

  /** How many times each topic is seen overall. */
  val countTopic = new Array[Int](numTopics);

  override def reset() = {
    countTopicTerm.foreach(_ := 0);
    countTopic := 0;
  }

  override def state =
    HardAssignmentModelState(countTopicTerm, countTopic);

  override def state_=(state : HardAssignmentModelState) = {
    countTopicTerm := state.countTopicTerm;
    countTopic := state.countTopic;
    checkrep();
  }

  override final def pTopicTerm(topic : Int, term : Int) =
    (countTopicTerm(topic)(term) + termSmoothing(term)) /
    (countTopic(topic) + termSmoothDenom);

  /** Checks the representation invariants */
  registerCheck(() => {
    require(countTopic.length == numTopics, "Counts out of sync with numTopics");
    require(countTopicTerm.length == numTopics, "Counts out of sync with numTopics");

    for (z <- 0 until numTopics) {
      require(countTopicTerm(z).length == numTerms, "Counts out of sync with numTerms");
      require(countTopicTerm(z).forall(_ >= 0),
              "countTopicTerm "+z+" has negative entry");
      require(countTopicTerm(z).sum == countTopic(z),
              "countTopicTerm "+z+" sum mismatch: " + countTopicTerm(z).sum + " actual versus " + countTopic(z) + " expected.");
    }
  });

  /**
   * Returns human-readable summary of the current topic model.
   */
  def summary : Iterator[String] = {
    ((0 until numTopics).iterator).flatMap(topic => {
      Iterator(topicName(topic) + "\t\t" + countTopic(topic)) ++
      TopK(20,(0 until numTerms),(term : Int) => countTopicTerm(topic)(term)).iterator.map( term => {
        "\t" + termIndex.get.get(term) + "\t" + countTopicTerm(topic)(term)
      }) ++
      Iterator("\n")
    });
  }
}

/**
 * Abstract base class of a Gibbs sampler for LDA.  Not threadsafe.
 *
 * @author dramage
 */
abstract class HardAssignmentGibbsSampler[Document <: HardAssignmentDocument] {

  /** Increment the given count of topic and term in the model data structures. */
  def incModelTopicTerm(doc : Document, topic : Int, term : Int) : Unit;

  /** Decrement the given count of topic and term in the model data structures. */
  def decModelTopicTerm(doc : Document, topic : Int, term : Int) : Unit;

  /** Creates and samples from a probability distribution over topics for a given term. */
  protected def sampleZ(doc : Document, term : Int) : Int;

  /** Resamples each topic assignment in the given document. */
  def sample(doc : Document) {
    if (!doc.hasAssignments) {
      // special case sampler:  if no assignments have yet been made,
      // there are no counts to decrement before calling sampleZ.

      var i = 0;
      while (i < doc.observed.length) {
        val term = doc.observed(i);

        // sample new z from dist
        val newZ = sampleZ(doc, term);
        doc.assigned(i) = newZ;
        doc.theta(newZ) += 1;
        incModelTopicTerm(doc,newZ,term);

        i += 1;
      }
    } else {
      // regular sampler.  decrement counts, resample, then increment.

      var i = 0;
      while (i < doc.observed.length) {
        val term = doc.observed(i);

        // remove old z from dist
        val oldZ = doc.assigned(i);
        doc.theta(oldZ) -= 1;
        decModelTopicTerm(doc,oldZ,term);

        // sample new z from dist
        val newZ = sampleZ(doc, term);
        doc.assigned(i) = newZ;
        doc.theta(newZ) += 1;
        incModelTopicTerm(doc,newZ,term);

        i += 1;
      }
    }
  }
}

