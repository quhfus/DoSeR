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
package pldp;

import lda._;
import llda._;
import plda._;
import learn.GibbsInferParams;

import java.io.File;
import java.util.Random;

import scala.collection.mutable.ArrayBuffer;

import scalala.operators.Implicits._;

import scalanlp.serialization.{FileSerialization,TextSerialization};
import scalanlp.util.Index;
import scalanlp.stage._;
import scalanlp.pipes.Pipes.global._;

/**
 * A fast Array[Int]-like object that can grow as needed to accomodate
 * more elements.
 * 
 * @author dramage
 */
class IntExpandArray(initialSize : Int = 10) {
  protected var data = new Array[Int](initialSize);
  protected var size = 0;

  final def apply(pos : Int) = {
    if (pos >= size) 0
    else data(pos);
  }

  final def update(pos : Int, value : Int) {
    if (pos >= size) {
      var newData = new Array[Int](pos + initialSize);
      System.arraycopy(data, 0, newData, 0, size);
      data = newData;
    }
    data(pos) = value;
  }

  final def clear() = {
    size = 0;
  }

  final def length = size;

  final def append(value : Int) = {
    if (size == data.length) {
      var newData = new Array[Int](size + initialSize);
      System.arraycopy(data, 0, newData, 0, size);
      data = newData;
    }
    data(size) = value;
    size += 1;
  }

  final def +=(value : Int) =
    append(value);

  final def sum = {
    var rv = 0;
    var i = 0;
    while (i < size) {
      rv += data(i);
      i += 1;
    }
    rv;
  }
}

/**
 * Partially Latent Dirichlet Process model.  Mixes together a set of
 * dirichlet process models by picking an (observed) subset of dirichlet
 * process models.
 *
 * @param alpha Held out probability of creating a new topic in the mixture.
 *
 * @author dramage
 */
class PLDP
(val termIndex  : Index[String], val termSmoothing  : Array[Double],
 val labelIndex : Index[String], val labelSmoothing : Array[Double],
 var alpha : Double = .01) {

  /** Number of labels in the model. */
  final def numLabels = labelIndex.size;

  /** Number of terms in the model. */
  final def numTerms = termIndex.size;

  var termSmoothingDenom = termSmoothing.sum;

  /** Number of times we have seen the label, topic, and term together. */
  val countLabelTopicTerm : Array[ArrayBuffer[Array[Int]]] =
    Array.fill(numLabels)(new ArrayBuffer[Array[Int]](30));

  /** Number of times we have seen the label and topic. */
  val countLabelTopic : Array[IntExpandArray] =
    Array.fill(numLabels)(new IntExpandArray());

  /** Tracks the number of non-empty topics in the given label. */
  val numUsedLabelTopics : Array[Int] =
    new Array[Int](numLabels);

  /** Returns the number of allocated (possibly-unused) topics in the label. */
  def numAllocatedLabelTopics(label : Int) =
    countLabelTopic(label).length;

  checkrep();

  @inline private final def require(condition : Boolean, msg : String) =
    if (!condition) throw new RuntimeException(msg);

  def status(println : (String=>Unit) = System.err.println) {
    println("[PLD] status");
    for ((labelName,labelId) <- labelIndex.zipWithIndex) {
      println("  "+labelName);
      println("    Using " + numUsedLabelTopics(labelId) + " / " + numAllocatedLabelTopics(labelId) + " topics");
    }
  }

  def log(msg : String) = System.err.println(msg);

  def checkrep() {
    log("[PLDP] checkrep");

    require(termIndex.size == termSmoothing.length,
            "Term index and smoothing do not align");

    require(labelIndex.size == labelSmoothing.length,
            "Label index and smoothing do not align");

    require(termSmoothing.forall(_ >= 0),
            "Found negative term smoothing");

    require(labelSmoothing.forall(_ >= 0),
            "Found negative label smoothing");

    require(termSmoothingDenom == termSmoothing.sum,
            "Mismatched term smoothing denom");

    for (label <- 0 until numLabels) {
      var checkNumUsedLabelTopic = 0;

      require(countLabelTopicTerm(label).length == countLabelTopic(label).length,
              "countLabelTopicTerm and countLabelTopic length mismatch");

      for (topic <- 0 until countLabelTopic(label).length) {
        var checkCountLabelTopic = 0;
        for ((count,term) <- countLabelTopicTerm(label)(topic).iterator.zipWithIndex) {
          require(count >= 0, "Found negative count");
          checkCountLabelTopic += count;
        }
        require(countLabelTopic(label)(topic) == checkCountLabelTopic,
                "countLabelTopicTerm (b) totals do not match countLabelTopic");

        if (checkCountLabelTopic > 0) {
          checkNumUsedLabelTopic += 1;
        }
      }

      require(numUsedLabelTopics(label) == checkNumUsedLabelTopic,
              "numUsedLabelTopics does not match non-zeros in countLabelTopic");
    }
  }

  def saveState(path : File) {
    ( for ((topics,labelNum) <- countLabelTopicTerm.iterator.zipWithIndex;
           (topic,topicNum) <- topics.iterator.zipWithIndex;
           if topic.sum > 0)
      yield {
        labelNum+"\t"+topicNum+"\t"+
        (for ((count,term) <- topic.iterator.zipWithIndex; if count > 0) yield (term+":"+count)).mkString(" ");
      }
    ) | file(path, "label-topic-term-counts.txt");

    termIndex.iterator | file(path, "term-index.txt");
    labelIndex.iterator | file(path, "label-index.txt");
    termSmoothing.iterator.map(_.toString) | file(path,"term-smoothing.tsv");
    labelSmoothing.iterator.map(_.toString) | file(path,"label-smoothing.tsv");
    Iterator(alpha.toString) | file("model-alpha.txt");
  }

  def loadState(path : File) {
    // check that indexes match
    if (this.termIndex != Index(file(path, "term-index.txt").getLines)) {
      throw new TrainingException("Incompatible model: term indexes don't match");
    }
    if (this.labelIndex != Index(file(path, "label-index.txt").getLines)) {
      throw new TrainingException("Incompatible model: topic indexes don't match");
    }

    // load smoothing values
    for ((line,i) <- file(path, "term-smoothing.tsv").getLines.zipWithIndex) {
      this.termSmoothing(i) = line.toDouble;
    }
    termSmoothingDenom = termSmoothing.sum;

    for ((line,i) <- file(path, "label-smoothing.tsv").getLines.zipWithIndex) {
      this.labelSmoothing(i) = line.toDouble;
    }

    countLabelTopicTerm.foreach(_.clear);
    countLabelTopic.foreach(_.clear);
    for (line <- file(path, "label-topic-term-counts.txt").getLines) {
      val fields = line.split("\t");
      val labelNum = fields(0).toInt;
      val topicNum = fields(1).toInt;
      while (topicNum >= countLabelTopicTerm(labelNum).size) {
        countLabelTopicTerm(labelNum) += new Array[Int](numTerms);
        countLabelTopic(labelNum) += 0;
      }
      for (tc <- fields(2).split(" ").map(_.split(":"))) {
        val term = tc(0).toInt;
        val count = tc(1).toInt;
        countLabelTopicTerm(labelNum)(topicNum)(term) = count;
        countLabelTopic(labelNum)(topicNum) += count;
      }
      if (countLabelTopic(labelNum)(topicNum) > 0) {
        numUsedLabelTopics(labelNum) += 1;
      }
    }

    checkrep();
  }

//  /** Creates a gibbs labeled lda instance and does inference in it. */
//  def inferPerDocTopicDistribution[Item,ID](dataset : LabeledLDADataset[Item], inferenceParams : GibbsInferParams) : Iterable[Array[Double]] = {
//    val topicIndex = Index(
//      for ((label,labelI) <- labelIndex.zipWithIndex;
//           topicI <- 0 until numUsedLabelTopics(labelI))
//      yield label+"-"+topicI
//    );
//
//    val plda = new GibbsLabeledLDA(
//      termIndex = this.termIndex, termSmoothing = this.termSmoothing,
//      topicIndex = topicIndex, topicSmoothing = Array.tabulate(topicIndex.size)(z => this.alpha)
//    );
//
//    for ((label,labelI) <- this.labelIndex.zipWithIndex;
//         ((topic,oldTopicI),newTopicI) <- this.countLabelTopicTerm(labelI).zipWithIndex.filter(tup => countLabelTopic(labelI)(tup._2) > 0).zipWithIndex;
//         (term,count) <- topic.iterator) {
//      plda.countTopicTerm(newTopicI)(term) += count;
//      plda.countTopic(newTopicI) += count;
//    }
//
//    val pldaDataset = PLDA.Dataset(dataset).asLabeledLDADataset(
//      numLatentTopics = if (labelIndex.indexOf("*latent*")>=0) this.numUsedLabelTopics(labelIndex.indexOf("*latent*")) else 0,
//      numTopicsPerLabel = Map() ++ (for ((label,labelI) <- this.labelIndex.zipWithIndex) yield (label, this.numUsedLabelTopics(labelI)))
//    );
//
//    plda.inferPerDocTopicDistribution(pldaDataset, inferenceParams);
//  }
}

object PLDP {

  type ActiveLabelTopic = Long;

  /** Returns a document-specific active label index and subtopic */
  @inline final def getActiveLabel(lt : ActiveLabelTopic) =
    (lt & 0x00000000ffffffffl).asInstanceOf[Int];

  /** Returns the topic half of an ActiveLabelTopic. */
  @inline final def getTopic(lt : ActiveLabelTopic) =
    (lt >> 32).asInstanceOf[Int];

  /** Returns the label half o an ActiveLabelTopic. */
  @inline final def mkActiveLabelTopic(activeLabelIndex: Int, topic : Int) : ActiveLabelTopic =
    activeLabelIndex.asInstanceOf[Long] | (topic.asInstanceOf[Long] << 32);

  class Document(val observed : Array[Int], val activeLabels : Array[Int]) {
    /** Total document length */
    final def length = observed.length;

    var hasAssignments = false;

    /** Which label has been assigned to each observation. */
    val assignedActiveLabelTopic = new Array[ActiveLabelTopic](length);

    /** Total number of times each (activeLabel)(topic) has been seen. */
    val countActiveLabelTopic = Array.fill(activeLabels.length)(new IntExpandArray());

    /** Total number of times each activeLabel has been seen. */
    val countActiveLabel : Array[Int] =
      new Array[Int](activeLabels.length);

    def checkrep() {
      System.err.println("[PLDP] doc checkrep");
      val check = Array.fill(activeLabels.length)(new IntExpandArray());
      for (i <- 0 until length) {
        check(getActiveLabel(assignedActiveLabelTopic(i)))(getTopic(assignedActiveLabelTopic(i))) += 1;
      }
      for (i <- 0 until check.length) {
        if (check(i) != countActiveLabelTopic(i)) {
          throw new RuntimeException("Mismatch: "+check(i) +" != "+countActiveLabelTopic(i));
        }
      }
      for (activeLabel <- 0 until activeLabels.length) {
        if (countActiveLabelTopic(activeLabel).sum != countActiveLabel(activeLabel)) {
          throw new RuntimeException("countActiveLabelTopic.sum != countActiveLabel");
        }
      }
    }
  }

  class Modeler[Document<:PLDP.Document, DocumentCollection<:Iterable[Document]]
  (val model : PLDP, val documents : DocumentCollection, val random : Random) {

    /** How often was the given label and topic observed with the given term. */
    def countLabelTopicTerm(label : Int, topic : Int, term : Int) =
      model.countLabelTopicTerm(label)(topic)(term);

    /** How often was the given label and topic observed globally. */
    def countLabelTopic(label : Int, topic : Int) =
      model.countLabelTopic(label)(topic);

    /** Updates the given (label,topic,term) by += update */
    def updateLabelTopicTerm(label : Int, topic : Int, term : Int, update : Int) = {
      if (topic == model.countLabelTopicTerm(label).length) {
        model.countLabelTopicTerm(label).append(new Array[Int](model.numTerms))
        model.countLabelTopic(label).append(0);
      }
      model.countLabelTopicTerm(label)(topic)(term) += update;
      model.countLabelTopic(label)(topic) += update;

      if (model.countLabelTopic(label)(topic) == 0 && update < 0) {
        // decrement numUsedLabelTopics if this update makes the given topic empty
        model.numUsedLabelTopics(label) -= 1;
      } else if (model.countLabelTopic(label)(topic) == update && update > 0) {
        // increment numUsedLabelTopics if this update makes the given topic non-empty
        model.numUsedLabelTopics(label) += 1;
      }
    }

    /** Resamples all assignments in the current document. */
    def sample(doc : Document) : Unit = {
      if (doc.observed.length == 0 || doc.activeLabels.length == 0) {
        return;
      }

      if (!doc.hasAssignments) {
        // special case - do not already have assignments
        var i = 0;
        while (i < doc.length) {
          val term = doc.observed(i);
          assert(term >= 0 && term < model.numTerms, "Invalid document term");

          val newActiveLabelTopic = sampleActiveLabelTopic(doc, term);
          val newActiveLabel = getActiveLabel(newActiveLabelTopic);
          val newTopic = getTopic(newActiveLabelTopic);
          val newLabel = doc.activeLabels(newActiveLabel);
          doc.assignedActiveLabelTopic(i) = newActiveLabelTopic;
          doc.countActiveLabelTopic(newActiveLabel)(newTopic) += 1;
          doc.countActiveLabel(newActiveLabel) += 1;
          updateLabelTopicTerm(newLabel, newTopic, term, +1);

          i += 1;
        }
        doc.hasAssignments = true;
      } else {
        // regular sampler - decrement counts before sampling.
        var i = 0;
        while (i < doc.length) {
          val term = doc.observed(i);

          val oldActiveLabelTopic = doc.assignedActiveLabelTopic(i);
          val oldActiveLabel = getActiveLabel(oldActiveLabelTopic);
          val oldTopic = getTopic(oldActiveLabelTopic);
          val oldLabel = doc.activeLabels(oldActiveLabel);
          doc.countActiveLabelTopic(oldActiveLabel)(oldTopic) -= 1;
          doc.countActiveLabel(oldActiveLabel) -= 1;
          updateLabelTopicTerm(oldLabel, oldTopic, term, -1);

          val newActiveLabelTopic = sampleActiveLabelTopic(doc, term);
          val newActiveLabel = getActiveLabel(newActiveLabelTopic);
          val newTopic = getTopic(newActiveLabelTopic);
          val newLabel = doc.activeLabels(newActiveLabel);
          doc.assignedActiveLabelTopic(i) = newActiveLabelTopic;
          doc.countActiveLabelTopic(newActiveLabel)(newTopic) += 1;
          doc.countActiveLabel(newActiveLabel) += 1;
          updateLabelTopicTerm(newLabel, newTopic, term, +1);

          i += 1;
        }
      }

      // doc.checkrep();
    }

    /** Sampling probability of corresponding sampleI */
    private var sampleP : Array[Double] = new Array[Double](1000);

    /** Sampling (existing) LabelTopic at corresponding prob sampleP */
    private var sampleI : Array[Long] = new Array[Long](1000);

    /** Number of samples. */
    private var sampleN : Int = 0;

    /** Sum of sampleP. */
    private var sampleZ : Double = 0;

    /** Clear the sampling array. */
    @inline private final def clearSamples() {
      sampleN = 0;
      sampleZ = 0;
    }

    /** Record the given sample making room if necessary. */
    @inline private final def addSample(prob : Double, indx : Long) {
      if (sampleN >= sampleP.length) {
        var newSampleP = new Array[Double](sampleN + 1000);
        System.arraycopy(sampleP, 0, newSampleP, 0, sampleN);
        sampleP = newSampleP;

        var newSampleI = new Array[Long](sampleN + 1000);
        System.arraycopy(sampleI, 0, newSampleI, 0, sampleN);
        sampleI = newSampleI;
      }
      sampleP(sampleN) = prob;
      sampleI(sampleN) = indx;
      sampleZ += prob;
      sampleN += 1;
    }

    @inline private final def drawSample() : ActiveLabelTopic = {
      // draw a sample
      var i = sampleN;
      var threshold = random.nextDouble * sampleZ;
      while (i > 0 && threshold > 0) {
        i -= 1;
        threshold -= sampleP(i);
      }
      return sampleI(i);
    }

    /** Fixed held-out probability of using a new topic for any given term. */
    val pNewTopic : Array[Double] =
      Array.tabulate(model.numTerms)(term =>
        model.alpha * (model.termSmoothing(term) / model.termSmoothingDenom));

    /** Resamples a new LabelTopic for the given term in the given doc. */
    def sampleActiveLabelTopic(doc : Document, term : Int) : ActiveLabelTopic = {
      // ready sampling distribution for topics within labels
      clearSamples();

      // compute sampling distributions (for a new or existing label-topic)
      {
        var activeLabel = 0;
        while (activeLabel < doc.activeLabels.length) {
          val label = doc.activeLabels(activeLabel);
          var topic = 0;

          // have we scored for a new topic yet?  only want to score a
          // one new topic per label, and unused topics within a label
          // count as old topics.
          var scoredNewTopic = false;

          // current number of non-empty topics in label
          val numUsedLabelTopics = model.numUsedLabelTopics(label);
          val numAllocatedLabelTopics = model.numAllocatedLabelTopics(label);

          while (topic < numAllocatedLabelTopics) {
            if (model.countLabelTopic(label)(topic) == 0 && !scoredNewTopic) {
              // this is an unused topic, and we haven't scored a new one yet,
              // so act as if this is a new topic
              scoredNewTopic = true;
              addSample(pNewTopic(term), mkActiveLabelTopic(activeLabel,topic));
            } else {
              // this is a non-empty existing topic
              val pTopicTerm =
                doc.countActiveLabelTopic(activeLabel)(topic) *
                ((countLabelTopicTerm(label,topic,term) + model.termSmoothing(term)) /
                 (countLabelTopic(label,topic) + model.termSmoothingDenom));

              addSample(pTopicTerm, mkActiveLabelTopic(activeLabel,topic));
            }

            topic += 1;
          }

          if (!scoredNewTopic) {
            // add a new topic if we haven't already re-used an empty topic
            addSample(pNewTopic(term), mkActiveLabelTopic(activeLabel,topic));
          }

          activeLabel += 1;
        }
      }

      return drawSample();
    }
  }

  //
  // Model IO
  //

  /** Constructs an instance of Saveable to a File. */
  implicit val Writable : FileSerialization.Writable[PLDP] =
  new FileSerialization.Writable[PLDP] {
    override def write(target : File, model : PLDP) {
      var saveTo = target;

      if (target.exists) {
        if (target.isDirectory && file(target,"dataset.txt").exists) {
          saveTo = file(target, "final");
        } else {
          throw new SaveException(target+" already exists");
        }
      }

      saveTo.mkdir();

      model.saveState(saveTo);
    }
  }

  implicit val Readable : FileSerialization.Readable[PLDP] =
  new FileSerialization.Readable[PLDP] {
    override def read(base : File) = {
      if (!base.exists) {
        throw new java.io.FileNotFoundException(base + " does not exist.");
      }
      if (!base.isDirectory) {
        throw new java.io.FileNotFoundException(base + " is not a valid model directory.");
      }

      val candidates =
        (List(base,file(base,"final")) ++ base.listFiles.filter(_.getName.matches("\\d+")).sortWith(_.getName > _.getName)).
        filter(_.isDirectory).filter(_.list.contains("term-index.txt"));

      if (candidates.isEmpty) {
        throw new java.io.FileNotFoundException(base + " is not a valid model directory.");
      }

      val path = candidates.head;

      // load term and label index, smoothing values
      val termIndex =
        Index(file(path,"term-index.txt").getLines);
      val labelIndex =
        Index(file(path,"label-index.txt").getLines);
      val termSmoothing =
        file(path,"term-smoothing.tsv").getLines.map(_.toDouble).toList.toArray;
      val labelSmoothing =
        file(path,"label-smoothing.tsv").getLines.map(_.toDouble).toList.toArray;

      val alpha =
        file(path,"model-alpha.txt").getLines.next.toDouble;

//      val tokenizer : Option[Tokenizer] = {
//        if (file(path,"tokenizer.txt").exists) {
//          Some(TextSerialization.fromString[Tokenizer](file(path,"tokenizer.txt").getLines.mkString("\n")));
//        } else {
//          None;
//        }
//      }

      // initialize model
      val model = new PLDP(
        termIndex = termIndex, termSmoothing = termSmoothing,
        labelIndex = labelIndex, labelSmoothing = labelSmoothing,
        alpha = alpha
      );

      model.loadState(path);

      model;
    }
  }
}


object TrainPLDP {

  /**
   * Trains a PLDP model, writing the model output to the given output folder.
   * If the output folder already exists, this method looks to see if it is a
   * compatible model whose training can be continued.
   *
   * @param output The name of the new output folder to create.
   * @param dataset The dataset to use while training.
   * @param modelParams Parameters describing the model to be trained.
   * @param trainingParams Parameters controlling convergence and verbosity of output
   *   during training.
   */
  def apply[Item]
  (dataset : LabeledLDADataset[Item],
   alpha : Double = .01,
   modelLatentTopics : Boolean = false,
   numIterations : Int = 1500,
   verbose : Boolean = true,
   output : Option[File] = None,
   termIndex : Option[Index[String]] = None,
   labelIndex : Option[Index[String]] = None)
  : PLDP = {
    val thisTermIndex = termIndex match {
      case Some(index) => index;
      case None => Index(for (doc <- dataset.items; term <- dataset.getTerms(doc)) yield term);
    }

    val thisLabelIndex = labelIndex match {
      case Some(index) => index;
      case None => Index(
          (
            if (modelLatentTopics) Iterator("*latent*") else Iterator.empty
          ) ++ (
            for (doc <- dataset.items.iterator; label <- dataset.getLabels(doc).iterator) yield label
          )
        );
    }

    val datasetLabels = {
      if (modelLatentTopics) {
        dataset.asLabels(thisLabelIndex).map(Array(0) ++ _)
      } else {
        dataset.asLabels(thisLabelIndex)
      }
    };

    val thisTermSmoothing = Array.tabulate(thisTermIndex.size)(i => .01);
    val thisLabelSmoothing = Array.tabulate(thisLabelIndex.size)(i => .01);
    val thisAlpha = alpha;

    // model to train (or to compare against when loading)
    val model = new PLDP(
      termIndex = thisTermIndex, termSmoothing = thisTermSmoothing,
      labelIndex = thisLabelIndex, labelSmoothing = thisLabelSmoothing,
      alpha = thisAlpha);

    val documents = List() ++ (
      for ((words,labels) <- dataset.asTerms(thisTermIndex) zip datasetLabels)
      yield new PLDP.Document(words, labels)
    );

    System.err.println("[PLDP] dataset size:" + documents.size);

    val modeler = new PLDP.Modeler[PLDP.Document,List[PLDP.Document]](
      model, documents, new Random());

    for (i <- 0 to numIterations) {
      System.err.println("[PLDP] iteration "+i);
      for ((doc,docNum) <- documents.zipWithIndex) {
        if (docNum % 100 == 0) {
          System.err.println("[doc] " + docNum + " " + doc.observed.length+" "+doc.activeLabels.length+" "+doc.hasAssignments);
        }
        modeler.sample(doc);
      }

      model.checkrep();
      model.status();

      System.err.println();
      if (i % 5 == 0 && verbose) {
        for ((label,l) <- model.labelIndex.zipWithIndex) {
          println(l + " " + label);
          for ((topic,t) <- model.countLabelTopicTerm(l).zipWithIndex;
               if (topic.sum > 10);
               topk = scalanlp.util.TopK(10,topic.iterator.zipWithIndex,(tup : (Int,Int)) => tup._1)) {
            println("  "+t+" "+topk.map(_._2).map(model.termIndex.get).mkString(" "));
          }
        }
      }

      if (i % 25 == 0 && output.isDefined) {
        val path = file(output.get,String.format("%05d",int2Integer(i)));
        path.mkdirs();
        model.saveState(path);
      }
    }

    return model;
  }
}
