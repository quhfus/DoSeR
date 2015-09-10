package doser.entitydisambiguation.algorithms.collective.rules;

import java.util.List;
import java.util.Set;

import doser.entitydisambiguation.algorithms.collective.CollectiveSFRepresentation;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;

public class FactRule extends Rule {

	public FactRule(EntityCentricKnowledgeBaseDefault eckb) {
		super(eckb);
	}

	@Override
	public boolean applyRule(List<CollectiveSFRepresentation> rep) {
		System.out.println("Fact Rule Applying");
		for (CollectiveSFRepresentation col : rep) {
			if (col.getCandidates().size() == 1) {
				String url = col.getCandidates().get(0);
				Set<String> set = eckb.getFeatureDefinition().getRelations(url);
				System.out.println("Set For : "+ url +" "+set.toString());
				for(CollectiveSFRepresentation col1 : rep) {
					if(col1.getCandidates().size() > 1) {
						String s = checkRelationsToSFRepresentation(set, col1);
						if(s != null) {
							col1.setDisambiguatedEntity(s);
							System.out.println("ICH SETZE DIE ENTITÄT: "+s);
						}
					}
				}
			}
		}
		return false;
	}

	private String checkRelationsToSFRepresentation(Set<String> relations,
			CollectiveSFRepresentation rep) {
		List<String> l = rep.getCandidates();
		String result = null;
		for(String rel : relations) {
			for(String can : l) {
				if(rel.equalsIgnoreCase(can.replaceAll("http://dbpedia.org/resource/", ""))) {
					result = can;
					break;
				}
			}
		}
		return result;
	}

}
