package doser.lucene.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.ArrayUtil;

/** Scorer for conjunctions, sets of queries, all of which are required. */
class ConjunctionScorer extends Scorer {
	static final class DocsAndFreqs {
		final long cost;
		int doc = -1;
		final Scorer scorer;

		DocsAndFreqs(final Scorer scorer) {
			this.scorer = scorer;
			cost = scorer.cost();
		}
	}

	private final LearnToRankClause[] clauses;
	private final float coord;
	private final int docBase;
	protected final DocsAndFreqs[] docsAndFreqs;
	protected int lastDoc = -1;

	private final DocsAndFreqs lead;

	ConjunctionScorer(final Weight weight, final Scorer[] scorers,
			final float coord, final LearnToRankClause[] ltrclauses,
			final int docBase) {
		super(weight);
		this.coord = coord;
		this.docBase = docBase;
		clauses = ltrclauses;
		docsAndFreqs = new DocsAndFreqs[scorers.length];
		for (int i = 0; i < scorers.length; i++) {
			docsAndFreqs[i] = new DocsAndFreqs(scorers[i]);
		}
		// Sort the array the first time to allow the least frequent DocsEnum to
		// lead the matching.
		ArrayUtil.timSort(docsAndFreqs, new Comparator<DocsAndFreqs>() {
			@Override
			public int compare(final DocsAndFreqs obj1, final DocsAndFreqs obj2) {
				return Long.signum(obj1.cost - obj2.cost);
			}
		});

		lead = docsAndFreqs[0]; // least frequent DocsEnum leads the
								// intersection
	}

	ConjunctionScorer(final Weight weight, final Scorer[] scorers,
			final LearnToRankClause[] ltrclauses, final int docBase) {
		this(weight, scorers, 1f, ltrclauses, docBase);
	}

	@Override
	public int advance(final int target) throws IOException {
		lead.doc = lead.scorer.advance(target);
		return lastDoc = doNext(lead.doc);
	}

	@Override
	public long cost() {
		return lead.scorer.cost();
	}

	@Override
	public int docID() {
		return lastDoc;
	}

	private int doNext(int doc) throws IOException { // NOPMD by quh on 28.02.14
														// 10:45
		for (;;) {
			// doc may already be NO_MORE_DOCS here, but we don't check
			// explicitly
			// since all scorers should advance to NO_MORE_DOCS, match, then
			// return that value.
			advanceHead: for (;;) {
				for (int i = 1; i < docsAndFreqs.length; i++) {
					// invariant: docsAndFreqs[i].doc <= doc at this point.

					// docsAndFreqs[i].doc may already be equal to doc if we
					// "broke advanceHead"
					// on the previous iteration and the advance on the lead
					// scorer exactly matched.
					if (docsAndFreqs[i].doc < doc) {
						docsAndFreqs[i].doc = docsAndFreqs[i].scorer
								.advance(doc);

						if (docsAndFreqs[i].doc > doc) {
							// DocsEnum beyond the current doc - break and
							// advance lead to the new highest doc.
							doc = docsAndFreqs[i].doc;
							break advanceHead;
						}
					}
				}
				// success - all DocsEnums are on the same doc
				return doc;
			}
			// advance head for next iteration
			doc = lead.doc = lead.scorer.advance(doc);
		}
	}

	@Override
	public int freq() {
		return docsAndFreqs.length;
	}

	@Override
	public Collection<ChildScorer> getChildren() {
		final ArrayList<ChildScorer> children = new ArrayList<ChildScorer>(
				docsAndFreqs.length);
		for (final DocsAndFreqs docs : docsAndFreqs) {
			children.add(new ChildScorer(docs.scorer, "MUST"));
		}
		return children;
	}

	@Override
	public int nextDoc() throws IOException {
		lead.doc = lead.scorer.nextDoc();
		return lastDoc = doNext(lead.doc);
	}

	@Override
	public float score() throws IOException {
		// TODO: sum into a double and cast to float if we ever send required
		// clauses to BS1
		float sum = 0.0f;
		for (int i = 0; i < docsAndFreqs.length; i++) {
			final float val = docsAndFreqs[i].scorer.score()
					* clauses[i].getWeight();
			sum += val;
			clauses[i].addFeatureValue(docBase, lastDoc, val);
		}
		return sum * coord;
	}
}