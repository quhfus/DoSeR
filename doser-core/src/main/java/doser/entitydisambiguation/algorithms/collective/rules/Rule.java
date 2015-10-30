package doser.entitydisambiguation.algorithms.collective.rules;

import java.util.List;

import doser.entitydisambiguation.algorithms.collective.hybrid.SurfaceForm;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;

public abstract class Rule {

	protected EntityCentricKnowledgeBaseDefault eckb;
	
	public Rule(EntityCentricKnowledgeBaseDefault eckb) {
		super();
		this.eckb = eckb;
	}
	
	public abstract boolean applyRule(List<SurfaceForm> rep);
	
}
