package doser.entitydisambiguation.algorithms.collective.rules;

import java.util.LinkedList;
import java.util.List;

import doser.entitydisambiguation.algorithms.collective.CollectiveSFRepresentation;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;

/**
 * Falls eine Surface Form eindeutig ist und weitere Surface Forms eine
 * Abkürzung darstellen, diese allerdings nicht eindeutig sind, wird dies sofort
 * aufgelöst.
 * 
 * Beispiel: 
 * 1 Surface Form: Burlington Industries Inc (eindeutig) 
 * 2 Surface Form: Burlington (ambiguous) ...
 * 
 * 
 * @author quh
 *
 */

public class UnambiguousToAmbiguousRule extends Rule {

	public static final int STRINGLENGTH = 4;

	public UnambiguousToAmbiguousRule(EntityCentricKnowledgeBaseDefault eckb) {
		super(eckb);
	}
	
	@Override
	public boolean applyRule(List<CollectiveSFRepresentation> rep) {
		List<CollectiveSFRepresentation> unambiguous = new LinkedList<CollectiveSFRepresentation>();
		for (CollectiveSFRepresentation c : rep) {
			if (c.getCandidates().size() == 1) {
				unambiguous.add(c);
			}
		}
		for (CollectiveSFRepresentation c : rep) {
			String sf = c.getSurfaceForm();
			for (CollectiveSFRepresentation un : unambiguous) {
				if(isSubString(un.getSurfaceForm(), sf)) {
					c.setDisambiguatedEntity(un.getCandidates().get(0));
				}
			}
		}
		
//		for (CollectiveSFRepresentation c : rep) {
//			if (c.getCandidates().size() > 1) {
//				for (CollectiveSFRepresentation un : unambiguous) {
//					if (isSubString(un.getSurfaceForm(), c.getSurfaceForm())
//							&& c.getCandidates().contains(
//									un.getCandidates().get(0))) {
//						c.setDisambiguatedEntity(un.getCandidates().get(0));
//					}
//				}
//			}
//		}
		return false;
	}

	private boolean isSubString(String s1, String s2) {
		if (s1.toLowerCase().contains(s2.toLowerCase())) {
			return true;
		} else
			return false;
	}
}
