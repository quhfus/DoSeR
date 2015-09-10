package doser.entitydisambiguation.algorithms.collective.rules;

import java.util.ArrayList;
import java.util.List;

import doser.entitydisambiguation.algorithms.collective.CollectiveSFRepresentation;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;

public class RuleAdapation {

	private ArrayList<Rule> beforerules;
	
	private ArrayList<Rule> afterrules;
	
	public RuleAdapation(EntityCentricKnowledgeBaseDefault eckb) {
		super();
		this.beforerules = new ArrayList<Rule>();
		this.beforerules.add(new NoCandidatesCheckPlural(eckb));
		this.beforerules.add(new NoCandidatesExpansionRules(eckb));
		this.beforerules.add(new UnambiguousToAmbiguousRule(eckb));
	}

	public void performRuleChainBeforeCandidateSelection(List<CollectiveSFRepresentation> rep) {
		for(Rule r : beforerules) {
			r.applyRule(rep);
		}
	}
	
	public void performRuleChainAfterCandidateSelection(List<CollectiveSFRepresentation> rep) {
		for(Rule r : afterrules) {
			r.applyRule(rep);
		}
	}
}
