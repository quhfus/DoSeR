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

import scalanlp.collection.LazyIterable;
import scalanlp.pipes.Pipes.global._;
import scalanlp.io.CSVFile;
import scalanlp.serialization.{FileSerialization,DataSerialization,TableRowReadable,TableRowWritable};

/**
 * A Modeler holds a model and a set of data items, allowing certain
 * operations to be performed in aggregate on that data.
 *
 * @author dramage
 */
trait Modeler[ModelParams,Model,ModelState,DatumParams,Datum,DatumState] {

  /** Model companion used by this modeler. */
  def companion : ModelCompanion[ModelParams,Model,ModelState,DatumParams,Datum,DatumState];

  /** Description of the model being trained. */
  def description : String;

  /** Clears all documents and begins using the given model. */
  def initialize(mp : ModelParams);

  /** Returns the current model, if one has been initialized. */
  def model : Option[Model];

  def getModelParams : ModelParams;

  /** Uses the given ModelState in the current model. */
  def useModelState(ms : ModelState);

  /** Gets the current model state. */
  def getModelState : ModelState;

  /** Clears the model state (re-initializes). */
  def clearModelState();

  /** Clears all data items. */
  def clearData();
  
  /** Adds the given data items. */
  def addData(dps : Iterable[DatumParams])

  /** Returns all data items. */
  def getData : Iterable[Datum];
  
  /** Returns a function of the given data states. */
  def getMappedDataState[X:DataSerialization.ReadWritable](f : (DatumState => X)) =
    getData.map(datum => f(companion.getDatumState(datum)));
  
  /** Returns the number of data items available. */
  def getDataSize() : Int;

  /** Uses the given data state. */
  def useDataState(dss : Iterable[DatumState]);

  /** Returns the state of all data items. */
  def getDataState : Iterable[DatumState];

  /** Clears the state of all data items (but does not remove them). */
  def clearDataState();

  /** Does one iteration of learning. */
  def learnIteration();

  private type HasSummary = { def summary : Iterator[String] };
  private type HasName = { def name : String };

  /** Train the model on the given data. */
  def train
  (mp : ModelParams,
   data : Iterable[DatumParams],
   output : File = null,
   saveDataState : Boolean = true,
   maxIterations : Int = -1,
   outputIterations : Option[Int] = Some(50))
  (implicit msrw : FileSerialization.ReadWritable[ModelState],
   mprw : FileSerialization.ReadWritable[ModelParams],
   dr : TableRowReadable[DatumState], dw : TableRowWritable[DatumState])
  = {
    initialize(mp);
    clearModelState();
    clearData();
    addData(data);

    val desc = this.description + "\n" +
      ( try { data.asInstanceOf[HasName].name; } catch { case _ => "" } );

    Train(
      iteration = learnIteration,
      maxIterations = if (maxIterations > 0) Some(maxIterations) else None,
      output = if (output != null) Some(output) else None,
      saveState = Some(((path : File) => {
        try {
          model.get.asInstanceOf[HasSummary].summary | file(path, "summary.txt");
        } catch {
          // case ex : Throwable => ex.printStackTrace;
          case _ => /* ok - no summary */
        }

        if (companion.isInstanceOf[LogProbabilityEstimateModelCompanion[_,_]]) {
          val lpemc = companion.asInstanceOf[LogProbabilityEstimateModelCompanion[Model,Datum]];
          val lpe = getData.map(datum => lpemc.getLogProbabilityEstimate(model.get, datum)).sum;
          Iterator.single(lpe.toString) | file(path, "log-probability-estimate.txt");
        }

        FileSerialization.write(path, getModelParams);
        FileSerialization.write(path, getModelState);

        if (saveDataState) {
          CSVFile(path, "data-state.csv.gz").write(getDataState);
        }
      })),
      loadState = Some(((path : File) => {
        if (!CSVFile(path, "data-state.csv.gz").exists) {
          throw new TrainingException("Cannot continue training a model unless it was trained with saveDataState = true.  Delete the folder and try again.");
        }

        if (FileSerialization.read[ModelParams](path) != getModelParams) {
          throw new IllegalArgumentException("Cannot continue training model with incompatible parameters: "+path);
        }

        useModelState(FileSerialization.read[ModelState](path));
        useDataState(LazyIterable[DatumState] {
          CSVFile(path, "data-state.csv.gz").read[Iterator[DatumState]]
        });
      })),
      description = Some(desc),
      outputIterations = outputIterations
    );
  }
}
