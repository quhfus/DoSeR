package doser.entitydisambiguation.algorithms.collective.general;

import doser.entitydisambiguation.algorithms.AbstractDisambiguationAlgorithm;
import doser.entitydisambiguation.algorithms.IllegalDisambiguationAlgorithmInputException;
import doser.entitydisambiguation.backend.AbstractDisambiguationTask;
import doser.entitydisambiguation.backend.DisambiguationTaskCollective;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBase;
import doser.entitydisambiguation.knowledgebases.AbstractKnowledgeBase;

public class CollectiveDisambiguationGeneralEntities extends AbstractDisambiguationAlgorithm {

	private EntityCentricKnowledgeBase eckb;
	
	@Override
	protected boolean checkAndSetInputParameter(AbstractDisambiguationTask task) {
		AbstractKnowledgeBase kb = task.getKb();
		if (!(task instanceof DisambiguationTaskCollective)) {
			return false;
		}
		
		this.eckb = (EntityCentricKnowledgeBase) kb;
		this.task = (DisambiguationTaskCollective) task;
		return true;
	}

	@Override
	protected void processAlgorithm() throws IllegalDisambiguationAlgorithmInputException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean preDisambiguation() {
		return true;
	}

}
