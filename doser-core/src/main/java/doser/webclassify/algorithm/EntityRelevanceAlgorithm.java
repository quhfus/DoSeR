package doser.webclassify.algorithm;

import java.util.Map;

import doser.entitydisambiguation.dpo.DisambiguatedEntity;

public interface EntityRelevanceAlgorithm {

	public String process(Map<DisambiguatedEntity, Integer> map);
	
}
