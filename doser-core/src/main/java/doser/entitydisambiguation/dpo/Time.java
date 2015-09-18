package doser.entitydisambiguation.dpo;

import java.util.List;

public class Time {

	private String mention;
	
	private List<DisambiguatedEntity> relevantEntities;

	public String getMention() {
		return mention;
	}

	public void setMention(String mention) {
		this.mention = mention;
	}

	public List<DisambiguatedEntity> getRelevantEntities() {
		return relevantEntities;
	}

	public void setRelevantEntities(List<DisambiguatedEntity> relevantEntities) {
		this.relevantEntities = relevantEntities;
	}
}
