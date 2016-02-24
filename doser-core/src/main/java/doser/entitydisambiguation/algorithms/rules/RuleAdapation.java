package doser.entitydisambiguation.algorithms.rules;

import java.util.ArrayList;
import java.util.List;

import doser.entitydisambiguation.knowledgebases.EntityCentricKBDBpedia;
import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.knowledgebases.AbstractKnowledgeBase;

public class RuleAdapation {

	private List<AbstractRule> ruleChain;
	
	public RuleAdapation() {
		super();
		this.ruleChain = new ArrayList<AbstractRule>();
	}
	
	public void addNoCandidatesCheckPluralRule(AbstractKnowledgeBase eckb) {
		this.ruleChain.add(new NoCandidatesCheckPlural(eckb));
	}
	
	public void addNoCandidatesExpansionRule(AbstractKnowledgeBase eckb) {
		this.ruleChain.add(new NoCandidatesExpansionRules(eckb));
	}
	
	public void addUnambiguousToAmbiguousRule(EntityCentricKBDBpedia eckb) {
		this.ruleChain.add(new UnambiguousToAmbiguousRule(eckb));
	}
	
	public void addPatternRule(EntityCentricKBDBpedia eckb, String topic) {
		if (topic != null) {
			this.ruleChain.add(new PatternRule(eckb));
		}
	}
	
	public void addContextRule(EntityCentricKBDBpedia eckb) {
		this.ruleChain.add(new ContextRule(eckb));
	}

	public void performRuleChainBeforeCandidateSelection(List<SurfaceForm> rep) {
		for (AbstractRule r : ruleChain) {
			r.applyRule(rep);
		}
	}
}
