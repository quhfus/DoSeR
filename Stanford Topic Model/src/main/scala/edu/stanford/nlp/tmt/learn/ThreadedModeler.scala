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

import scala.collection.mutable.ListBuffer;

import scalanlp.collection.LazyIterable;

import data.concurrent.Concurrent;

/**
 * Runs data parallel models as multiple threads on a single machine.
 *
 * @author dramage
 */
class ThreadedModeler[ModelParams,Model,ModelState,DatumParams,Datum,DatumState]
(override val companion : DataParallelModelCompanion[ModelParams,Model,ModelState,DatumParams,Datum,DatumState],
 numShards : Int = Runtime.getRuntime.availableProcessors)
extends Modeler[ModelParams,Model,ModelState,DatumParams,Datum,DatumState] {

  override def description = companion.name;

  protected implicit val ft : scala.concurrent.FutureTaskRunner =
    new scala.concurrent.ThreadRunner

  /** Each model is created independently. */
  var models : IndexedSeq[Model] = null;

  /** The shards that will contain the data. */
  val lists = List.fill(numShards)(ListBuffer[Datum]());

  /** Sharded view of data.  ListBuffer's toList does not make a copy. */
  def data =
    new Sharded[Datum](lists.map(_.toList));

  override def initialize(mp : ModelParams) = {
    models = IndexedSeq.tabulate(numShards)(s => companion.createModel(mp));
    lists.foreach(_.clear());
  }

  override def model =
    if (models != null) Some(models(0)) else None;

  override def getModelParams() =
    companion.getModelParams(model.get);

  override def useModelState(ms : ModelState) =
    models.foreach(model => companion.useModelState(model, ms));

  override def getModelState() =
    companion.getModelState(models(0));

  override def clearModelState() = {
    companion.clearModel(models(0));
    val state = companion.getModelState(models(0));
    var i = 1;
    while (i < models.length) {
      companion.useModelState(models(i), state);
      i += 1;
    }
  }

  override def clearData() =
    lists.foreach(_.clear());

  override def addData(dps : Iterable[DatumParams]) = {
    var i = data.size % numShards;
    for (dp <- dps.iterator) {
      lists(i % numShards) += companion.createDatum(models(i % numShards), dp);
      i += 1;
    }
  }

  override def getData =
    data;

  override def getDataSize =
    data.size;

  override def useDataState(dss : Iterable[DatumState]) = {
    require(data.size == dss.size, "Wrong number of items in DatumState iterable.")
    for ((d,s) <- data.iterator zip dss.iterator) {
      companion.useDatumState(d, s);
    }
  }

  override def getDataState() =
    LazyIterable[DatumState](data.size){ data.iterator.map(companion.getDatumState); }

  override def clearDataState() =
    data.foreach(companion.clearDatum);

  override def learnIteration = {
    data.foreachShard((shard,i) => {
      companion.doLearn(models(i), shard);
    });
    val state = Concurrent.reduce(models.map(companion.getModelState), companion.doCollect);
    models.foreach(model => companion.useModelState(model, state));
  }
}

object ThreadedModeler {
  def apply[ModelParams,Model,ModelState,DatumParams,Datum,DatumState]
  (companion : DataParallelModelCompanion[ModelParams,Model,ModelState,DatumParams,Datum,DatumState],
   numShards : Int = Runtime.getRuntime.availableProcessors) =
    new ThreadedModeler[ModelParams,Model,ModelState,DatumParams,Datum,DatumState](companion, numShards);
}
