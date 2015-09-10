package doser.lucene.query;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.ReaderUtil;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.TermState;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ComplexExplanation;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.SimScorer;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.ToStringUtils;

/**
 * A Query that matches documents containing a term. This may be combined with
 * other terms with a {@link BooleanQuery}.
 */
public class TermQuery extends Query {
	final class TermWeight extends Weight {
		private final Similarity similarity;
		private final Similarity.SimWeight stats;
		private final TermContext termStates;

		public TermWeight(final IndexSearcher searcher,
				final TermContext termStates) throws IOException {
			assert termStates != null : "TermContext must not be null";
			this.termStates = termStates;
			similarity = searcher.getSimilarity();
			stats = similarity.computeWeight(getBoost(),
					searcher.collectionStatistics(term.field()),
					searcher.termStatistics(term, termStates));
		}

		@Override
		public Explanation explain(final AtomicReaderContext context,
				final int doc) throws IOException {
			final Scorer scorer = scorer(context, context.reader()
					.getLiveDocs());
			if (scorer != null) {
				final int newDoc = scorer.advance(doc);
				if (newDoc == doc) {
					final float freq = scorer.freq();
					final SimScorer docScorer = similarity.simScorer(stats,
							context);
					final ComplexExplanation result = new ComplexExplanation();
					result.setDescription("weight(" + getQuery() + " in " + doc
							+ ") [" + similarity.getClass().getSimpleName()
							+ "], result of:");
					final Explanation scoreExplanation = docScorer.explain(doc,
							new Explanation(freq, "termFreq=" + freq));
					result.addDetail(scoreExplanation);
					result.setValue(scoreExplanation.getValue());
					result.setMatch(true);
					return result;
				}
			}
			return new ComplexExplanation(false, 0.0f, "no matching term");
		}

		@Override
		public Query getQuery() {
			return TermQuery.this;
		}

		/**
		 * Returns a {@link TermsEnum} positioned at this weights Term or null
		 * if the term does not exist in the given context
		 */
		private TermsEnum getTermsEnum(final AtomicReaderContext context)
				throws IOException {
			final TermState state = termStates.get(context.ord);
			if (state == null) { // term is not present in that reader
				assert termNotInReader(context.reader(), term) : "no termstate found but term exists in reader term="
						+ term;
				return null;
			}
			// System.out.println("LD=" + reader.getLiveDocs() + " set?=" +
			// (reader.getLiveDocs() != null ? reader.getLiveDocs().get(0) :
			// "null"));
			final TermsEnum termsEnum = context.reader().terms(term.field())
					.iterator(null);
			termsEnum.seekExact(term.bytes(), state);
			return termsEnum;
		}

		@Override
		public float getValueForNormalization() {
			return stats.getValueForNormalization();
		}

		@Override
		public void normalize(final float queryNorm, final float topLevelBoost) {
			stats.normalize(queryNorm, topLevelBoost);
		}

		private boolean termNotInReader(final AtomicReader reader,
				final Term term) throws IOException {
			// only called from assert
			// System.out.println("TQ.termNotInReader reader=" + reader +
			// " term=" + field + ":" + bytes.utf8ToString());
			return reader.docFreq(term) == 0;
		}

		@Override
		public String toString() {
			return "weight(" + TermQuery.this + ")";
		}

		@Override
		public Scorer scorer(AtomicReaderContext context, Bits acceptDocs)
				throws IOException {
			assert termStates.topReaderContext == ReaderUtil
					.getTopLevelContext(context) : "The top-reader used to create Weight ("
					+ termStates.topReaderContext
					+ ") is not the same as the current reader's top-reader ("
					+ ReaderUtil.getTopLevelContext(context);
			final TermsEnum termsEnum = getTermsEnum(context);
			if (termsEnum == null) {
				return null;
			}
			final DocsEnum docs = termsEnum.docs(acceptDocs, null);
			assert docs != null;
			return new TermScorer(this, docs, similarity.simScorer(stats,
					context));
		}
	}

	private final int docFreq;
	private final TermContext perReaderTermS;

	private final Term term;

	/** Constructs a query for the term <code>t</code>. */
	public TermQuery(final Term term) {
		this(term, -1);
	}

	/**
	 * Expert: constructs a TermQuery that will use the provided docFreq instead
	 * of looking up the docFreq against the searcher.
	 */
	public TermQuery(final Term term, final int docFreq) {
		this.term = term;
		this.docFreq = docFreq;
		perReaderTermS = null;
	}

	/**
	 * Expert: constructs a TermQuery that will use the provided docFreq instead
	 * of looking up the docFreq against the searcher.
	 */
	public TermQuery(final Term term, final TermContext states) {
		assert states != null;
		this.term = term;
		docFreq = states.docFreq();
		perReaderTermS = states;
	}

	@Override
	public Weight createWeight(final IndexSearcher searcher) throws IOException {
		final IndexReaderContext context = searcher.getTopReaderContext();
		final TermContext termState;
		if ((perReaderTermS == null)
				|| (perReaderTermS.topReaderContext != context)) {
			// make TermQuery single-pass if we don't have a PRTS or if the
			// context differs!
			termState = TermContext.build(context, term);
		} else {
			// PRTS was pre-build for this IS
			termState = perReaderTermS;
		}

		// we must not ignore the given docFreq - if set use the given value
		// (lie)
		if (docFreq != -1) {
			termState.setDocFreq(docFreq);
		}

		return new TermWeight(searcher, termState);
	}

	/** Returns true iff <code>o</code> is equal to this. */
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof TermQuery)) {
			return false;
		}
		final TermQuery other = (TermQuery) obj;
		return (getBoost() == other.getBoost()) && term.equals(other.term);
	}

	@Override
	public void extractTerms(final Set<Term> terms) {
		terms.add(getTerm());
	}

	/** Returns the term of this query. */
	public Term getTerm() {
		return term;
	}

	/** Returns a hash code value for this object. */
	@Override
	public int hashCode() {
		return Float.floatToIntBits(getBoost()) ^ term.hashCode();
	}

	/** Prints a user-readable version of this query. */
	@Override
	public String toString(final String field) {
		final StringBuilder buffer = new StringBuilder();
		if (!term.field().equals(field)) {
			buffer.append(term.field());
			buffer.append(':');
		}
		buffer.append(term.text());
		buffer.append(ToStringUtils.boost(getBoost()));
		return buffer.toString();
	}

}