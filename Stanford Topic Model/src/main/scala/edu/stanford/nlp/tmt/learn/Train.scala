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
package edu.stanford.nlp.tmt.learn;

import java.io.File;

import scalanlp.serialization.TextSerialization;

import scalanlp.ra.RA.global.pipes._;

/**
 * Static method for resumable training of a model.
 * 
 * @author dramage
 */
object Train {
  /**
   * Resumable model training with a status summary.
   *
   * @param iteration One iteration of learning, returning some summary of the
   *   computation
   *
   * @param maxIterations Hard maximum number of iterations of learning.
   *
   * @param output Target for saving the final model (and periodic intermediate
   *   models if outputIterations is defined).
   *
   * @param saveState Saves the state of the model to the given folder.
   *   Mandatory if output is not None.
   *
   * @param loadState Loads the state of the model from the given folder.
   *   Mandatory if output is not None.
   *
   * @param description Some (human-readable) description of the source of the
   *   model and data.  For checking if the right model is stored on disk for
   *   resumed training.
   *
   * @param outputIterations If defined, saves the intermediate state of the
   *   model (and its history) every outputIterations iterations (e.g. every
   *   50 iterations if outputIterations=Some(50)).
   *
   * @param convergence Test if the model has converged from a list of
   *   iteration numbers and summaries.  The length of the list is maxHistory
   *   if maxHistory is defined, else the length of the history list is
   *   unbounded.
   *
   * @param maxHistory Maximum length of history provided to convergence.
   *
   * @param log Where status messages go.
   */
  def apply[Summary:TextSerialization.ReadWritable](
    iteration : () => Summary,
    maxIterations : Option[Int] = None,
    output : Option[File] = None,
    saveState : Option[File => Unit] = None,
    loadState : Option[File => Unit] = None,
    description : Option[String] = None,
    outputIterations : Option[Int] = None,
    convergence : Option[List[(Int,Summary)] => Boolean] = None,
    maxHistory : Option[Int] = None,
    log : (String => Unit) = System.err.println
  ) {
    // training history as (Iteration,Summary) for the maxHistorySize most
    // recent summaries
    var history = List[(Int,Summary)]();

    // thrown when loaders are not defined as they should be
    def errBadLoaders() =
      throw new TrainingException("If output is not None, must provide loadState and saveState.");

    // Verify that save and load are defined if output is defined.
    if (output.isDefined && !(saveState.isDefined && loadState.isDefined)) {
      errBadLoaders();
    }

    // Check to make sure that we will stop
    if (!maxIterations.isDefined && !convergence.isDefined) {
      throw new TrainingException("Neither maxIterations nor convergence "+
                                  "test specified: will loop forever.");
    }

    // get the starting iteration from looking at the output path and
    // resuming previous state if necessary
    var tic : Int = output match {
      case None =>
        // start training from scratch with no saving
        0;

      case Some(target) =>
        if (!target.exists || (target.isDirectory && target.listFiles.length == 0)) {
          // output path provided but does not yet exist
          target.mkdirs();

          // write description if provided
          description match {
            case Some(s) => Iterator(s) | file(target, "description.txt");
            case None => /* intentionally empty */;
          }

          // starting from iteration 0
          0;
        } else {
          // resume training from target state
          val partials = target.listFiles.filter(_.getName.matches("\\d+")).
            toList.sortWith(_.getName.toInt < _.getName.toInt);

          // check description if provided
          description match {
            case Some(s) =>
              if (file(target, "description.txt").getLines.mkString("\n") != s) {
                throw new TrainingException(
                  "Cannot continue training model in "+target+": "+
                  "model on disk was trained with different parameters or data.");
              }
            case None => /* intentionally empty */
          }

          // no partial models to load
          if (partials.isEmpty) {
            0;
          } else {
            // the last model, to continue training
            val continued = partials.last;

            // reload that models history, if it is present
            if (file(continued, "history.txt").exists) {
              for (line <- file(continued, "history.txt").getLines) {
                val split = line.indexOf(' ');
                history = history :+
                  (line.take(split).toInt,
                   TextSerialization.fromString[Summary](line.drop(split+1)));
              }
            }

            // load model state if we have a loader
            loadState match {
              case Some(load) => load(continued);
              case None => errBadLoaders();
            }

            // starting from iteration continued+1
            continued.getName.toInt+1;
          }
        }
    }

    // convergence test
    def converged() =
      ( maxIterations match {
          case Some(maximum) => tic >= maximum;
          case None => false;
        }
      ) || (
        convergence match {
          case Some(test) => test(history);
          case None => false;
        }
      );

    var done = converged();

    while (!done) {
      log("[Train] iteration "+tic);

      // do one iteration of learning
      val summary = iteration();

      // update history with the summary
      history = history :+ ((tic,summary));
      if (maxHistory.isDefined && history.length > maxHistory.get) {
        history = history.drop(history.length - maxHistory.get);
      }

      // check convergence
      done = converged();

      // save if this is an output iteration
      if (output.isDefined && (done || (outputIterations.isDefined && (tic % outputIterations.get) == 0))) {
        // save intermediate model to numbered output folder
        val path = file(output.get, String.format("%05d",int2Integer(tic)));

        if (path.exists()) {
          throw new TrainingException("Could not create new folder "+path);
        }
        path.mkdir();

        log("[Train] writing snapshot to "+path);
        // write model
        saveState match {
          case Some(save) => save(path);
          case None => errBadLoaders();
        }

        // write history
        if (history.length > 0 && !history.head._2.isInstanceOf[Unit]) {
          ( for ((iter,summary) <- history.iterator) yield
            iter + " " + TextSerialization.toString(summary)
          ) | file(path, "history.txt");
        }
        
        // write description if provided
        description match {
          case Some(s) => Iterator(s) | file(path, "description.txt");
          case None => /* intentionally empty */;
        }
      }

      tic += 1;
    }
  }
}

class TrainingException(msg : String) extends RuntimeException(msg);
