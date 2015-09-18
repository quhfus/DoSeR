package doser.webclassify.algorithm;

import java.util.Map;

import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.webclassify.dpo.Paragraph;

public interface EntityRelevanceAlgorithm {

	public String process(Map<DisambiguatedEntity, Integer> map, Paragraph p);
	
}
