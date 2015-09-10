package doser.lucene.query;

import java.io.IOException;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Weight;

public class LTRBooleanQuery extends BooleanQuery {

	public class LTRBooleanWeight extends BooleanWeight {

		public LTRBooleanWeight(final IndexSearcher searcher,
				final boolean disableCoord) throws IOException {
			super(searcher, disableCoord);
		}

		@Override
		public float coord(final int overlap, final int maxOverlap) {
			// return 1.0f;
			return maxOverlap == 1 ? 1F : similarity.coord(overlap, maxOverlap);
		}
	}

	public LTRBooleanQuery() {
		super();
	}

	public LTRBooleanQuery(final boolean bool) {
		super(bool);
	}

	@Override
	public Weight createWeight(final IndexSearcher searcher) throws IOException {
		return new LTRBooleanWeight(searcher, isCoordDisabled());
	}
}
