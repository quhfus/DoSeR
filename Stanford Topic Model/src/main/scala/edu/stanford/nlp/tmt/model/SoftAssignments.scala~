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
 * Distributions as soft assignment model state.
 *
 * @author dramage
 */
@serializable @SerialVersionUID(1)
case class SoftAssignmentModelState
(countTopicTerm : Array[Array[Double]], countTopic : Array[Double]) {
  def numTopics = countTopic.length;
  def numTerms = countTopicTerm(0).length;
}

object SoftAssignmentModelState {
  implicit object FileReadWritable extends FileSerialization.ReadWritable[SoftAssignmentModelState] {
    override def read(path : File) = {
      val countTopicTerm = CSVFile(path, "topic-term-distributions.csv.gz").read[Array[Array[Double]]];
      val countTopic = countTopicTerm.map(_.sum);

      SoftAssignmentModelState(countTopicTerm, countTopic);
    }

    override def write(path : File, state : SoftAssignmentModelState) = {
      path.mkdirs;
      CSVFile(path, "topic-term-distributions.csv.gz").write(state.countTopicTerm);
    }
  }
}

/**
 * Document with per-term distribution over topics.  The assignments in
 * termTheta and the totals in theta must be kept in sync by the caller.
 * Duplicate observations are collapsed into a single termIndex with termCount.
 *
 * @author dramage
 */
trait SoftAssignmentDocument extends Stateful[(String,Array[Array[Double]])] {
  val id : String;
  val observed : Array[Int];
  val termIndex : Array[Int];
  val termCount : Array[Short];
  val termTheta : Array[Array[Double]];
  val theta     : Array[Double];

  checkrep();

  override def state =
    (id, termTheta);

  override def state_=(state : (String,Array[Array[Double]])) = {
    if (id != state._1)
      throw new IllegalArgumentException("Could not restore state for "+id+": got state for "+state._1);

    termTheta := state._2;
    theta := 0;
    for (tI <- 0 until termIndex.length) {
      theta :+= termCount(tI).toInt :* termTheta(tI);
    }

    checkrep();
  }

  final def assigned =
    (for (term <- observed) yield termTheta(termIndex.indexOf(term))).toArray;

  /** Throws a RuntimeException if the rep invariants are broken. */
  def checkrep() {
    require(termIndex.length == termCount.length);
    require(termCount.length == termTheta.length);

    val counts = new Array[Double](theta.length);
    var tI = 0;
    while (tI < termIndex.length) {
      require(termTheta(tI).length == theta.length, "bad length");
      require(termIndex(tI) >= 0, "bad term index");
      require(termCount(tI) > 0, "bad term count");

      var zI = 0;
      while (zI < termTheta(tI).length) {
        require(termTheta(tI)(zI) >= 0, "bad assignment");
        counts(zI) += termCount(tI) * termTheta(tI)(zI);
        zI += 1;
      }
      tI += 1;
    }

    var zI = 0;
    while (zI < theta.length) {
      require(math.abs(counts(zI) - theta(zI)) < 1e-8,
              "totals out of sync with observations");
      zI += 1;
    }
  }
}

object SoftAssignmentDocument {
  /**
   * Creates a termIndex and termCount array by tallying how often
   * each distinct term id occurs in the given set of observed terms.
   * If any term occurs more than Short.MaxValue times, all counts
   * are downscaled by (Short.MaxValue - 1.0) / maxTermCount, rounding up to
   * the nearest whole Short.
   */
  def createTermIndexAndCountFromObserved(observed : Array[Int]) : (Array[Int],Array[Short]) = {
    val sa = new scalala.collection.sparse.SparseArray[Int](observed.max + 1);
    observed.foreach(term => sa(term) += 1);
    
    // if any given term occurs more than Short.MaxValue, downscale all counts
    val termIndex = sa.indexArray;
    val termCount = {
      var maxTermCount = 0;
      sa.foreachActiveValue(v => if (v > maxTermCount) maxTermCount = v);
      if (maxTermCount > Short.MaxValue) {
        val scale = (Short.MaxValue - 1.0) / maxTermCount;
        Array.tabulate(sa.activeLength)(i => math.ceil(sa.valueAt(i) * scale).toShort);
      } else {
        Array.tabulate(sa.activeLength)(i => sa.valueAt(i).toShort);
      }
    }
    
    (termIndex, termCount);
  }
}

object SoftAssignmentDocumentState {
  val averageFn = average _;

  def average(state : (String,Array[Array[Double]])) : (String,Option[Array[Double]]) = {
    if (state._2.length == 0) {
      (state._1, None);
    } else {
      val rv = new Array[Double](state._2(0).length);
      var i = 0;
      while (i < state._2.length) {
        rv :+= state._2(i);
        i += 1;
      }
      rv :/= i;
      (state._1, Some(rv));
    }
  }
}

/**
 * Soft assignments-based model keeps distributions for each word observation.
 * Used, e.g., as the basis of the CVB0 model families.
 *
 * @author dramage
 */
trait SoftAssignmentModel[ModelParams,DocParams,Doc<:Stateful[(String,Array[Array[Double]])]]
extends TopicModel[ModelParams,SoftAssignmentModelState,DocParams,Doc,(String,Array[Array[Double]])]
with ClosedTopicSet with DirichletTermSmoothing {

  /** How many times each term is seen in each topic. */
  val countTopicTerm = Array.tabulate(numTopics)(z => new Array[Double](numTerms));

  /** How many times each topic is seen overall. */
  val countTopic = new Array[Double](numTopics);

  override def reset() = {
    countTopicTerm.foreach(_ := 0);
    countTopic := 0;
  }

  override def state =
    SoftAssignmentModelState(countTopicTerm, countTopic);

  override def state_=(state : SoftAssignmentModelState) = {
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

    var z = 0;
    while (z < numTopics) {
      require(countTopicTerm(z).length == numTerms, "Counts out of sync with numTerms");
      var i = 0;
      var s = 0.0;
      while (i < numTerms) {
        s += countTopicTerm(z)(i);
        if (countTopicTerm(z)(i) < 0) {
          throw new IllegalArgumentException(
                "countTopicTerm("+z+")("+i+") == "+countTopicTerm(z)(i)+" < 0");
        }
        i += 1;
      }

      if (math.abs(s - countTopic(z)) > 1e-6) {
        throw new IllegalArgumentException("countTopicTerm "+z+" sum mismatch: " + countTopicTerm(z).sum + " actual versus " + countTopic(z) + " expected.");
      }

      z += 1;
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
