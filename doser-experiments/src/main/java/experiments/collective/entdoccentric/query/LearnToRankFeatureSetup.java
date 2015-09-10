package experiments.collective.entdoccentric.query;

import experiments.collective.entdoccentric.StandardQueryDataObject.EntityObject;
import experiments.collective.entdoccentric.LTR.LearnToRankQuery;
import experiments.collective.entdoccentric.dpo.EntityToDisambiguate;

public interface LearnToRankFeatureSetup {

	public void setMainQuery(LearnToRankQuery query);
	
	public void setSubQueries(EntityObject dataObject);
	
	public void setSubQueries(EntityToDisambiguate task);
}
