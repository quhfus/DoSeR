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

import java.io.File;

import scalala.operators.Implicits._;
import scalala.collection.sparse.SparseArray;

import scalanlp.stage.Item;
import scalanlp.collection.LazyIterable;
import scalanlp.io.CSVFile;
import scalanlp.serialization._;
import scalanlp.util.TopK;

import data._;
import data.concurrent.Concurrent;
import learn._;
import model._;
import model.lda._;
import model.llda._;
import model.plda._;

package object stage {

  /** Trains a GibbsLDA model using the given model parametesr. */
  def TrainGibbsLDA(modelParams : LDAModelParams, dataset : Iterable[LDADocumentParams], output : File, maxIterations : Int = 1500) : GibbsLDA = {
    val modeler = SerialModeler(GibbsLDA);
    modeler.train(modelParams, dataset, output, saveDataState = false, maxIterations = maxIterations);
    if (output != null) {
      val table : Iterable[(String,Array[Double])] =
        modeler.data.view.map(doc => (doc.id,doc.signature));
      CSVFile(output, "document-topic-distributions.csv").write(table);
    }
    modeler.model.get;
  }

  /** Trains a CVB0LDA model using the given model parametesr. */
  def TrainCVB0LDA(modelParams : LDAModelParams, dataset : Iterable[LDADocumentParams], output : File, maxIterations : Int = 1000) : CVB0LDA = {
    val modeler = ThreadedModeler(CVB0LDA);
    modeler.train(modelParams, dataset, output, saveDataState = false, maxIterations = maxIterations);
    if (output != null) {
      val table : Iterable[(String,Array[Double])] =
        modeler.data.view.map(doc => (doc.id,doc.signature));
      CSVFile(output, "document-topic-distributions.csv").write(table);
    }
    return modeler.model.get;
  }

  /** Trains a GibbsLabeledLDA model using the given model parametesr. */
  def TrainGibbsLabeledLDA(modelParams : LabeledLDAModelParams, dataset : Iterable[LabeledLDADocumentParams], output : File, maxIterations : Int = 1500) : GibbsLabeledLDA = {
    val modeler = SerialModeler(GibbsLabeledLDA);
    modeler.train(modelParams, dataset, output, saveDataState = false, maxIterations = maxIterations);
    if (output != null) {
      val table : Iterable[(String,List[(Int,Double)])] =
        modeler.data.view.map(doc => (doc.id,doc.signature.activeIterator.toList));
      CSVFile(output, "document-topic-distributions.csv").write(table);
    }
    modeler.model.get;
  }

  /** Trains a CVB0LabeledLDA model using the given model parametesr. */
  def TrainCVB0LabeledLDA(modelParams : LabeledLDAModelParams, dataset : Iterable[LabeledLDADocumentParams], output : File, maxIterations : Int = 1000) : CVB0LabeledLDA = {
    val modeler = ThreadedModeler(CVB0LabeledLDA);
    modeler.train(modelParams, dataset, output, saveDataState = false, maxIterations = maxIterations);
    if (output != null) {
      val table : Iterable[(String,List[(Int,Double)])] =
        modeler.data.view.map(doc => (doc.id,doc.signature.activeIterator.toList));
      CSVFile(output, "document-topic-distributions.csv").write(table);
    }
    return modeler.model.get;
  }

  /** Trains a CVB0PLDA model using the given model parametesr. */
  def TrainCVB0PLDA(modelParams : PLDAModelParams, dataset : Iterable[PLDADocumentParams], output : File, maxIterations : Int = 1000) : CVB0PLDA = {
    val modeler = ThreadedModeler(CVB0PLDA);
    modeler.train(modelParams, dataset, output, saveDataState = false, maxIterations = maxIterations);
    if (output != null) {
      val table : Iterable[(String,List[(Int,Double)])] =
        modeler.data.view.map(doc => (doc.id,doc.signature.activeIterator.toList));
      CSVFile(output, "document-topic-distriubtions.csv").write(table);
    }
    return modeler.model.get;
  }

  /** Loads a GibbsLDA model from the given path. */
  def LoadGibbsLDA(path : File) : GibbsLDA = {
    val snapshots = path.listFiles.filter(_.isDirectory).filter(_.getName.matches("\\d+")).sortWith(_.getName.toInt > _.getName.toInt);
    if (!snapshots.isEmpty) {
      GibbsLDA.load(snapshots.head);
    } else {
      GibbsLDA.load(path);
    }
  }

  /** Loads a CVB0LDA model from the given path. */
  def LoadCVB0LDA(path : File) : CVB0LDA = {
    val snapshots = path.listFiles.filter(_.isDirectory).filter(_.getName.matches("\\d+")).sortWith(_.getName.toInt > _.getName.toInt);
    if (!snapshots.isEmpty) {
      CVB0LDA.load(snapshots.head);
    } else {
      CVB0LDA.load(path);
    }
  }

  /** Loads a GibbsLabeledLDA model from the given path. */
  def LoadGibbsLabeledLDA(path : File) : GibbsLabeledLDA = {
    val snapshots = path.listFiles.filter(_.isDirectory).filter(_.getName.matches("\\d+")).sortWith(_.getName.toInt > _.getName.toInt);
    if (!snapshots.isEmpty) {
      GibbsLabeledLDA.load(snapshots.head);
    } else {
      GibbsLabeledLDA.load(path);
    }
  }

  /** Loads a CVB0LabeledLDA model from the given path. */
  def LoadCVB0LabeledLDA(path : File) : CVB0LabeledLDA = {
    val snapshots = path.listFiles.filter(_.isDirectory).filter(_.getName.matches("\\d+")).sortWith(_.getName.toInt > _.getName.toInt);
    if (!snapshots.isEmpty) {
      CVB0LabeledLDA.load(snapshots.head);
    } else {
      CVB0LabeledLDA.load(path);
    }
  }

  /** Loads a CVB0PLDA model from the given path. */
  def LoadCVB0PLDA(path : File) : CVB0PLDA = {
    val snapshots = path.listFiles.filter(_.isDirectory).filter(_.getName.matches("\\d+")).sortWith(_.getName.toInt > _.getName.toInt);
    if (!snapshots.isEmpty) {
      CVB0PLDA.load(snapshots.head);
    } else {
      CVB0PLDA.load(path);
    }
  }

  /** Loads the document-topic assignments from the given path. */
  def LoadLDADocumentTopicDistributions(path : CSVFile) = LazyIterable[(String,Array[Double])] {
    CSVTableSerialization.read[Iterator[(String,Array[Double])]](path);
  }

  /** Returns an array of per-topic probabilities. */
  def InferGibbsDocumentTopicDistributions(
    model : GibbsLDA, dataset : Iterable[LDADocumentParams])
  = LazyIterable[(String,Array[Double])] {
    Concurrent.map(dataset.iterator, (dp : LDADocumentParams) => (dp.id -> model.infer(dp)));
  }

  /** Returns an array of per-topic probabilities. */
  def InferCVB0DocumentTopicDistributions(
    model : CVB0LDA, dataset : Iterable[LDADocumentParams])
  = LazyIterable[(String,Array[Double])] {
    Concurrent.map(dataset.iterator, (dp : LDADocumentParams) => (dp.id -> model.infer(dp)));
  }

  /** Returns an array of per-topic probabilities. */
  def InferGibbsLabeledLDADocumentTopicDistributions(
    model : GibbsLabeledLDA, dataset : Iterable[LabeledLDADocumentParams])
  = LazyIterable[(String,Array[Double])] {
    Concurrent.map(dataset.iterator, (dp : LabeledLDADocumentParams) => (dp.id -> model.infer(dp).toArray));
  }

  /** Returns an array of per-topic probabilities. */
  def InferCVB0LabeledLDADocumentTopicDistributions(
    model : CVB0LabeledLDA, dataset : Iterable[LabeledLDADocumentParams])
  = LazyIterable[(String,Array[Double])] {
    Concurrent.map(dataset.iterator, (dp : LabeledLDADocumentParams) => (dp.id -> model.infer(dp).toArray));
  }

  /** Returns an array of per-topic probabilities. */
  def InferCVB0PLDADocumentTopicDistributions(
    model : CVB0PLDA, dataset : Iterable[PLDADocumentParams])
  = LazyIterable[(String,Array[Double])] {
    Concurrent.map(dataset.iterator, (dp : PLDADocumentParams) => (dp.id -> model.infer(dp).toArray));
  }


  /**
   * Estimates the per-word topic distributions using the given model counts
   * and the per-document topic distributions.  This is not as exact as
   * inference, but is nearly so, and is much faster.
   */
  def EstimatePerWordTopicDistributions(
    model : LDA[_,_,_],
    dataset : Iterable[LDADocumentParams],
    perDocTopicDistributions : Iterable[(String,Array[Double])]
  ) : LazyIterable[(String,Array[Array[Double]])] = {
    def joinFn(dp : LDADocumentParams, td : (String,Array[Double])) : (String,Array[Array[Double]]) = {
      val termDists = dp.terms.map(term => {
        val termDist = Array.tabulate(model.numTopics)(
          topic => model.pTopicTerm(topic, term) * td._2(topic));
        termDist :/= termDist.sum;
        termDist;
      });
      (td._1, termDists);
    }

    Dataset.join(
      dataset, (dp : LDADocumentParams) => dp.id,
      perDocTopicDistributions, (td : (String,Array[Double])) => td._1,
      joinFn);
  }

  /**
   * Estimates the per-word topic distributions using the given model counts
   * and the per-document topic distributions.  This is not as exact as
   * inference, but is nearly so, and is much faster.
   */
  def EstimateLabeledLDAPerWordTopicDistributions(
    model : LabeledLDA[_,_,_],
    dataset : Iterable[LabeledLDADocumentParams],
    perDocTopicDistributions : Iterable[(String,SparseArray[Double])]
  ) : LazyIterable[(String,Array[SparseArray[Double]])] = {
    def joinFn(dp : LabeledLDADocumentParams, td : (String,SparseArray[Double])) : (String,Array[SparseArray[Double]]) = {
      val termDists = dp.terms.map(term => {
        val termDist = new SparseArray[Double](td._2.length);
        var sum = 0.0;
        td._2.foreachActivePair((topic : Int, prob : Double) => {
          val p = prob * model.pTopicTerm(topic,term);
          termDist(topic) = p;
          sum += p
        });
        termDist :/= sum;
        termDist;
      });
      (td._1, termDists);
    }

    Dataset.join(
      dataset, (dp : LabeledLDADocumentParams) => dp.id,
      perDocTopicDistributions, (td : (String,SparseArray[Double])) => td._1,
      joinFn);
  }

  /**
   * Gets the usage of each topic overall within a corpus.
   */
  def QueryTopicUsage[ID](
    model : TopicModel[_,_,_,_,_] with ClosedTopicSet,
    dataset : Iterable[LDADocumentParams],
    perDocTopicDistribution : Iterable[(String,Array[Double])])
  (implicit active : scalala.generic.collection.CanGetActiveValues[Array[Double],Int,Double])
  : Iterable[TopicUsage] = {
    type Tup = (LDADocumentParams,Int,Double,Double);

    // construct a view of the input as an iterator of tuples of
    // (doc,grouping,term,topic,probability)
    val tuples : Iterator[Tup] = {
      for ((doc,dist) <-
              Dataset.join(
                dataset, (dp : LDADocumentParams) => dp.id,
                perDocTopicDistribution, (item : (String,Array[Double])) => item._1
              ).iterator;
           (topic,prob) <- active(dist._2))
      yield (doc, topic, prob, prob * doc.terms.length);
    }

    def topicName(topic : Int) = model.topicIndex match {
      case Some(idx) => idx.get(topic);
      case None => "Topic "+("0"*(model.numTopics.toString.length - topic.toString.length))+topic;
    }

    // group and aggregate the tuples by topic and grouping
    Pivot.pivot(
      tuples,
      groupBy = (tup : Tup) => tup._2,
      map = (tup : Tup) => (tup._3, tup._4),
      reduce = (a : (Double,Double), b : (Double,Double)) => (a._1 + b._1, a._2 + b._2)
    ).toList.map(tup => {
      TopicUsage(topicName(tup._1), tup._2._1, tup._2._2)
    }).sorted;
  }

  /**
   * Gets the usage of each topic by sub-group.
   */
  def QueryTopicUsage[ID,Grouping](
    model : TopicModel[_,_,_,_,_] with ClosedTopicSet,
    dataset : Iterable[LDADocumentParams],
    perDocTopicDistribution : Iterable[(String,Array[Double])],
    grouping : Iterable[Item[ID,Grouping]])
  (implicit active : scalala.generic.collection.CanGetActiveValues[Array[Double],Int,Double],
   ordering : Ordering[Grouping])
  : Iterable[GroupedTopicUsage[Grouping]] = {
    type Tup = (LDADocumentParams,Grouping,Int,Double,Double);

    // construct a view of the input as an iterator of tuples of
    // (doc,grouping,term,topic,probability)
    val tuples : Iterator[Tup] = {
      for (((doc,grouping),dist) <-
              Dataset.join(
                Dataset.join(
                  dataset, (dp : LDADocumentParams) => dp.id,
                  grouping, (gp : Item[ID,Grouping]) => gp.id.toString),
                (tup : (LDADocumentParams,Item[ID,Grouping])) => tup._1.id,
                perDocTopicDistribution,
                (item : (String,Array[Double])) => item._1).iterator;
           (topic,prob) <- active(dist._2))
      yield (doc, grouping.value, topic, prob, prob * doc.terms.length);
    }

    def topicName(topic : Int) = model.topicIndex match {
      case Some(idx) => idx.get(topic);
      case None => "Topic "+("0"*(model.numTopics.toString.length - topic.toString.length))+topic;
    }

    // group and aggregate the tuples by topic and grouping
    Pivot.pivot(
      tuples,
      groupBy = (tup : Tup) => ((tup._3,tup._2)),
      map = (tup : Tup) => (tup._4, tup._5),
      reduce = (a : (Double,Double), b : (Double,Double)) => (a._1 + b._1, a._2 + b._2)
    ).toList.map(tup => {
      GroupedTopicUsage[Grouping](topicName(tup._1._1), tup._1._2, tup._2._1, tup._2._2)
    }).sorted;
  }

  /** Returns the top terms associated with the model. */
  def QueryTopTerms(model : TopicModel[_,_,_,_,_] with ClosedTopicSet, numTerms : Int) = LazyIterable[(String,String,Int,Double)] {
    for (topic <- Iterator.range(0, model.numTopics);
         (term,rank) <- TopK(numTerms, 0 until model.numTerms, (t: Int) => model.pTopicTerm(topic, t)).iterator.zipWithIndex)
    yield {
      (model.topicIndex match { case Some(idx) => idx.get(topic); case None => "Topic "+topic; },
       model.termIndex  match { case Some(idx) => idx.get(term);  case None => term.toString; },
       rank,
       model.pTopicTerm(topic, term));
    }
  }

  /**
   * Gets the top terms in each topic, but counting terms instances
   * separately for members of each group.
   */
  def QueryTopTerms[ID,Dist](
    model : TopicModel[_,_,_,_,_] with ClosedTopicSet,
    dataset : Iterable[LDADocumentParams],
    perDocWordTopicDistribution : Iterable[(String,Array[Dist])],
    numTopTerms : Int)
  (implicit active : scalala.generic.collection.CanGetActiveValues[Dist,Int,Double])
  : Iterable[TopTerms] = {
    type Tup = (LDADocumentParams,Int,Int,Double);

    // construct a view of the input as an iterator of tuples of
    // (doc,grouping,term,topic,probability)
    def tuples : Iterator[Tup] = {
      for ((doc,dists) <-
              Dataset.join(
                dataset, (dp : LDADocumentParams) => dp.id,
                perDocWordTopicDistribution, (item : (String,Array[Dist])) => item._1).iterator;
           (term,termDist) <- (doc.terms.iterator zip dists._2.iterator);
           (topic,prob) <- active(termDist))
      yield (doc, term, topic, prob);
    }


    def topicName(topic : Int) = model.topicIndex match {
      case Some(idx) => idx.get(topic);
      case None => "Topic "+("0"*(model.numTopics.toString.length - topic.toString.length))+topic;
    }

    // group and aggregate the tuples by topic and grouping
    Pivot.reduce(
      tuples,
      groupBy = (tup : Tup) => tup._3,
      initial = () => new Array[Double](model.numTerms),
      reduceLeft = (a : Array[Double], tup : Tup) => { a(tup._2) += tup._4; a },
      reduce = (a : Array[Double], b : Array[Double]) => { a :+= b; a }
    ).toList.map(tup => {
      val tops = TopK(numTopTerms, tup._2.iterator.zipWithIndex, (st : (Double,Int)) => st._1);
      TopTerms(topicName(tup._1), tops.map(_._2).map(model.termIndex.get.get).toList);
    }).sorted;
  }

  /**
   * Gets the top terms in each topic, but counting terms instances
   * separately for members of each group.
   */
  def QueryTopTerms[ID,Dist,Grouping](
    model : TopicModel[_,_,_,_,_] with ClosedTopicSet,
    dataset : Iterable[LDADocumentParams],
    perDocWordTopicDistribution : Iterable[(String,Array[Dist])],
    numTopTerms : Int,
    grouping : Iterable[Item[ID,Grouping]])
  (implicit active : scalala.generic.collection.CanGetActiveValues[Dist,Int,Double],
   ordering : Ordering[Grouping])
  : Iterable[GroupedTopTerms[Grouping]] = {
    type Tup = (LDADocumentParams,Grouping,Int,Int,Double);

    // construct a view of the input as an iterator of tuples of
    // (doc,grouping,term,topic,probability)
    def tuples : Iterator[Tup] = {
      for (((doc,grouping),dists) <-
              Dataset.join(
                Dataset.join(
                  dataset, (dp : LDADocumentParams) => dp.id,
                  grouping, (gp : Item[ID,Grouping]) => gp.id.toString),
                (tup : (LDADocumentParams,Item[ID,Grouping])) => tup._1.id,
                perDocWordTopicDistribution,
                (item : (String,Array[Dist])) => item._1).iterator;
           (term,termDist) <- (doc.terms.iterator zip dists._2.iterator);
           (topic,prob) <- active(termDist))
      yield (doc, grouping.value, term, topic, prob);
    }


    def topicName(topic : Int) = model.topicIndex match {
      case Some(idx) => idx.get(topic);
      case None => "Topic "+("0"*(model.numTopics.toString.length - topic.toString.length))+topic;
    }

    // group and aggregate the tuples by topic and grouping
    Pivot.reduce(
      tuples,
      groupBy = (tup : Tup) => ((tup._4,tup._2)),
      initial = () => new Array[Double](model.numTerms),
      reduceLeft = (a : Array[Double], tup : Tup) => { a(tup._3) += tup._5; a },
      reduce = (a : Array[Double], b : Array[Double]) => { a :+= b; a }
    ).toList.map(tup => {
      val tops = TopK(numTopTerms, tup._2.iterator.zipWithIndex, (st : (Double,Int)) => st._1);
      GroupedTopTerms[Grouping](topicName(tup._1._1), tup._1._2, tops.map(_._2).map(model.termIndex.get.get).toList);
    }).sorted;
  }
}

package stage {
  /**
   * Describes the usage of a topic within a grouping.
   *
   * @author dramage
   */
  case class TopicUsage
  (topic : String, numDocuments : Double, numWords : Double);

  object TopicUsage extends TableRowCompanion[TopicUsage,(String,Double,Double)] {
    override val header = Some(List("Topic","Documents","Words"));

    implicit val ordering : Ordering[TopicUsage] = new Ordering[TopicUsage] {
      override def compare(a : TopicUsage, b : TopicUsage) =
        a.topic compare b.topic;
    }
  }

  /**
   * Describes the usage of a topic within a grouping.
   *
   * @author dramage
   */
  case class GroupedTopicUsage[Grouping]
  (topic : String, grouping : Grouping, numDocuments : Double, numWords : Double)

  object GroupedTopicUsage {
    class CustomRowWritable[Grouping:TableMultiCellWritable]
    extends TableRowWritable[GroupedTopicUsage[Grouping]] {
      override val header = Some(
        List("Topic") ++
        (List("Group ID").iterator ++ Stream.from(0).map(i => "").iterator).take(implicitly[TableMultiCellWritable[Grouping]].size).toList ++
        List("Documents", "Words")
      );

      override def write(writer : TableRowWriter, value : GroupedTopicUsage[Grouping]) = {
        implicitly[TableCellWritable[String]].write(writer.next, value.topic);
        implicitly[TableMultiCellWritable[Grouping]].write(writer, value.grouping);
        implicitly[TableCellWritable[Double]].write(writer.next, value.numDocuments);
        implicitly[TableCellWritable[Double]].write(writer.next, value.numWords);
        writer.finish;
      }
    }

    implicit def mkCustomRowWritable[Grouping:TableMultiCellWritable] =
      new CustomRowWritable[Grouping];

    implicit def mkOrdering[Grouping:Ordering] : Ordering[GroupedTopicUsage[Grouping]]
    = new Ordering[GroupedTopicUsage[Grouping]] {
      override def compare(a : GroupedTopicUsage[Grouping], b : GroupedTopicUsage[Grouping]) = {
        if (a.topic < b.topic) -1
        else if (a.topic == b.topic) implicitly[Ordering[Grouping]].compare(a.grouping, b.grouping);
        else 1;
      }
    }
  }

  /**
   * Describes the top-k terms in a topic.
   *
   * @author dramage
   */
  case class TopTerms(topic : String, terms : List[String]);

  object TopTerms extends TableRowCompanion[TopTerms,(String,List[String])] {
    override val header = Some(List("Topic","Top Terms"));

    implicit val ordering : Ordering[TopTerms] = new Ordering[TopTerms] {
      override def compare(a : TopTerms, b : TopTerms) =
        a.topic compare b.topic;
    }
  }

  /**
   * Describes the top-k terms in a topic by grouping.
   *
   * @author dramage
   */
  case class GroupedTopTerms[Grouping]
  (topic : String, grouping : Grouping, terms : List[String]);

  object GroupedTopTerms {
    class CustomRowWritable[Grouping:TableMultiCellWritable]
    extends TableRowWritable[GroupedTopTerms[Grouping]] {
      override val header = Some(
        List("Topic") ++
        (List("Group ID").iterator ++ Stream.from(0).map(i => "").iterator).take(implicitly[TableMultiCellWritable[Grouping]].size).toList ++
        List("Top Terms")
      );

      override def write(writer : TableRowWriter, value : GroupedTopTerms[Grouping]) = {
        implicitly[TableCellWritable[String]].write(writer.next, value.topic);
        implicitly[TableMultiCellWritable[Grouping]].write(writer, value.grouping);
        implicitly[TableRowWritable[List[String]]].write(writer, value.terms);
      }
    }

    implicit def mkCustomRowWritable[Grouping:TableMultiCellWritable] =
      new CustomRowWritable[Grouping];

    implicit def mkOrdering[Grouping:Ordering] : Ordering[GroupedTopTerms[Grouping]]
    = new Ordering[GroupedTopTerms[Grouping]] {
      override def compare(a : GroupedTopTerms[Grouping], b : GroupedTopTerms[Grouping]) = {
        if (a.topic < b.topic) -1
        else if (a.topic == b.topic) implicitly[Ordering[Grouping]].compare(a.grouping, b.grouping);
        else 1;
      }
    }
  }
}
