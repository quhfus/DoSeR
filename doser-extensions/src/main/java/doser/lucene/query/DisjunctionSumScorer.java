package doser.lucene.query;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

/**
 * A Scorer for OR like queries, counterpart of <code>ConjunctionScorer</code>.
 * This Scorer implements {@link Scorer#advance(int)} and uses advance() on the
 * given Scorers.
 */
class DisjunctionSumScorer extends AbstractDisjunctionScorer {
	private final LearnToRankClause[] clauses;

	/** The document number of the current match. */
	private int doc = -1;

	private final int docBase;

	/** The minimum number of scorers that should match. */
	private final int minimumNrMatchers;

	/** The number of subscorers that provide the current match. */
	protected int nrMatchers = -1;

	private double scoreVal = Float.NaN;

	/**
	 * Construct a <code>AbstractDisjunctionScorer</code>.
	 * 
	 * @param weight
	 *            The weight to be used.
	 * @param subScorers
	 *            A collection of at least two subscorers.
	 * @param minimumNrMatchers
	 *            The positive minimum number of subscorers that should match to
	 *            match this query. <br>
	 *            When <code>minimumNrMatchers</code> is bigger than the number
	 *            of <code>subScorers</code>, no matches will be produced. <br>
	 *            When minimumNrMatchers equals the number of subScorers, it
	 *            more efficient to use <code>ConjunctionScorer</code>.
	 */
	public DisjunctionSumScorer(final Weight weight,
			final List<Scorer> subScorers, final int minimumNrMatchers,
			final LearnToRankClause[] ltrWeights, final int docBase)
			throws IOException {
		super(weight, subScorers.toArray(new Scorer[subScorers.size()]),
				ltrWeights, subScorers.size());

		if (minimumNrMatchers <= 0) {
			throw new IllegalArgumentException(
					"Minimum nr of matchers must be positive");
		}
		if (numScorers <= 1) {
			throw new IllegalArgumentException(
					"There must be at least 2 subScorers");
		}
		clauses = ltrWeights;
		this.minimumNrMatchers = minimumNrMatchers;
		this.docBase = docBase;
	}

	/**
	 * Construct a <code>AbstractDisjunctionScorer</code>, using one as the
	 * minimum number of matching subscorers.
	 */
	public DisjunctionSumScorer(final Weight weight,
			final List<Scorer> subScorers,
			final LearnToRankClause[] ltrWeights, final int docBase)
			throws IOException {
		this(weight, subScorers, 1, ltrWeights, docBase);
	}

	/**
	 * Advances to the first match beyond the current whose document number is
	 * greater than or equal to a given target. <br>
	 * The implementation uses the advance() method on the subscorers.
	 * 
	 * @param target
	 *            The target document number.
	 * @return the document whose number is greater than or equal to the given
	 *         target, or -1 if none exist.
	 */
	@Override
	public int advance(final int target) throws IOException {
		if (numScorers == 0) {
			return doc = NO_MORE_DOCS;
		}
		while (subScorers[0].docID() < target) {
			if (subScorers[0].advance(target) == NO_MORE_DOCS) {
				if (numScorers == 0) {
					return doc = NO_MORE_DOCS;
				}
			} else {
				heapAdjust(0);
			}
		}

		afterNext();

		if (nrMatchers >= minimumNrMatchers) {
			return doc;
		} else {
			return nextDoc();
		}
	}

	public void afterNext() throws IOException {
		final Scorer sub = subScorers[0];
		doc = sub.docID();
		if (doc == NO_MORE_DOCS) {
			nrMatchers = Integer.MAX_VALUE; // stop looping
		} else {
			scoreVal = sub.score() * clauses[0].getWeight();
			clauses[0].addFeatureValue(docBase, doc, (float) scoreVal);
			nrMatchers = 1;
			countMatches(1);
			countMatches(2);
		}
	}

	@Override
	public long cost() {
		// TODO Auto-generated method stub
		return 0;
	}

	// TODO: this currently scores, but so did the previous impl
	// TODO: remove recursion.
	// TODO: if we separate scoring, out of here, modify this
	// and afterNext() to terminate when nrMatchers == minimumNrMatchers
	// then also change freq() to just always compute it from scratch
	private void countMatches(final int root) throws IOException {
		if ((root < numScorers) && (subScorers[root].docID() == doc)) {
			nrMatchers++;
			final float val = subScorers[root].score()
					* clauses[root].getWeight();
			// if(root == 5) {
			// System.out.println("first"+doc);
			// System.out.println(subScorers[root].toString()+ " "
			// +subScorers[root].score());
			// }
			scoreVal += val;
			clauses[root].addFeatureValue(docBase, doc, val);
			countMatches((root << 1) + 1);
			countMatches((root << 1) + 2);
		}
	}

	@Override
	public int docID() {
		return doc;
	}

	@Override
	public int freq() throws IOException {
		return nrMatchers;
	}

	@Override
	public int nextDoc() throws IOException {
		assert doc != NO_MORE_DOCS;
		while (true) {
			while (subScorers[0].docID() == doc) {
				if (subScorers[0].nextDoc() == NO_MORE_DOCS) {
					heapRemoveRoot();
					if (numScorers < minimumNrMatchers) {
						return doc = NO_MORE_DOCS;
					}
				} else {
					heapAdjust(0);
				}
			}
			afterNext();
			if (nrMatchers >= minimumNrMatchers) {
				break;
			}
		}

		return doc;
	}

	/**
	 * Returns the scoreVal of the current document matching the query.
	 * Initially invalid, until {@link #nextDoc()} is called the first time.
	 */
	@Override
	public float score() throws IOException {
		return (float) scoreVal;
	}
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

// import java.util.List;
// import java.io.IOException;
//
// import org.apache.lucene.search.Scorer;
// import org.apache.lucene.search.Weight;
//
// /**
// * A Scorer for OR like queries, counterpart of
// <code>ConjunctionScorer</code>.
// * This Scorer implements {@link Scorer#advance(int)} and uses advance() on
// the
// * given Scorers.
// */
// class DisjunctionSumScorer extends AbstractDisjunctionScorer {
// /** The minimum number of scorers that should match. */
// private final int minimumNrMatchers;
//
// /** The document number of the current match. */
// private int doc = -1;
//
// /** The number of subscorers that provide the current match. */
// protected int nrMatchers = -1;
//
// private double scoreVal = Float.NaN;
//
// private final int docBase;
//
// private final LearnToRankClause[] clauses;
//
// /**
// * Construct a <code>AbstractDisjunctionScorer</code>.
// *
// * @param weight
// * The weight to be used.
// * @param subScorers
// * A collection of at least two subscorers.
// * @param minimumNrMatchers
// * The positive minimum number of subscorers that should match to
// * match this query. <br>
// * When <code>minimumNrMatchers</code> is bigger than the number
// * of <code>subScorers</code>, no matches will be produced. <br>
// * When minimumNrMatchers equals the number of subScorers, it
// * more efficient to use <code>ConjunctionScorer</code>.
// */
// public DisjunctionSumScorer(Weight weight, List<Scorer> subScorers,
// int minimumNrMatchers, LearnToRankClause[] learnToRankWeights,
// int docBase) throws IOException {
// super(weight, subScorers.toArray(new Scorer[subScorers.size()]),
// learnToRankWeights,
// subScorers.size());
//
// if (minimumNrMatchers <= 0) {
// throw new IllegalArgumentException(
// "Minimum nr of matchers must be positive");
// }
// if (numScorers <= 1) {
// throw new IllegalArgumentException(
// "There must be at least 2 subScorers");
// }
// this.clauses = learnToRankWeights;
// this.minimumNrMatchers = minimumNrMatchers;
// this.docBase = docBase;
// }
//
// /**
// * Construct a <code>AbstractDisjunctionScorer</code>, using one as the
// minimum
// * number of matching subscorers.
// */
// public DisjunctionSumScorer(Weight weight, List<Scorer> subScorers,
// LearnToRankClause[] learnToRankWeights, int docBase)
// throws IOException {
// this(weight, subScorers, 1, learnToRankWeights, docBase);
// }
//
// @Override
// public int nextDoc() throws IOException {
// assert doc != NO_MORE_DOCS;
// while (true) {
// while (subScorers[0].docID() == doc) {
// if (subScorers[0].nextDoc() != NO_MORE_DOCS) {
// heapAdjust(0);
// } else {
// heapRemoveRoot();
// if (numScorers < minimumNrMatchers) {
// return doc = NO_MORE_DOCS;
// }
// }
// }
// afterNext();
// if (nrMatchers >= minimumNrMatchers) {
// break;
// }
// }
//
// return doc;
// }
//
// private void afterNext() throws IOException {
// final Scorer sub = subScorers[0];
// doc = sub.docID();
// if (doc == NO_MORE_DOCS) {
// nrMatchers = Integer.MAX_VALUE; // stop looping
// } else {
// scoreVal = sub.score() * clauses[0].getWeight();
// clauses[0].addFeatureValue(docBase, doc, (float) scoreVal);
// nrMatchers = 1;
// countMatches(1);
// countMatches(2);
// }
// }
//
// // TODO: this currently scores, but so did the previous impl
// // TODO: remove recursion.
// // TODO: if we separate scoring, out of here, modify this
// // and afterNext() to terminate when nrMatchers == minimumNrMatchers
// // then also change freq() to just always compute it from scratch
// private void countMatches(int root) throws IOException {
// if (root < numScorers && subScorers[root].docID() == doc) {
// nrMatchers++;
// float val = subScorers[root].score() * clauses[root].getWeight();
// // if(root == 5) {
// // System.out.println("first"+doc);
// // System.out.println(subScorers[root].toString()+ " "
// +subScorers[root].score());
// // }
// scoreVal += val;
// clauses[root].addFeatureValue(docBase, doc, val);
// countMatches((root << 1) + 1);
// countMatches((root << 1) + 2);
// }
// }
//
// /**
// * Returns the scoreVal of the current document matching the query. Initially
// * invalid, until {@link #nextDoc()} is called the first time.
// */
// @Override
// public float scoreVal() throws IOException {
// return (float) scoreVal;
// }
//
// @Override
// public int docID() {
// return doc;
// }
//
// @Override
// public int freq() throws IOException {
// return nrMatchers;
// }
//
// /**
// * Advances to the first match beyond the current whose document number is
// * greater than or equal to a given target. <br>
// * The implementation uses the advance() method on the subscorers.
// *
// * @param target
// * The target document number.
// * @return the document whose number is greater than or equal to the given
// * target, or -1 if none exist.
// */
// @Override
// public int advance(int target) throws IOException {
// if (numScorers == 0)
// return doc = NO_MORE_DOCS;
// while (subScorers[0].docID() < target) {
// if (subScorers[0].advance(target) != NO_MORE_DOCS) {
// heapAdjust(0);
// } else {
// heapRemoveRoot();
// if (numScorers == 0) {
// return doc = NO_MORE_DOCS;
// }
// }
// }
//
// afterNext();
//
// if (nrMatchers >= minimumNrMatchers) {
// return doc;
// } else {
// return nextDoc();
// }
// }
// }
