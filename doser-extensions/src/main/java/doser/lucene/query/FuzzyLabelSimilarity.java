package doser.lucene.query;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;

public class FuzzyLabelSimilarity extends Similarity { // NOPMD by quh on
														// 28.02.14 10:49

	/**
	 * Collection statistics for the TF-IDF model. The only statistic of
	 * interest to this model is idf.
	 */
	private static class IDFStats extends SimWeight {
		private final String field;
		/** The idf and its explanation */
		private final Explanation idf;
		private final float queryBoost;
		private float queryNorm;
		private float queryWeight;
		private float value;

		public IDFStats(final String field, final Explanation idf,
				final float queryBoost) {
			// TODO: Validate?
			this.field = field;
			this.idf = idf;
			this.queryBoost = queryBoost;
			// this.queryWeight = idf.getValue() * queryBoost;
			queryWeight = queryBoost;// compute query weight
		}

		@Override
		public float getValueForNormalization() {
			// TODO: (sorta LUCENE-1907) make non-static class and expose this
			// squaring via a nice method to subclasses?
			return queryWeight * queryWeight; // sum of squared
												// weights
		}

		@Override
		public void normalize(final float queryNorm, final float topLevelBoost) {
			this.queryNorm = queryNorm * topLevelBoost;
			queryWeight *= this.queryNorm; // normalize query weight
			value = queryWeight;// * idf.getValue(); // idf for
								// document
		}
	}

	private final class TFIDFSimScorer extends SimScorer {
		private final NumericDocValues norms;
		private final IDFStats stats;
		private final float weightValue;

		TFIDFSimScorer(final IDFStats stats, final NumericDocValues norms)
				throws IOException {
			this.stats = stats;
			weightValue = stats.value;
			this.norms = norms;
		}

		@Override
		public float computePayloadFactor(final int doc, final int start,
				final int end, final BytesRef payload) {
			return scorePayload(doc, start, end, payload);
		}

		@Override
		public float computeSlopFactor(final int distance) {
			return sloppyFreq(distance);
		}

		@Override
		public Explanation explain(final int doc, final Explanation freq) {
			return explainScore(doc, freq, stats, norms);
		}

		// ///////////////////////
		// Test
		// ////////////////////////
		@Override
		public float score(final int doc, final float freq) {
			final float raw = tf(freq) * weightValue; // compute tf(f)*weight

			return norms == null ? raw : raw * decodeNormValue(norms.get(doc)); // normalize
			// for
			// field
		}
	}

	/** Cache of decoded bytes. */
	private static final float[] NORM_TABLE = new float[256];

	static {
		for (int i = 0; i < 256; i++) {
			NORM_TABLE[i] = SmallFloat.byte315ToFloat((byte) i);
		}
	}

	/**
	 * True if overlap tokens (tokens with a position of increment of zero) are
	 * discounted from the document's length.
	 */
	private boolean discountOverlaps = true;

	@Override
	public final long computeNorm(final FieldInvertState state) {
		final float normValue = lengthNorm(state);
		return encodeNormValue(normValue);
	}

	@Override
	public final SimWeight computeWeight(final float queryBoost,
			final CollectionStatistics collectionStats,
			final TermStatistics... termStats) {
		final Explanation idf = termStats.length == 1 ? this.idfExplain(
				collectionStats, termStats[0]) : this.idfExplain(
				collectionStats, termStats);
		return new IDFStats(collectionStats.field(), idf, queryBoost);
	}

	/**
	 * Computes a score factor based on the fraction of all query terms that a
	 * document contains. This value is multiplied into scores.
	 * 
	 * <p>
	 * The presence of a large portion of the query terms indicates a better
	 * match with the query, so implementations of this method usually return
	 * larger values when the ratio between these parameters is large and
	 * smaller values when the ratio between them is small.
	 * 
	 * @param overlap
	 *            the number of query terms matched in the document
	 * @param maxOverlap
	 *            the total number of terms in the query
	 * @return a score factor based on term overlap with the query
	 */
	@Override
	public float coord(final int overlap, final int maxOverlap) {
		return overlap / (float) maxOverlap;
	}

	/**
	 * Decodes a normalization factor stored in an index.
	 * 
	 * @see #encodeNormValue(float)
	 */
	public final float decodeNormValue(final long norm) {
		return NORM_TABLE[(int) (norm & 0xFF)]; // & 0xFF maps negative bytes to
												// positive above 127
	}

	/** Encodes a normalization factor for storage in an index. */
	public final long encodeNormValue(final float floatVal) {
		return SmallFloat.floatToByte315(floatVal);
	}

	private Explanation explainScore(final int doc, final Explanation freq,
			final IDFStats stats, final NumericDocValues norms) {
		final Explanation result = new Explanation();
		result.setDescription("score(doc=" + doc + ",freq=" + freq
				+ "), product of:");

		// explain query weight
		final Explanation queryExpl = new Explanation();
		queryExpl.setDescription("queryWeight, product of:");

		final Explanation boostExpl = new Explanation(stats.queryBoost, "boost");
		if (stats.queryBoost != 1.0f) {
			queryExpl.addDetail(boostExpl);
		}
		queryExpl.addDetail(stats.idf);

		final Explanation queryNormExpl = new Explanation(stats.queryNorm,
				"queryNorm");
		queryExpl.addDetail(queryNormExpl);

		queryExpl.setValue(boostExpl.getValue() * stats.idf.getValue()
				* queryNormExpl.getValue());

		result.addDetail(queryExpl);

		// explain field weight
		final Explanation fieldExpl = new Explanation();
		fieldExpl.setDescription("fieldWeight in " + doc + ", product of:");

		final Explanation tfExplanation = new Explanation();
		tfExplanation.setValue(tf(freq.getValue()));
		tfExplanation.setDescription("tf(freq=" + freq.getValue()
				+ "), with freq of:");
		tfExplanation.addDetail(freq);
		fieldExpl.addDetail(tfExplanation);
		fieldExpl.addDetail(stats.idf);

		final Explanation fieldNormExpl = new Explanation();
		final float fieldNorm = norms != null ? decodeNormValue(norms.get(doc))
				: 1.0f;

		fieldNormExpl.setValue(fieldNorm);
		fieldNormExpl.setDescription("fieldNorm(doc=" + doc + ")");
		fieldExpl.addDetail(fieldNormExpl);

		fieldExpl.setValue(tfExplanation.getValue() * stats.idf.getValue()
				* fieldNormExpl.getValue());

		result.addDetail(fieldExpl);

		// combine them
		result.setValue(queryExpl.getValue() * fieldExpl.getValue());

		if (queryExpl.getValue() == 1.0f) {
			return fieldExpl;
		}

		return result;
	}

	/**
	 * Returns true if overlap tokens are discounted from the document's length.
	 * 
	 * @see #setDiscountOverlaps
	 */
	public boolean getDiscountOverlaps() { // NOPMD by quh on 28.02.14 10:49
		return discountOverlaps;
	}

	/**
	 * Computes a score factor based on a term's document frequency (the number
	 * of documents which contain the term). This value is multiplied by the
	 * {@link #tf(float)} factor for each term in the query and these products
	 * are then summed to form the initial score for a document.
	 * 
	 * <p>
	 * Terms that occur in fewer documents are better indicators of topic, so
	 * implementations of this method usually return larger values for rare
	 * terms, and smaller values for common terms.
	 * 
	 * @param docFreq
	 *            the number of documents which contain the term
	 * @param numDocs
	 *            the total number of documents in the collection
	 * @return a score factor based on the term's document frequency
	 */
	public float idf(final long docFreq, final long numDocs) {
		return (float) (Math.log(numDocs / (double) (docFreq + 1)) + 1.0);
	}

	/**
	 * Computes a score factor for a simple term and returns an explanation for
	 * that score factor.
	 * 
	 * <p>
	 * The default implementation uses:
	 * 
	 * <pre class="prettyprint">
	 * idf(docFreq, searcher.maxDoc());
	 * </pre>
	 * 
	 * Note that {@link CollectionStatistics#maxDoc()} is used instead of
	 * {@link org.apache.lucene.index.IndexReader#numDocs()
	 * IndexReader#numDocs()} because also {@link TermStatistics#docFreq()} is
	 * used, and when the latter is inaccurate, so is
	 * {@link CollectionStatistics#maxDoc()}, and in the same direction. In
	 * addition, {@link CollectionStatistics#maxDoc()} is more efficient to
	 * compute
	 * 
	 * @param collectionStats
	 *            collection-level statistics
	 * @param termStats
	 *            term-level statistics for the term
	 * @return an Explain object that includes both an idf score factor and an
	 *         explanation for the term.
	 */
	public Explanation idfExplain(final CollectionStatistics collectionStats,
			final TermStatistics termStats) {
		final long docfreq = termStats.docFreq();
		final long max = collectionStats.maxDoc();
		final float idf = idf(docfreq, max);
		return new Explanation(idf, "idf(docFreq=" + docfreq + ", maxDocs="
				+ max + ")");
	}

	/**
	 * Computes a score factor for a phrase.
	 * 
	 * <p>
	 * The default implementation sums the idf factor for each term in the
	 * phrase.
	 * 
	 * @param collectionStats
	 *            collection-level statistics
	 * @param termStats
	 *            term-level statistics for the terms in the phrase
	 * @return an Explain object that includes both an idf score factor for the
	 *         phrase and an explanation for each term.
	 */
	public Explanation idfExplain(final CollectionStatistics collectionStats,
			final TermStatistics termStats[]) {
		final long max = collectionStats.maxDoc();
		float idf = 0.0f;
		final Explanation exp = new Explanation();
		exp.setDescription("idf(), sum of:");
		for (final TermStatistics stat : termStats) {
			final long docFreq = stat.docFreq();
			final float termIdf = idf(docFreq, max);
			exp.addDetail(new Explanation(termIdf, "idf(docFreq=" + docFreq
					+ ", maxDocs=" + max + ")"));
			idf += termIdf;
		}
		exp.setValue(idf);
		return exp;
	}

	/**
	 * Compute an index-time normalization value for this field instance.
	 * <p>
	 * This value will be stored in a single byte lossy representation by
	 * {@link #encodeNormValue(float)}.
	 * 
	 * @param state
	 *            statistics of the current field (such as length, boost, etc)
	 * @return an index-time normalization value
	 */
	public float lengthNorm(final FieldInvertState state) {
		final int numTerms;
		if (discountOverlaps) {
			numTerms = state.getLength() - state.getNumOverlap();
		} else {
			numTerms = state.getLength();
		}
		return state.getBoost() * ((float) (1.0 / Math.sqrt(numTerms)));
	}

	/**
	 * Computes the normalization value for a query given the sum of the squared
	 * weights of each of the query terms. This value is multiplied into the
	 * weight of each query term. While the classic query normalization factor
	 * is computed as 1/sqrt(sumOfSquaredWeights), other implementations might
	 * completely ignore sumOfSquaredWeights (ie return 1).
	 * 
	 * <p>
	 * This does not affect ranking, but the default implementation does make
	 * scores from different queries more comparable than they would be by
	 * eliminating the magnitude of the Query vector as a factor in the score.
	 * 
	 * @param sumSquaredWeights
	 *            the sum of the squares of query term weights
	 * @return a normalization factor for query weights
	 */
	@Override
	public float queryNorm(final float sumSquaredWeights) {
		return (float) (1.0 / Math.sqrt(sumSquaredWeights));
	}

	/**
	 * Calculate a scoring factor based on the data in the payload.
	 * Implementations are responsible for interpreting what is in the payload.
	 * Lucene makes no assumptions about what is in the byte array.
	 * 
	 * @param doc
	 *            The docId currently being scored.
	 * @param start
	 *            The start position of the payload
	 * @param end
	 *            The end position of the payload
	 * @param payload
	 *            The payload byte array to be scored
	 * @return An implementation dependent float to be used as a scoring factor
	 */
	public float scorePayload(final int doc, final int start, final int end,
			final BytesRef payload) {
		return 1;
	}

	/**
	 * Determines whether overlap tokens (Tokens with 0 position increment) are
	 * ignored when computing norm. By default this is true, meaning overlap
	 * tokens do not count when computing norms.
	 * 
	 * @lucene.experimental
	 * 
	 * @see #computeNorm
	 */
	public void setDiscountOverlaps(final boolean bool) {
		discountOverlaps = bool;
	}

	@Override
	public final SimScorer simScorer(final SimWeight stats,
			final AtomicReaderContext context) throws IOException {
		final IDFStats idfstats = (IDFStats) stats;
		return new TFIDFSimScorer(idfstats, context.reader().getNormValues(
				idfstats.field));
	}

	/**
	 * Computes the amount of a sloppy phrase match, based on an edit distance.
	 * This value is summed for each sloppy phrase match in a document to form
	 * the frequency to be used in scoring instead of the exact term count.
	 * 
	 * <p>
	 * A phrase match with a small edit distance to a document passage more
	 * closely matches the document, so implementations of this method usually
	 * return larger values when the edit distance is small and smaller values
	 * when it is large.
	 * 
	 * @see PhraseQuery#setSlop(int)
	 * @param distance
	 *            the edit distance of this sloppy phrase match
	 * @return the frequency increment for this match
	 */
	public float sloppyFreq(final int distance) {
		return 1.0f / (distance + 1);
	}

	/**
	 * Computes a score factor based on a term or phrase's frequency in a
	 * document. This value is multiplied by the {@link #idf(long, long)} factor
	 * for each term in the query and these products are then summed to form the
	 * initial score for a document.
	 * 
	 * <p>
	 * Terms and phrases repeated in a document indicate the topic of the
	 * document, so implementations of this method usually return larger values
	 * when <code>freq</code> is large, and smaller values when
	 * <code>freq</code> is small.
	 * 
	 * @param freq
	 *            the frequency of a term within a document
	 * @return a score factor based on a term's within-document frequency
	 */
	public float tf(final float freq) { // NOPMD by quh on 28.02.14 10:49
		return (float) Math.sqrt(freq);
	}

	@Override
	public String toString() {
		return "DefaultSimilarity";
	}
}