package doser.lucene.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;

public class LearnToRankQuery extends Query implements
		Iterable<LearnToRankClause>, Cloneable { // NOPMD by quh on 28.02.14
													// 11:00

	/**
	 * Expert: the Weight for LearnToRankQuery, used to normalize, score and
	 * explain these queries.
	 * 
	 * <p>
	 * NOTE: this API and implementation is subject to change suddenly in the
	 * next release.
	 * </p>
	 */
	public class LearnToRankWeight extends Weight {

		private final List<LearnToRankClause> optionalClauses;

		private final List<LearnToRankClause> requiredClauses;

		// private final boolean termConjunction;

		public LearnToRankWeight(final IndexSearcher searcher)
				throws IOException {
			// Check for required and optional weights
			final List<LearnToRankClause> requiredClauses = new LinkedList<LearnToRankClause>();
			final List<LearnToRankClause> optionalClauses = new LinkedList<LearnToRankClause>();

			// boolean termConjunction = true;
			for (int i = 0; i < clausesList.size(); i++) {
				final LearnToRankClause clause = clausesList.get(i);
				final Weight cweight = clause.getQuery().createWeight(searcher);
				if (clause.isMustOccur()) {
					requiredClauses.add(clause);
				} else {
					optionalClauses.add(clause);
				}
				// if (!(clause.isMustOccur() && w instanceof TermWeight)) {
				// termConjunction = false;
				// }
				clause.setW(cweight);
			}
			this.requiredClauses = requiredClauses;
			this.optionalClauses = optionalClauses;
			// this.termConjunction = termConjunction;
		}

		public float coord(final int overlap, final int maxOverlap) {
			// LUCENE-4300: in most cases of maxOverlap=1, BQ rewrites itself
			// away,
			// so coord() is not applied. But when BQ cannot optimize itself
			// away
			// for a single clause (minNrShouldMatch, prohibited clausesList,
			// etc),
			// its
			// important not to apply coord(1,1) for consistency, it might not
			// be 1.0F
			return maxOverlap == 1 ? 1F : (overlap / (float) maxOverlap);
		}

		//
		// private Scorer createConjunctionTermScorer(AtomicReaderContext
		// context,
		// Bits acceptDocs) throws IOException {
		//
		// // TODO: fix scorer API to specify "needsScores" up
		// // front, so we can do match-only if caller doesn't
		// // needs scores
		//
		// final DocsAndFreqs[] docsAndFreqs = new DocsAndFreqs[weights.length];
		// for (int i = 0; i < docsAndFreqs.length; i++) {
		// final TermWeight weight = (TermWeight) weights[i];
		// final Scorer scorer = weight.scorer(context, true, false,
		// acceptDocs);
		// if (scorer == null) {
		// return null;
		// } else {
		// assert scorer instanceof TermScorer;
		// docsAndFreqs[i] = new DocsAndFreqs(
		// (LearnToRankTermScorer) scorer);
		// }
		// }
		// return new ConjunctionTermScorer(this, coord(
		// docsAndFreqs.length, docsAndFreqs.length), docsAndFreqs);
		// }

		/**
		 * Explanation is not necessary to create a working LearnToRank Query.
		 * Method content will be created later! :-)
		 */
		@Override
		public Explanation explain(final AtomicReaderContext context,
				final int doc) throws IOException {
			return new Explanation();
		}

		@Override
		public Query getQuery() {
			return LearnToRankQuery.this;
		}

		/**
		 * Possibility to additionally boost the featureclauses with a weight w
		 * but this is not recommended! Boost should be one!
		 */
		@Override
		public float getValueForNormalization() throws IOException {
			return 1f;
		}

		@Override
		public void normalize(final float norm, final float topLevelBoost) {
			final float boost = topLevelBoost * getBoost();

			for (final LearnToRankClause learnToRankClause : requiredClauses) {
				final LearnToRankClause clause = learnToRankClause;
				clause.getW().normalize(norm, boost);
			}
			for (final LearnToRankClause learnToRankClause : optionalClauses) {
				final LearnToRankClause clause = learnToRankClause;
				clause.getW().normalize(norm, boost);
			}
		}

		@Override
		public Scorer scorer(AtomicReaderContext context, Bits acceptDocs)
				throws IOException {
			// if (termConjunction) {
			// Wird bewusst ausgeklammert um irgendwann durch Zufall
			// rauszufinden was hier genau passiert.
			// // specialized scorer for term conjunctions
			// return createConjunctionTermScorer(context, acceptDocs);
			// new Exception().printStackTrace();
			// }

			final List<Scorer> requiredScorer = new ArrayList<Scorer>();
			final List<Scorer> optionalScorer = new ArrayList<Scorer>();
			final List<LearnToRankClause> reqClausesWNull = new ArrayList<LearnToRankClause>();
			final List<LearnToRankClause> optClausesWNull = new ArrayList<LearnToRankClause>();

			for (final LearnToRankClause learnToRankClause : requiredClauses) {
				final LearnToRankClause clause = learnToRankClause;
				final Scorer subscorer = clause.getW().scorer(context, acceptDocs);
				if (subscorer == null) {
					return null;
				} else {
					requiredScorer.add(subscorer);
					reqClausesWNull.add(clause);
				}
			}

			for (final LearnToRankClause learnToRankClause : optionalClauses) {
				final LearnToRankClause clause = learnToRankClause;
				final Scorer subscorer = clause.getW().scorer(context, acceptDocs);
				if (subscorer != null) {
					optionalScorer.add(subscorer);
					optClausesWNull.add(clause);
				}
			}

			final LearnToRankClause[] reqClausesArr = new LearnToRankClause[reqClausesWNull
					.size()];
			final LearnToRankClause[] optClausesArr = new LearnToRankClause[optClausesWNull
					.size()];
			reqClausesWNull.toArray(reqClausesArr);
			optClausesWNull.toArray(optClausesArr);

			if ((reqClausesArr.length == 0) && (optClausesArr.length == 0)) {
				return null;
			}
			final Scorer scorer = new LearnToRankScorer(this, optionalScorer,
					requiredScorer, optClausesArr, reqClausesArr,
					context.docBase);
			return scorer;
		}
	}

	private ArrayList<LearnToRankClause> clausesList;

	public LearnToRankQuery() {
		clausesList = new ArrayList<LearnToRankClause>();
	}

	/**
	 * Adds a clause to a LearnToRank query.
	 */
	public LearnToRankClause add(final Query query, final String name,
			final boolean mustOccur) {
		final LearnToRankClause clause = new LearnToRankClause(query, name,
				mustOccur);
		clausesList.add(clause);
		return clause;
	}

	/** Returns the list of clausesList in this query. */
	public List<LearnToRankClause> clauses() {
		return clausesList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public LearnToRankQuery clone() {
		final LearnToRankQuery clone = (LearnToRankQuery) super.clone();
		clone.clausesList = (ArrayList<LearnToRankClause>) clausesList.clone();
		return clone;
	}

	public double[] createFeatureVector(final int docIndex) {
		final double[] result = new double[clausesList.size()];
		for (int i = 0; i < clausesList.size(); i++) {
			result[i] = clausesList.get(i).getFeatureValue(docIndex);
		}
		return result;
	}

	@Override
	public Weight createWeight(final IndexSearcher searcher) throws IOException {
		return new LearnToRankWeight(searcher);
	}

	/** Returns true iff <code>o</code> is equal to this. */
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof LearnToRankQuery)) {
			return false;
		}
		final LearnToRankQuery other = (LearnToRankQuery) obj;
		return (getBoost() == other.getBoost())
				&& clausesList.equals(other.clausesList);
	}

	@Override
	public void extractTerms(final Set<Term> terms) {
		// Not needed up to now
	}

	/** Returns the set of clausesList in this query. */
	public LearnToRankClause[] getClauses() {
		return clausesList.toArray(new LearnToRankClause[clausesList.size()]);
	}

	/** Returns a hash code value for this object. */
	@Override
	public int hashCode() {
		return Float.floatToIntBits(getBoost()) ^ clausesList.hashCode();
	}

	/**
	 * Returns an iterator on the clausesList in this query. It implements the
	 * {@link Iterable} interface to make it possible to do:
	 * 
	 * <pre class="prettyprint">
	 * for (FeatureClause clause : featureQuery) {
	 * }
	 * </pre>
	 */
	@Override
	public Iterator<LearnToRankClause> iterator() {
		return clauses().iterator();
	}

	/**
	 * Expert: called to re-write queries into primitive queries. For example, a
	 * PrefixQuery will be rewritten into a BooleanQuery that consists of
	 * TermQuerys.
	 * 
	 * But this method is not used or optimized in this class!
	 */
	@Override
	public Query rewrite(final IndexReader reader) throws IOException {
		for (int i = 0; i < clausesList.size(); i++) {
			final Query query = clausesList.get(i).getQuery().rewrite(reader);
			clausesList.get(i).setQuery(query);
		}
		return this;
	}

	@Override
	public String toString(final String field) {
		return "This is a Learn To Rank query whose string representation is not implemented.";
	}
}
