package doser.entitydisambiguation.algorithms.rules;

import java.util.List;

import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.knowledgebases.AbstractKnowledgeBase;

abstract class AbstractRule {

	protected AbstractKnowledgeBase eckb;
	
	AbstractRule(AbstractKnowledgeBase eckb) {
		super();
		this.eckb = eckb;
	}
	
	abstract boolean applyRule(List<SurfaceForm> rep);
	
}
