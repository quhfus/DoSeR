package experiments.collective.entdoccentric.query;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;

import experiments.collective.entdoccentric.PriorLoader;
import experiments.collective.entdoccentric.StartupInformationLoader;


/**
 * Due to major performance problems if we use an IndexReader request for every
 * single document, we create a <Concept, Occurence> Hashmap to improve the
 * overall performance.
 * 
 * Our StartupInformationLoader provides these necessary information much
 * faster.
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class SensePriorQuery extends Query {

	private String keyword;

	public SensePriorQuery(String keyword) {
		super();
		this.keyword = keyword;
	}

	public Weight createWeight(IndexSearcher searcher) throws IOException {
		return new PriorWeight();
	}

	@Override
	public String toString(String field) {
		return "SensePriorQuery";
	}

	class PriorWeight extends Weight {

		@Override
		public Explanation explain(AtomicReaderContext context, int doc)
				throws IOException {
			return null;
		}

		@Override
		public Query getQuery() {
			return SensePriorQuery.this;
		}

		@Override
		public float getValueForNormalization() throws IOException {
			return 0;
		}

		@Override
		public void normalize(float norm, float topLevelBoost) {
		}

		@Override
		public Scorer scorer(AtomicReaderContext context,
				boolean scoreDocsInOrder, boolean topScorer, Bits acceptDocs)
				throws IOException {
			return new SensePriorScorer(this, context);
		}

		class SensePriorScorer extends Scorer {

			private int lastDoc = -1;

			private AtomicReaderContext context;

			SensePriorScorer(Weight weight, AtomicReaderContext context) {
				super(weight);
				this.context = context;
			}

			@Override
			public float score() throws IOException {
				return StartupInformationLoader.getSensePriorOfDocument(
						context.docBase + lastDoc, keyword);
			}

			@Override
			public int freq() throws IOException {
				return 1;
			}

			@Override
			public int docID() {
				return lastDoc;
			}

			@Override
			public int nextDoc() throws IOException {
				if ((context.reader().numDocs() - 1) > lastDoc) {
					return ++lastDoc;
				} else {
					return NO_MORE_DOCS;
				}
			}

			@Override
			public int advance(int target) throws IOException {
				int maxdoc = context.reader().numDocs();
				if (target > maxdoc - 1) {
					return NO_MORE_DOCS;
				}
				return lastDoc = target;
			}

			@Override
			public String toString() {
				return "SensePrior";
			}

		}

	}

}
