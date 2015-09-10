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

/**
 * Single-threaded modeler that holds data items in an internal ListBuffer.
 *
 * @author dramage
 */
class SerialModeler[ModelParams,Model,ModelState,DatumParams,Datum,DatumState]
(override val companion : LocalUpdateModelCompanion[ModelParams,Model,ModelState,DatumParams,Datum,DatumState])
extends Modeler[ModelParams,Model,ModelState,DatumParams,Datum,DatumState] {
  protected var _model : Option[Model] = null;
  protected var _iter : Option[Iterator[Datum]] = null;

  val data = ListBuffer[Datum]();

  override def description = companion.name;

  override def initialize(mp : ModelParams) = {
    _model = Some(companion.createModel(mp));
    data.clear;
  }

  override def model =
    _model;

  override def getModelParams() =
    companion.getModelParams(model.get);

  override def useModelState(ms : ModelState) =
    companion.useModelState(model.get, ms);

  override def getModelState() =
    companion.getModelState(model.get);

  override def clearModelState() =
    companion.clearModel(model.get);

  override def clearData() =
    data.clear;

  override def addData(dps : Iterable[DatumParams]) =
    for (dp <- dps) data += companion.createDatum(model.get, dp);

  override def getData =
    data;

  override def getDataSize() =
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

  override def learnIteration =
    companion.doLearn(model.get, data);
}

object SerialModeler {
  def apply[ModelParams,Model,ModelState,DatumParams,Datum,DatumState]
  (companion : LocalUpdateModelCompanion[ModelParams,Model,ModelState,DatumParams,Datum,DatumState]) =
    new SerialModeler(companion);
}
