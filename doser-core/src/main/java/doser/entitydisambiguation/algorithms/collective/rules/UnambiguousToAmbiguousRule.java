package doser.entitydisambiguation.algorithms.collective.rules;

import java.util.LinkedList;
import java.util.List;

import doser.entitydisambiguation.algorithms.collective.SurfaceForm;
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
	public boolean applyRule(List<SurfaceForm> rep) {
		List<SurfaceForm> unambiguous = new LinkedList<SurfaceForm>();
		for (SurfaceForm c : rep) {
			if (c.getCandidates().size() == 1) {
				unambiguous.add(c);
			}
		}
		for (SurfaceForm c : rep) {
			if (c.getCandidates().size() > 1) {
				for (SurfaceForm un : unambiguous) {
					if (isSubString(un.getSurfaceForm(), c.getSurfaceForm())
							&& c.getCandidates().contains(
									un.getCandidates().get(0))) {
						c.setDisambiguatedEntity(un.getCandidates().get(0));
					}
				}
			}
		}
		return false;
	}

	private boolean isSubString(String s1, String s2) {
		if (s1.toLowerCase().contains(s2.toLowerCase())) {
			return true;
		} else
			return false;
	}
}
