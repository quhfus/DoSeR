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

import scalala.operators.{BinaryUpdateOp,OpAdd,OpDiv};

/**
 * Inference parameters for gibbs sampling.  These defaults tend to
 * make sense for LDA inference on relatively short documents, but
 * are not applicable to all chains.
 *
 * @author dramage
 */
case class GibbsInferParams(
  burnIterations : Int = 500, chains : Int = 1,
  samplesPerChain : Int = 20, skipIterations : Int = 20
);

/**
 * Inference in Gibbs Samplers based on updating a given result distribution
 * using samples drawn from a chain.
 *
 * @author dramage
 */
object GibbsInfer {
  def apply[Dist,State]
  (result : Dist, reset : (() => Unit), sample : (() => Unit), state : (() => State), params : GibbsInferParams)
  (implicit addInto : BinaryUpdateOp[Dist,State,OpAdd], divInto : BinaryUpdateOp[Dist,Double,OpDiv]) = {
    import params._;

    var chain = 0;
    while (chain < chains) {
      reset();

      var iter = 0;
      while (iter < burnIterations-skipIterations) {
        sample();
        iter += 1;
      }

      // collect samples from chain
      var s = 0;
      while (s < samplesPerChain) {
        var skip = 0;
        while (skip < skipIterations) {
          sample();
          skip += 1;
        }
        sample();
        addInto(result, state());

        s += 1;
      }
      chain += 1;
    }

    divInto(result, chains * samplesPerChain);
    result;
  }
}
