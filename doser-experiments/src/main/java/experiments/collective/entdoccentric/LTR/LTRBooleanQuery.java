package experiments.collective.entdoccentric.LTR;

import java.io.IOException;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Weight;

public class LTRBooleanQuery extends BooleanQuery {

	public LTRBooleanQuery(boolean b) {
		super(b);
	}
	
	public LTRBooleanQuery() {
		super();
	}

	@Override
	public Weight createWeight(IndexSearcher searcher) throws IOException {
		return new LTRBooleanWeight(searcher, isCoordDisabled());
	}

	public class LTRBooleanWeight extends BooleanWeight {

		public LTRBooleanWeight(IndexSearcher searcher, boolean disableCoord)
				throws IOException {
			super(searcher, disableCoord);
		}

		@Override
		public float coord(int overlap, int maxOverlap) {
//			return 1.0f;
			return maxOverlap == 1 ? 1F : similarity.coord(overlap, maxOverlap);
		}
	}
}
