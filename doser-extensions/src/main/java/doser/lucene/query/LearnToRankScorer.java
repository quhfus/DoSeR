package doser.lucene.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.similarities.Similarity;

import doser.lucene.query.LearnToRankQuery.LearnToRankWeight;

/**
 * An alternative to BooleanScorer that also allows a minimum number of optional
 * scorers that should match. <br>
 * Implements skipTo(), and has no limitations on the numbers of added scorers. <br>
 * Uses ConjunctionScorer, AbstractDisjunctionScorer, ReqOptScorer and
 * ReqExclScorer.
 */
class LearnToRankScorer extends Scorer { // NOPMD by quh on 28.02.14 10:44

	/** Count a scorer as a single match. */
	private class SingleMatchScorer extends Scorer {
		private final LearnToRankClause clause;
		// Save the score of lastScoredDoc, so that we don't compute it more
		// than
		// once in score().
		private float lastDocScore = Float.NaN;
		private int lastScoredDoc = -1;

		private final Scorer scorer;

		SingleMatchScorer(final Scorer scorer, final LearnToRankClause clause) {
			super(scorer.getWeight());
			this.scorer = scorer;
			this.clause = clause;
		}

		@Override
		public int advance(final int target) throws IOException {
			return scorer.advance(target);
		}

		@Override
		public long cost() {
			return 0;
		}

		@Override
		public int docID() {
			return scorer.docID();
		}

		@Override
		public int freq() throws IOException {
			return 1;
		}

		@Override
		public int nextDoc() throws IOException {
			return scorer.nextDoc();
		}

		@Override
		public float score() throws IOException {
			final int doc = docID();
			if (doc > lastScoredDoc) {
				lastDocScore = scorer.score();
				lastScoredDoc = doc;
			}
			final float val = lastDocScore * clause.getWeight();
			clause.addFeatureValue(docBase, doc, val);
			return val;
		}
	}

	/**
	 * The scorer to which all scoring will be delegated, except for computing
	 * and using the coordination factor.
	 */
	private final Scorer countingSumScorer;

	private int doc = -1;

	private final int docBase;

	private final LearnToRankClause[] optionalClauses;

	private final List<Scorer> optionalScorers;

	private final LearnToRankClause[] requiredClauses;

	private final List<Scorer> requiredScorers;

	/**
	 * Creates a {@link Scorer} with the given similarity and lists of required,
	 * prohibited and optional scorers. In no required scorers are added, at
	 * least one of the optional scorers will have to match during the search.
	 * 
	 * @param weight
	 *            The BooleanWeight to be used.
	 * @param disableCoord
	 *            If this parameter is true, coordination level matching (
	 *            {@link Similarity#coord(int, int)}) is not used.
	 * @param minNrShouldMatch
	 *            The minimum number of optional added scorers that should match
	 *            during the search. In case no required scorers are added, at
	 *            least one of the optional scorers will have to match during
	 *            the search.
	 * @param required
	 *            the list of required scorers.
	 * @param prohibited
	 *            the list of prohibited scorers.
	 * @param optional
	 *            the list of optional scorers.
	 */
	public LearnToRankScorer(final LearnToRankWeight weight,
			final List<Scorer> optional, final List<Scorer> required,
			final LearnToRankClause[] optionalClauses,
			final LearnToRankClause[] requiredClauses, final int docBase)
			throws IOException {
		super(weight);
		this.docBase = docBase;
		optionalScorers = optional;
		requiredScorers = required;
		this.optionalClauses = optionalClauses;
		this.requiredClauses = requiredClauses;
		countingSumScorer = createSumScorer();
	}

	@Override
	public int advance(final int target) throws IOException {
		return doc = countingSumScorer.advance(target);
	}

	@Override
	public long cost() {
		// TODO Auto-genedrated method stub
		return 0;
	}

	private Scorer countingConjunctionSumScorer(
			final List<Scorer> requiredScorers,
			final LearnToRankClause[] requiredClauses) throws IOException {
		// each scorer from the list counted as a single matcher
		final Scorer[] sco = new Scorer[requiredScorers.size()];
		for (int i = 0; i < sco.length; i++) {
			sco[i] = requiredScorers.get(i);
		}
		return new ConjunctionScorer(weight, sco, requiredClauses, docBase) {
			private float lastDocScore = Float.NaN;
			private int lastScoredDoc = -1;

			@Override
			public float score() throws IOException {
				final int doc = docID();
				if (doc >= lastScoredDoc && doc > lastScoredDoc) {
					lastDocScore = super.score();
					lastScoredDoc = doc;
				}
				return lastDocScore;
			}
		};
	}

	private Scorer countingDisjunctionSumScorer(final List<Scorer> scorers,
			final int minNrShouldMatch) throws IOException {
		// each scorer from the list counted as a single matcher

		return new DisjunctionSumScorer(weight, scorers, optionalClauses,
				docBase) {
			// Save the score of lastScoredDoc, so that we don't compute it more
			// than
			// once in score().
			private float lastDocScore = Float.NaN;
			private int lastScoredDoc = -1;

			@Override
			public float score() throws IOException {
				final int doc = docID();
				if (doc > lastScoredDoc) {
					lastDocScore = super.score();
					lastScoredDoc = doc;
				}
				return lastDocScore;
			}
		};
	}

	private Scorer createSumScorer() throws IOException {
		return (requiredScorers.size() == 0) ? makeCountingSumScorerNoReq()
				: makeCountingSumScorerSomeReq();
	}

	@Override
	public int docID() {
		return doc;
	}

	@Override
	public int freq() throws IOException {
		return countingSumScorer.freq();
	}

	@Override
	public Collection<ChildScorer> getChildren() {
		final ArrayList<ChildScorer> children = new ArrayList<ChildScorer>();
		for (final Scorer scorer : optionalScorers) {
			children.add(new ChildScorer(scorer, "SHOULD"));
		}
		return children;
	}

	/**
	 * Returns the scorer to be used for match counting and score summing. Uses
	 * requiredScorers, optionalScorers.
	 */

	private Scorer makeCountingSumScorerNoReq() throws IOException { // No //
																		// required
		// scorers
		// minNrShouldMatch optional scorers are required, but at least 1
		final int nrOptRequired = 1;
		Scorer reqCountSumScorer;
		if (optionalScorers.size() > nrOptRequired) {
			reqCountSumScorer = countingDisjunctionSumScorer(optionalScorers,
					nrOptRequired);
		} else {
			reqCountSumScorer = new SingleMatchScorer(optionalScorers.get(0),
					optionalClauses[0]);
		}
		return reqCountSumScorer;
	}

	private Scorer makeCountingSumScorerSomeReq() throws IOException {
		final Scorer reqCountSumScorer = requiredScorers.size() == 1 ? new SingleMatchScorer(
				requiredScorers.get(0), requiredClauses[0])
				: countingConjunctionSumScorer(requiredScorers, requiredClauses);

		if (optionalScorers.size() == 0) {
			return reqCountSumScorer;
		} else {
			return new ReqOptSumScorer(reqCountSumScorer,
					optionalScorers.size() == 1 ? new SingleMatchScorer(
							optionalScorers.get(0), optionalClauses[0])
							: countingDisjunctionSumScorer(optionalScorers, 1));
		}

	}

	@Override
	public int nextDoc() throws IOException {
		return doc = countingSumScorer.nextDoc();
	}

	@Override
	public float score() throws IOException {
		return countingSumScorer.score();
	}

	/**
	 * Scores and collects all matching documents.
	 * 
	 * @param collector
	 *            The collector to which all matching documents are passed
	 *            through.
	 */
//	@Override
//	public void score(final Collector collector) throws IOException {
//		collector.setScorer(this);
//		while ((doc = countingSumScorer.nextDoc()) != NO_MORE_DOCS) {
//			collector.collect(doc);
//		}
//	}
//
//	@Override
//	public boolean score(final Collector collector, final int max,
//			final int firstDocID) throws IOException {
//		doc = firstDocID;
//		collector.setScorer(this);
//		while (doc < max) {
//			collector.collect(doc);
//			doc = countingSumScorer.nextDoc();
//		}
//		return doc != NO_MORE_DOCS;
//	}
}

//
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.Collection;
// import java.util.List;
// import org.apache.lucene.search.Collector;
// import org.apache.lucene.search.Scorer;
// import org.apache.lucene.search.similarities.Similarity;
//
// import
// de.uop.code.disambiguation.ltr.lucene.query.LearnToRankQuery.LearnToRankWeight;
//
// /**
// * An alternative to BooleanScorer that also allows a minimum number of
// optional
// * scorers that should match. <br>
// * Implements skipTo(), and has no limitations on the numbers of added
// scorers. <br>
// * Uses ConjunctionScorer, AbstractDisjunctionScorer, ReqOptScorer and
// ReqExclScorer.
// */
// class LearnToRankScorer extends Scorer {
//
// private final List<Scorer> requiredScorers;
//
// private final List<Scorer> optionalScorers;
//
// private final LearnToRankClause[] optionalClauses;
//
// private final LearnToRankClause[] requiredClauses;
//
// private final int docBase;
//
// /**
// * The scorer to which all scoring will be delegated, except for computing
// * and using the coordination factor.
// */
// private final Scorer countingSumScorer;
//
// private int doc = -1;
//
// /**
// * Creates a {@link Scorer} with the given similarity and lists of required,
// * prohibited and optional scorers. In no required scorers are added, at
// * least one of the optional scorers will have to match during the search.
// *
// * @param weight
// * The BooleanWeight to be used.
// * @param disableCoord
// * If this parameter is true, coordination level matching (
// * {@link Similarity#coord(int, int)}) is not used.
// * @param minNrShouldMatch
// * The minimum number of optional added scorers that should match
// * during the search. In case no required scorers are added, at
// * least one of the optional scorers will have to match during
// * the search.
// * @param required
// * the list of required scorers.
// * @param prohibited
// * the list of prohibited scorers.
// * @param optional
// * the list of optional scorers.
// */
// public LearnToRankScorer(LearnToRankWeight weight, List<Scorer> optional,
// List<Scorer> required, LearnToRankClause[] optionalClauses,
// LearnToRankClause[] requiredClauses, int docBase)
// throws IOException {
// super(weight);
// this.docBase = docBase;
// this.optionalScorers = optional;
// this.requiredScorers = required;
// this.optionalClauses = optionalClauses;
// this.requiredClauses = requiredClauses;
// countingSumScorer = createSumScorer();
// }
//
//
// /** Count a scorer as a single match. */
// private class SingleMatchScorer extends Scorer {
// private Scorer scorer;
// private int lastScoredDoc = -1;
// // Save the score of lastScoredDoc, so that we don't compute it more
// // than
// // once in score().
// private float lastDocScore = Float.NaN;
//
// private LearnToRankClause clause;
//
// SingleMatchScorer(Scorer scorer, LearnToRankClause clause) {
// super(scorer.getWeight());
// this.scorer = scorer;
// this.clause = clause;
// }
//
// @Override
// public float score() throws IOException {
// int doc = docID();
// if (doc > lastScoredDoc) {
// lastDocScore = scorer.score();
// lastScoredDoc = doc;
// }
// float val = lastDocScore * clause.getWeight();
// clause.addFeatureValue(docBase, doc, val);
// return val;
// }
//
// @Override
// public int freq() throws IOException {
// return 1;
// }
//
// @Override
// public int docID() {
// return scorer.docID();
// }
//
// @Override
// public int nextDoc() throws IOException {
// return scorer.nextDoc();
// }
//
// @Override
// public int advance(int target) throws IOException {
// return scorer.advance(target);
// }
//
// @Override
// public long cost() {
// return 0;
// }
// }
//
// private Scorer createSumScorer() throws IOException {
// return (requiredScorers.size() == 0) ? makeCountingSumScorerNoReq()
// : makeCountingSumScorerSomeReq();
// }
//
// private Scorer countingDisjunctionSumScorer(final List<Scorer> scorers,
// int minNrShouldMatch) throws IOException {
// // each scorer from the list counted as a single matcher
// Scorer[] sco = new Scorer[scorers.size()];
// for (int i = 0; i < sco.length; i++) {
// sco[i] = scorers.get(i);
// }
//
// return new DisjunctionSumScorer(weight, sco,
// optionalClauses, docBase) {
// private int lastScoredDoc = -1;
// // Save the score of lastScoredDoc, so that we don't compute it more
// // than
// // once in score().
// private float lastDocScore = Float.NaN;
//
// @Override
// public float score() throws IOException {
// int doc = docID();
// if (doc > lastScoredDoc) {
// lastDocScore = super.score();
// lastScoredDoc = doc;
// }
// return lastDocScore;
// }
// };
// }
//
// private Scorer countingConjunctionSumScorer(List<Scorer> requiredScorers,
// LearnToRankClause[] requiredClauses) throws IOException {
// // each scorer from the list counted as a single matcher
// Scorer[] sco = new Scorer[requiredScorers.size()];
// for (int i = 0; i < sco.length; i++) {
// sco[i] = requiredScorers.get(i);
// }
// return new ConjunctionScorer(weight, sco,
// requiredClauses, docBase) {
// private int lastScoredDoc = -1;
// private float lastDocScore = Float.NaN;
//
// @Override
// public float score() throws IOException {
// int doc = docID();
// if (doc >= lastScoredDoc) {
// if (doc > lastScoredDoc) {
// lastDocScore = super.score();
// lastScoredDoc = doc;
// }
// }
// return lastDocScore;
// }
// };
// }
//
// /**
// * Returns the scorer to be used for match counting and score summing. Uses
// * requiredScorers, optionalScorers.
// */
//
// private Scorer makeCountingSumScorerNoReq() throws IOException { // No //
// required
// // scorers
// // minNrShouldMatch optional scorers are required, but at least 1
// int nrOptRequired = 1;
// Scorer requiredCountingSumScorer;
// if (optionalScorers.size() > nrOptRequired) {
// requiredCountingSumScorer = countingDisjunctionSumScorer(
// optionalScorers, nrOptRequired);
// } else {
// requiredCountingSumScorer = new SingleMatchScorer(
// optionalScorers.get(0), optionalClauses[0]);
// }
// return requiredCountingSumScorer;
// }
//
// private Scorer makeCountingSumScorerSomeReq() throws IOException {
// Scorer requiredCountingSumScorer = requiredScorers.size() == 1 ? new
// SingleMatchScorer(
// requiredScorers.get(0), requiredClauses[0])
// : countingConjunctionSumScorer(requiredScorers, requiredClauses);
//
// if (optionalScorers.size() == 0) {
// return requiredCountingSumScorer;
// } else {
// return new ReqOptSumScorer(requiredCountingSumScorer,
// optionalScorers.size() == 1 ? new SingleMatchScorer(
// optionalScorers.get(0), optionalClauses[0])
// : countingDisjunctionSumScorer(optionalScorers, 1));
// }
//
// }
//
// /**
// * Scores and collects all matching documents.
// *
// * @param collector
// * The collector to which all matching documents are passed
// * through.
// */
// @Override
// public void score(Collector collector) throws IOException {
// collector.setScorer(this);
// while ((doc = countingSumScorer.nextDoc()) != NO_MORE_DOCS) {
// collector.collect(doc);
// }
// }
//
// @Override
// public boolean score(Collector collector, int max, int firstDocID)
// throws IOException {
// doc = firstDocID;
// collector.setScorer(this);
// while (doc < max) {
// collector.collect(doc);
// doc = countingSumScorer.nextDoc();
// }
// return doc != NO_MORE_DOCS;
// }
//
// @Override
// public int docID() {
// return doc;
// }
//
// @Override
// public int nextDoc() throws IOException {
// return doc = countingSumScorer.nextDoc();
// }
//
// @Override
// public float score() throws IOException {
// float sum = countingSumScorer.score();
// return sum;
// }
//
// @Override
// public int freq() throws IOException {
// return countingSumScorer.freq();
// }
//
// @Override
// public int advance(int target) throws IOException {
// return doc = countingSumScorer.advance(target);
// }
//
// @Override
// public Collection<ChildScorer> getChildren() {
// ArrayList<ChildScorer> children = new ArrayList<ChildScorer>();
// for (Scorer s : optionalScorers) {
// children.add(new ChildScorer(s, "SHOULD"));
// }
// return children;
// }
//
// @Override
// public long cost() {
// // TODO Auto-generated method stub
// return 0;
// }

