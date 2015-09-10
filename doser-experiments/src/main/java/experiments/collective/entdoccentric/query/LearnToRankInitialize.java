package experiments.collective.entdoccentric.query;

import org.apache.lucene.search.Query;

import experiments.collective.entdoccentric.StandardQueryDataObject.EntityObject;
import experiments.collective.entdoccentric.LTR.LearnToRankQuery;

public class LearnToRankInitialize {

	public Query createLearnToRankQuery(EntityObject object,
			QuerySettings settings) {
		LearnToRankFeatureSetup setup = null;
		if (settings.isDocumentcentric()) {
			setup = new LearnToRankFeatureSetupDocumentCentric();
		} else {
			setup = new LearnToRankFeatureSetupEntityBased();
		}
		LearnToRankQuery query = new LearnToRankQuery();
		setup.setMainQuery(query);
		setup.setSubQueries(object);
		return query;
	}
}
