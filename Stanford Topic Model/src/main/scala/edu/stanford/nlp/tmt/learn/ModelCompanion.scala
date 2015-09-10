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
package learn;

import java.io.File;

import scalanlp.serialization.FileSerialization;

/**
 * Generic interface for trainable models with both model state and data state.
 *
 * @author dramage
 */
trait ModelCompanion[ModelParams,Model,ModelState,DatumParams,Datum,DatumState] {
  def name : String;

  /** Creates a new model from the given model parameters. */
  def createModel(mp : ModelParams) : Model;

  /** Creates a new datum for processing with the given model. */
  def createDatum(model : Model, dp : DatumParams) : Datum;

  /** Clears the state of the model. */
  def clearModel(model : Model);

  /** Clears the state of the datum. */
  def clearDatum(datum : Datum);

  /** Get model parameters used to construct a model like the given. */
  def getModelParams(model : Model) : ModelParams;

  /** Gets the state of the model. */
  def getModelState(model : Model) : ModelState;

  /** Uses the given model state. */
  def useModelState(model : Model, state : ModelState);

  /** Gets the state of the datum. */
  def getDatumState(datum : Datum) : DatumState;

  /** Uses the given datum state. */
  def useDatumState(datum : Datum, state : DatumState);

//  /** List of descriptions of this model. */
//  private val _descriptions = scala.collection.mutable.Map[String,()=>Iterator[String]]();
//
//  /** Registers a description of this model. */
//  protected def registerDescription(name : String, description : =>Iterator[String]) =
//    _descriptions += (name, () => description);
//
//  /** Gets the descriptions of this model. */
//  def descriptions : Map[String,()=>Iterator[String]] =
//    _descriptions.toMap();
}

/**
 * Models that can update their parameters from looking at one example
 * at a time.
 *
 * @author dramage
 */
trait LocalUpdateModelCompanion[ModelParams,Model,ModelState,DatumParams,Datum,DatumState]
extends ModelCompanion[ModelParams,Model,ModelState,DatumParams,Datum,DatumState] {
  /**
   * Does one iteration of learning on the given data, updating ModelState
   * and each Datum's DatumState as appropriate.
   */
  def doLearn(model : Model, data : Iterable[Datum]);
}

/**
 * DataParallel models can do assignments in parallel, updating model
 * parameters locally, aggregating model parameters upwards, e.g. in
 * a tree sum.
 *
 * @author dramage
 */
trait DataParallelModelCompanion[ModelParams,Model,ModelState,DatumParams,Datum,DatumState]
extends LocalUpdateModelCompanion[ModelParams,Model,ModelState,DatumParams,Datum,DatumState] {
  /**
   * Collects ModelStates into a new summary model state.
   */
  def doCollect(a : ModelState, b : ModelState) : ModelState;
}

/**
 * A model companion that provides an implicit FileSerialization backing for
 * a model where the parameters and state are both ReadWriteable.
 *
 * @author dramage
 */
trait SerializableModelCompanion[ModelParams,Model,ModelState] {
  self : ModelCompanion[ModelParams,Model,ModelState,_,_,_] =>

  /** Loads a model of this type from the given path. */
  def load(path : File)
  (implicit mpr : FileSerialization.Readable[ModelParams],
   msr : FileSerialization.Readable[ModelState]) : Model = {
    if (!new File(path, "description.txt").exists) {
      throw new scalanlp.serialization.SerializationException(
        path+" is not a "+name);
    }
    val description = scalanlp.io.TXTFile(path, "description.txt").iterator.next;
    if (description.trim != name) {
      throw new scalanlp.serialization.SerializationException(
        "Cannot load model "+path+": saved description.txt does match expected "+name);
    }

    val model = createModel(FileSerialization.read[ModelParams](path));
    useModelState(model, FileSerialization.read[ModelState](path));
    model;
  }

  /** Saves a model of this type to the given path. */
  def save(path : File, model : Model)
  (implicit mpw : FileSerialization.Writable[ModelParams],
   msw : FileSerialization.Writable[ModelState]) = {
    if (path.exists) {
      throw new scalanlp.serialization.SerializationException(
        "Cannot write model to "+path+": already exists");
    }
    path.mkdir();
    FileSerialization.write(new File(path, "description.txt"), name);
    FileSerialization.write(path, getModelParams(model));
    FileSerialization.write(path, getModelState(model));
  }

  /** FileSerialization.ReadWritable for the model: defers to load and save methods. */
  implicit def ReadWritable
  (implicit mprw : FileSerialization.ReadWritable[ModelParams],
   msrw : FileSerialization.ReadWritable[ModelState])
  : FileSerialization.ReadWritable[Model] = new FileSerialization.ReadWritable[Model] {
    override def read(path : File) : Model =
      self.load(path);

    override def write(path : File, model : Model) =
      self.save(path, model);
  }
}

///**
// * Models that have a summary describing their output.
// *
// * @author dramage
// */
//trait SummaryModelCompanion[Model] {
//  self : ModelCompanion[_,Model,_,_,_,_] =>
//
//  def summary(model : Model) : Iterator[String];
//}

/**
 * Models that support a log probability estimate.
 *
 * @author dramage
 */
trait LogProbabilityEstimateModelCompanion[Model,Datum] {
  self : ModelCompanion[_,Model,_,_,Datum,_] =>

  def getLogProbabilityEstimate(model : Model, datum : Datum) : Double;
}
