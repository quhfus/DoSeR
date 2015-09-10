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

import scalanlp.ra.Signature;

import scalanlp.serialization.{SubtypedCompanion,TypedCompanion1};

/**
 * Description of the Dirichlet hyperparmeters for a multinomial distribution.
 * @author dramage
 */
@serializable sealed trait DirichletParams extends Signature;
                                              
object DirichletParams extends SubtypedCompanion[DirichletParams] {
  prepare();
  register[SymmetricDirichletParams];
  register[AsymmetricDirichletParams];
  
  implicit def fromDouble(pseudocount : Double) : DirichletParams =
    SymmetricDirichletParams.fromDouble(pseudocount);
}

/**
 * A fixed-value (symmetric) dirichlet distribution, corresponding to add
 * psuedocount smoothing.
 *
 * @author dramage
 */
case class SymmetricDirichletParams(pseudocount : Double)
extends DirichletParams;

object SymmetricDirichletParams extends TypedCompanion1[Double,SymmetricDirichletParams] {
  implicit def fromDouble(pseudocount : Double) =
    SymmetricDirichletParams(pseudocount);
  
  prepare();
}

/**
 * A fixed-value (asymmetric) dirichlet distribution, corresponding to add
 * psuedocount smoothing.
 *
 * @author dramage
 */
case class AsymmetricDirichletParams(pseudocounts : Array[Double])
extends DirichletParams;

object AsymmetricDirichletParams extends TypedCompanion1[Array[Double],AsymmetricDirichletParams] {
  prepare();
}
