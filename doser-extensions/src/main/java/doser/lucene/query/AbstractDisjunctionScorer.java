package doser.lucene.query;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

/**
 * Base class for Scorers that score disjunctions. Currently this just provides
 * helper methods to manage the heap.
 */
abstract class AbstractDisjunctionScorer extends Scorer {
	protected final LearnToRankClause ltrClauses[];

	protected int numScorers;

	protected final Scorer subScorers[];

	protected AbstractDisjunctionScorer(final Weight weight,
			final Scorer subScorers[], final LearnToRankClause[] ltrClauses,
			final int numScorers) {
		super(weight);
		this.subScorers = subScorers;
		this.numScorers = numScorers;
		this.ltrClauses = ltrClauses;
		heapify();
	}

	@Override
	public final Collection<ChildScorer> getChildren() {
		final ArrayList<ChildScorer> children = new ArrayList<ChildScorer>(
				numScorers);
		for (int i = 0; i < numScorers; i++) {
			children.add(new ChildScorer(subScorers[i], "SHOULD"));
		}
		return children;
	}

	/**
	 * The subtree of subScorers at root is a min heap except possibly for its
	 * root element. Bubble the root down as required to make the subtree a
	 * heap.
	 */
	protected final void heapAdjust(final int root) {
		final Scorer scorer = subScorers[root];
		final LearnToRankClause clause = ltrClauses[root];
		final int doc = scorer.docID();
		int var = root;
		while (var <= ((numScorers >> 1) - 1)) {
			final int lchild = (var << 1) + 1;
			final Scorer lscorer = subScorers[lchild];
			final LearnToRankClause lclause = ltrClauses[lchild];
			final int ldoc = lscorer.docID();
			int rdoc = Integer.MAX_VALUE;
			final int rchild = (var << 1) + 2;
			Scorer rscorer = null;
			LearnToRankClause rclause = null;
			if (rchild < numScorers) {
				rscorer = subScorers[rchild];
				rclause = ltrClauses[rchild];
				rdoc = rscorer.docID();
			}
			if (ldoc < doc) {
				if (rdoc < ldoc) {
					subScorers[var] = rscorer;
					ltrClauses[var] = rclause; //
					subScorers[rchild] = scorer;
					ltrClauses[rchild] = clause;//
					var = rchild;
				} else {
					subScorers[var] = lscorer;
					ltrClauses[var] = lclause; //
					subScorers[lchild] = scorer;
					ltrClauses[lchild] = clause; //
					var = lchild;
				}
			} else if (rdoc < doc) {
				subScorers[var] = rscorer;
				ltrClauses[var] = rclause; //
				subScorers[rchild] = scorer;
				ltrClauses[rchild] = clause; //
				var = rchild;
			} else {
				return;
			}
		}
	}

	/**
	 * Organize subScorers into a min heap with scorers generating the earliest
	 * document on top.
	 */
	protected final void heapify() {
		for (int i = (numScorers >> 1) - 1; i >= 0; i--) {
			heapAdjust(i);
		}
	}

	/**
	 * Remove the root Scorer from subScorers and re-establish it as a heap
	 */
	protected final void heapRemoveRoot() {
		if (numScorers == 1) {
			subScorers[0] = null;
			ltrClauses[0] = null; //
			numScorers = 0;
		} else {
			subScorers[0] = subScorers[numScorers - 1];
			ltrClauses[0] = ltrClauses[numScorers - 1]; //
			subScorers[numScorers - 1] = null;
			ltrClauses[numScorers - 1] = null; //
			--numScorers;
			heapAdjust(0);
		}
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

// import java.util.ArrayList;
// import java.util.Collection;
//
// import org.apache.lucene.search.Scorer;
// import org.apache.lucene.search.Weight;
//
// /**
// * Base class for Scorers that score disjunctions. Currently this just
// provides
// * helper methods to manage the heap.
// */
// abstract class AbstractDisjunctionScorer extends Scorer {
// protected final Scorer subScorers[];
//
// protected final LearnToRankClause ltrClauses[];
//
// protected int numScorers;
//
// protected AbstractDisjunctionScorer(Weight weight, Scorer subScorers[],
// LearnToRankClause[] ltrClauses,
// int numScorers) {
// super(weight);
// this.subScorers = subScorers;
// this.numScorers = numScorers;
// this.ltrClauses = ltrClauses;
// heapify();
// }
//
// /**
// * Organize subScorers into a min heap with scorers generating the earliest
// * document on top.
// */
// protected final void heapify() {
// for (int i = (numScorers >> 1) - 1; i >= 0; i--) {
// heapAdjust(i);
// }
// }
//
// /**
// * The subtree of subScorers at root is a min heap except possibly for its
// * root element. Bubble the root down as required to make the subtree a
// * heap.
// */
// protected final void heapAdjust(int root) {
// Scorer scorer = subScorers[root];
// LearnToRankClause clause = ltrClauses[root];
// int doc = scorer.docID();
// int i = root;
// while (i <= (numScorers >> 1) - 1) {
// int lchild = (i << 1) + 1;
// Scorer lscorer = subScorers[lchild];
// LearnToRankClause lclause = ltrClauses[lchild];
// int ldoc = lscorer.docID();
// int rdoc = Integer.MAX_VALUE, rchild = (i << 1) + 2;
// Scorer rscorer = null;
// LearnToRankClause rclause = null;
// if (rchild < numScorers) {
// rscorer = subScorers[rchild];
// rclause = ltrClauses[rchild];
// rdoc = rscorer.docID();
// }
// if (ldoc < doc) {
// if (rdoc < ldoc) {
// subScorers[i] = rscorer;
// ltrClauses[i] = rclause; //
// subScorers[rchild] = scorer;
// ltrClauses[rchild] = clause;//
// i = rchild;
// } else {
// subScorers[i] = lscorer;
// ltrClauses[i] = lclause; //
// subScorers[lchild] = scorer;
// ltrClauses[lchild] = clause; //
// i = lchild;
// }
// } else if (rdoc < doc) {
// subScorers[i] = rscorer;
// ltrClauses[i] = rclause; //
// subScorers[rchild] = scorer;
// ltrClauses[rchild] = clause; //
// i = rchild;
// } else {
// return;
// }
// }
// }
//
// /**
// * Remove the root Scorer from subScorers and re-establish it as a heap
// */
// protected final void heapRemoveRoot() {
// if (numScorers == 1) {
// subScorers[0] = null;
// ltrClauses[0] = null; //
// numScorers = 0;
// } else {
// subScorers[0] = subScorers[numScorers - 1];
// ltrClauses[0] = ltrClauses[numScorers - 1]; //
// subScorers[numScorers - 1] = null;
// ltrClauses[numScorers - 1] = null; //
// --numScorers;
// heapAdjust(0);
// }
// }
//
// @Override
// public final Collection<ChildScorer> getChildren() {
// ArrayList<ChildScorer> children = new ArrayList<ChildScorer>(numScorers);
// for (int i = 0; i < numScorers; i++) {
// children.add(new ChildScorer(subScorers[i], "SHOULD"));
// }
// return children;
// }
// }
