package experiments.collective.entdoccentric.LTR;

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

import experiments.collective.entdoccentric.LTR.LearnToRankTermQuery.TermWeight;

public class LearnToRankQuery extends Query implements
		Iterable<LearnToRankClause> {

	private ArrayList<LearnToRankClause> clauses;

	public LearnToRankQuery() {
		clauses = new ArrayList<LearnToRankClause>();
	}

	@Override
	public String toString(String field) {
		return new String(
				"This is a Learn To Rank query whose string representation is not implemented.");
	}

	/**
	 * Returns an iterator on the clauses in this query. It implements the
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

	/** Returns the list of clauses in this query. */
	public List<LearnToRankClause> clauses() {
		return clauses;
	}

	/**
	 * Adds a clause to a LearnToRank query.
	 */
	public LearnToRankClause add(Query query, String name, boolean mustOccur) {
		LearnToRankClause clause = new LearnToRankClause(query, name, mustOccur);
		clauses.add(clause);
		return clause;
	}

	/** Returns the set of clauses in this query. */
	public LearnToRankClause[] getClauses() {
		return clauses.toArray(new LearnToRankClause[clauses.size()]);
	}

	@Override
	public Weight createWeight(IndexSearcher searcher) throws IOException {
		return new LearnToRankWeight(searcher);
	}

	public double[] createFeatureVector(int docIndex) {
		double[] result = new double[clauses.size()];
		for (int i = 0; i < clauses.size(); i++) {
			result[i] = clauses.get(i).getFeatureValue(docIndex);
		}
		return result;
	}

	/**
	 * Expert: called to re-write queries into primitive queries. For example, a
	 * PrefixQuery will be rewritten into a BooleanQuery that consists of
	 * TermQuerys.
	 * 
	 * But this method is not used or optimized in this class!
	 */
	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		for (int i = 0; i < clauses.size(); i++) {
			Query query = clauses.
					get(i)
					.getQuery()
					.rewrite(reader);
			clauses.get(i).setQuery(query);
		}
		return this;
	}

	@Override
	public void extractTerms(Set<Term> terms) {
		// Not needed up to now
	}

	@Override
	@SuppressWarnings("unchecked")
	public LearnToRankQuery clone() {
		LearnToRankQuery clone = (LearnToRankQuery) super.clone();
		clone.clauses = (ArrayList<LearnToRankClause>) this.clauses.clone();
		return clone;
	}

	/** Returns true iff <code>o</code> is equal to this. */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LearnToRankQuery)) {
			return false;
		}
		LearnToRankQuery other = (LearnToRankQuery) o;
		return (this.getBoost() == other.getBoost())
				&& this.clauses.equals(other.clauses);
	}

	/** Returns a hash code value for this object. */
	@Override
	public int hashCode() {
		return Float.floatToIntBits(getBoost()) ^ clauses.hashCode();
	}

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

		private List<LearnToRankClause> requiredClauses;

		private List<LearnToRankClause> optionalClauses;
		
		private Weight[] weights;

		private final boolean termConjunction;

		public LearnToRankWeight(IndexSearcher searcher) throws IOException {
			// Check for required and optional weights
			List<LearnToRankClause> requiredClauses = new LinkedList<LearnToRankClause>();
			List<LearnToRankClause> optionalClauses = new LinkedList<LearnToRankClause>();
			weights = new Weight[clauses.size()];
			
			boolean termConjunction = true;
			for (int i = 0; i < clauses.size(); i++) {
				LearnToRankClause clause = clauses.get(i);
				Weight w = clause.getQuery().createWeight(searcher);
				if (clause.isMustOccur()) {
					requiredClauses.add(clause);
				} else {
					optionalClauses.add(clause);
				}
				if (!(clause.isMustOccur() && w instanceof TermWeight)) {
					termConjunction = false;
				}
				weights[i] = w;
				clause.setW(w);
			}
			this.requiredClauses = requiredClauses;
			this.optionalClauses = optionalClauses;
			this.termConjunction = termConjunction;
		}

		/**
		 * Explanation is not necessary to create a working LearnToRank Query.
		 * Method content will be created later! :-)
		 */
		@Override
		public Explanation explain(AtomicReaderContext context, int doc)
				throws IOException {
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
		public void normalize(float norm, float topLevelBoost) {
			topLevelBoost *= getBoost(); // incorporate boost
			for (Iterator<LearnToRankClause> iterator = requiredClauses
					.iterator(); iterator.hasNext();) {
				LearnToRankClause clause = (LearnToRankClause) iterator.next();
				clause.getW().normalize(norm, topLevelBoost);
			}
			for (Iterator<LearnToRankClause> iterator = optionalClauses
					.iterator(); iterator.hasNext();) {
				LearnToRankClause clause = (LearnToRankClause) iterator.next();
				clause.getW().normalize(norm, topLevelBoost);
			}
		}

		@Override
		public Scorer scorer(AtomicReaderContext context,
				boolean scoreDocsInOrder, boolean topScorer, Bits acceptDocs)
				throws IOException {
			if (termConjunction) {
				// Wird bewusst ausgeklammert um irgendwann durch Zufall rauszufinden was hier genau passiert.
//				// specialized scorer for term conjunctions
//				return createConjunctionTermScorer(context, acceptDocs);
//				new Exception().printStackTrace();
			}

			List<Scorer> requiredScorer = new ArrayList<Scorer>();
			List<Scorer> optionalScorer = new ArrayList<Scorer>();
			List<LearnToRankClause> requiredClausesWithoutNull = new ArrayList<LearnToRankClause>();
			List<LearnToRankClause> optionalClausesWithoutNull = new ArrayList<LearnToRankClause>();
			
			for (Iterator<LearnToRankClause> iterator = requiredClauses
					.iterator(); iterator.hasNext();) {
				LearnToRankClause clause = (LearnToRankClause) iterator.next();
				Scorer subscorer = clause.getW().scorer(context, true, false,
						acceptDocs);
				if (subscorer != null) {
					requiredScorer.add(subscorer);
					requiredClausesWithoutNull.add(clause);
				} else {
					return null;
				}
			}

			for (Iterator<LearnToRankClause> iterator = optionalClauses
					.iterator(); iterator.hasNext();) {
				LearnToRankClause clause = (LearnToRankClause) iterator.next();
				Scorer subscorer = clause.getW().scorer(context, true, false,
						acceptDocs);
				if (subscorer != null) {
					optionalScorer.add(subscorer);
					optionalClausesWithoutNull.add(clause);
				}
			}

			LearnToRankClause[] requiredClausesArr = new LearnToRankClause[requiredClausesWithoutNull
					.size()];
			LearnToRankClause[] optionalClausesArr = new LearnToRankClause[optionalClausesWithoutNull
					.size()];
			requiredClausesWithoutNull.toArray(requiredClausesArr);
			optionalClausesWithoutNull.toArray(optionalClausesArr);

			if (requiredClausesArr.length == 0
					&& optionalClausesArr.length == 0) {
				return null;
			}
			Scorer scorer = new LearnToRankScorer(this, optionalScorer, requiredScorer,
					optionalClausesArr, requiredClausesArr, context.docBase);
			return scorer;
		}

		public float coord(int overlap, int maxOverlap) {
			// LUCENE-4300: in most cases of maxOverlap=1, BQ rewrites itself
			// away,
			// so coord() is not applied. But when BQ cannot optimize itself
			// away
			// for a single clause (minNrShouldMatch, prohibited clauses, etc),
			// its
			// important not to apply coord(1,1) for consistency, it might not
			// be 1.0F
			return maxOverlap == 1 ? 1F : (overlap / (float) maxOverlap);
		}
//
//		private Scorer createConjunctionTermScorer(AtomicReaderContext context,
//				Bits acceptDocs) throws IOException {
//
//			// TODO: fix scorer API to specify "needsScores" up
//			// front, so we can do match-only if caller doesn't
//			// needs scores
//
//			final DocsAndFreqs[] docsAndFreqs = new DocsAndFreqs[weights.length];
//			for (int i = 0; i < docsAndFreqs.length; i++) {
//				final TermWeight weight = (TermWeight) weights[i];
//				final Scorer scorer = weight.scorer(context, true, false,
//						acceptDocs);
//				if (scorer == null) {
//					return null;
//				} else {
//					assert scorer instanceof TermScorer;
//					docsAndFreqs[i] = new DocsAndFreqs(
//							(LearnToRankTermScorer) scorer);
//				}
//			}
//			return new ConjunctionTermScorer(this, coord(
//					docsAndFreqs.length, docsAndFreqs.length), docsAndFreqs);
//		}
	}
}
