package experiments.collective.entdoccentric.query;

import org.apache.lucene.search.Query;

import experiments.collective.entdoccentric.StandardInitialize;
import experiments.collective.entdoccentric.StandardQueryDataObject.EntityObject;


public final class QueryGenerator {

	private static QueryGenerator instance;

	private LearnToRankInitialize ltr;
	
	private StandardInitialize std;

	private QueryGenerator() {
		ltr = new LearnToRankInitialize();
		std = new StandardInitialize();
	}

	public synchronized static QueryGenerator getInstance() {
		if (instance == null) {
			instance = new QueryGenerator();
		}
		return instance;
	}

	public Query createQuery(EntityObject object,
			QuerySettings settings) {
		Query query = null;
		if (settings.getQuery().equals("std")) {
			query = std.createQuery(object, settings);
		} else if (settings.getQuery().equals("ltr")) {
			query = ltr.createLearnToRankQuery(object, settings);
		}
		return query;
	}
}