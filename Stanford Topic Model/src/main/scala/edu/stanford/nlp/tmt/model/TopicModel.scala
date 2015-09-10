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

import scalala.operators.Implicits._;

import scalanlp.serialization.FileSerialization;
import scalanlp.text.tokenize.Tokenizer;
import scalanlp.util.Index;

import scalanlp.pipes.Pipes.global._;

import learn.{Stateful,RepCheck,ModelCompanion,SerializableModelCompanion};

/**
 * Implementation trait for topic models.  Here, we define a topic
 * model to be any model with topics defined to be distributions
 * over words and per-document per-word distributions over topic
 * (in either a training or inference set).
 *
 * @param Docs Expected document collection.
 * @param Params Extra parameters expected by Learner and Inferer.
 * @param Learner Supports learning once bound to a dataset.
 * @param Inferer Supports inference once bound to a dataset.
 *
 * @author dramage
 */
trait TopicModel[ModelParams,ModelState,DocParams,Doc<:Stateful[DocState],DocState]
extends Stateful[ModelState] with RepCheck {

  /** Where log messages go.  Defaults to System.err.println. */
  val log : (String=>Unit) = System.err.println;

  /** The number of terms in the model. */
  val numTerms : Int;

  private var _termIndex : Option[Index[String]] = None;

  final protected def termIndex_=(index : Option[Index[String]]) =
    this._termIndex = index;

  /** The term index describing which terms are in the model. */
  final def termIndex : Option[Index[String]] = _termIndex;

  private var _tokenizer : Option[Tokenizer] = None;

  final protected def tokenizer_=(tokenizer : Option[Tokenizer]) =
    this._tokenizer = tokenizer;

  /** The tokenizer used to break input documents into terms. */
  final def tokenizer : Option[Tokenizer] = _tokenizer;

  registerCheck(() => {
    require(!termIndex.isDefined || termIndex.get.size == numTerms,
            "termIndex with "+termIndex.get.size+" terms doesn't match expected size "+numTerms);
  });

  /** The parameters used to create this model. */
  def params : ModelParams;

  /** Creates a document from the given document parameters. */
  def create(docParams : DocParams) : Doc;

  /**
   * Tokenizes the given input string using our stored tokenizer and term
   * index, if available.  Otherwise, throws an IllegalArgumentException.
   */
  protected def tokenize(document : String) = {
    require(tokenizer.isDefined, "Tokenizer must be defined to tokenize new documents.");
    require(termIndex.isDefined, "Term index must be defined to tokenize new documents.");
    tokenizer.get.apply(document).map(termIndex.get.apply).filter(_ >= 0);
  }
}

/**
 * ModelCompanion implementation for topic models.  Delegates to calls
 * on the topic model where possible.
 *
 * @author dramage
 */
trait TopicModelCompanion
[ModelParams,Model<:TopicModel[ModelParams,ModelState,DocParams,Doc,DocState],ModelState,
 DocParams,Doc<:Stateful[DocState],DocState]
extends ModelCompanion[ModelParams,Model,ModelState,DocParams,Doc,DocState]
with SerializableModelCompanion[ModelParams,Model,ModelState] {
  def apply(mp : ModelParams) : Model;

  override def createModel(mp : ModelParams) =
    apply(mp);

  override def clearModel(model : Model) =
    model.reset();

  override def createDatum(model : Model, dp : DocParams) =
    model.create(dp);

  override def clearDatum(doc : Doc) =
    doc.reset();

  override def getModelParams(model : Model) =
    model.params;

  override def getModelState(model : Model) =
    model.state;

  override def useModelState(model : Model, state : ModelState) =
    model.state = state;

  override def getDatumState(doc : Doc) =
    doc.state;

  /** Sets the state of the document. */
  override def useDatumState(doc : Doc, state : DocState) =
    doc.state = state;
}

/**
 * A parametric topic model with a fixed number of (named) topics.
 *
 * @author dramage
 */
trait ClosedTopicSet {
  this : TopicModel[_,_,_,_,_] =>

  /** The number of topics in the model. */
  val numTopics : Int;

  /** The term index describing which terms are in the model. */
  var topicIndex : Option[Index[String]] = None;

  /** Gets the name for this topic. */
  def topicName(topic : Int) : String = topicIndex match {
    case Some(idx) => idx.get(topic);
    case None => "Topic "+("0"*(numTopics.toString.length - topic.toString.length))+topic;
  }

  //
  // Model Queries
  //

  /** Returns the probability of the given term in the given topic. */
  def pTopicTerm(topic : Int, term : Int) : Double;

  /** Returns the probability of the given term in the given topic. */
  def pTopicTerm(topic : String, term : String) : Double =
    pTopicTerm(topicIndex.get.indexOf(topic), termIndex.get.indexOf(term));

  /**
   * Returns the distribution over terms for the given topic.  The return
   * value of this method is assumed to have already incorporated the
   * corresponding getTermSmoothing to the appropriate extent.
   */
  def getTopicTermDistribution(topic : Int) : Array[Double] =
    Array.tabulate(numTerms)(term => pTopicTerm(topic, term));

  /**
   * Returns the distribution over terms for the given topic.  The return
   * value of this method is assumed to have already incorporated the
   * corresponding getTermSmoothing to the appropriate extent.
   */
  final def getTopicTermDistribution(topic : String) : Array[Double] =
    getTopicTermDistribution(topicIndex.get.indexOf(topic));

  registerCheck(() => {
    require(!topicIndex.isDefined || topicIndex.get.size == numTopics,
            "topicIndex with "+topicIndex.get.size+" topics doesn't match expected size "+numTopics);
  });
}

/**
 * Used add-k asymmetric dirichlet term smoothing.
 *
 * @author dramage
 */
trait DirichletTermSmoothing {
  this : TopicModel[_,_,_,_,_] =>

  private var _termSmoothing : Array[Double] = null;
  private var _termSmoothDenom : Double = Double.NaN;

  protected def termSmoothing_=(smoothing : Array[Double]) = {
    _termSmoothing = smoothing;
    _termSmoothDenom = smoothing.sum;
  }

  /** Add-k prior counts for each term (eta in the model formulation). */
  final def termSmoothing : Array[Double] =
    if (_termSmoothing == null) throw new ModelException("termSmoothing not defined") else _termSmoothing;

  protected def termSmoothDenom =
    _termSmoothDenom;

  registerCheck(() => {
    require(termSmoothing.size == numTerms,
            "TermSmoothing and TermIndex must have same size.");
    require(termSmoothing.forall(_ > 0),
            "TermSmoothing must always be positive");
  });
}

/**
 * Used add-k asymmetric dirichlet topic smoothing.
 *
 * @author dramage
 */
trait DirichletTopicSmoothing {
  this : TopicModel[_,_,_,_,_] with ClosedTopicSet =>

  private var _topicSmoothing : Array[Double] = null;

  protected def topicSmoothing_=(smoothing : Array[Double]) =
    _topicSmoothing = smoothing;

  /** Prior counts for each topic (alpha in the model formulation). */
  final def topicSmoothing : Array[Double] =
    if (_topicSmoothing == null) throw new ModelException("topicSmoothing not defined") else _topicSmoothing;

  registerCheck(() => {
    require(topicSmoothing.size == numTopics,
            "TopicSmoothing and TopicIndex must have same size.");
    require(topicSmoothing.forall(_ > 0),
            "TopicSmoothing must always be positive");
  });
}

class TrainingException(msg : String) extends RuntimeException(msg);

class ModelException(msg : String) extends RuntimeException(msg);

class LoadException(msg : String) extends RuntimeException(msg);

class SaveException(msg : String) extends RuntimeException(msg);
