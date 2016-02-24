package doser.entitydisambiguation.backend;

import java.util.List;

import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.knowledgebases.KnowledgeBaseIdentifiers;

public class DisambiguationTaskCollective extends AbstractDisambiguationTask {

	private List<EntityDisambiguationDPO> entitiesToDis;
	
	/* A maintopic e.g. the column identifier in a table */
	private String mainTopic;

	public DisambiguationTaskCollective(final List<EntityDisambiguationDPO> entityToDis, String mainTopic) {
		super();
		this.entitiesToDis = entityToDis;
		this.mainTopic = mainTopic;
	}

	public List<EntityDisambiguationDPO> getEntityToDisambiguate() {
		return this.entitiesToDis;
	}
	
	public String getMainTopic() {
		return this.mainTopic;
	}

	public void setSurfaceForm(final List<EntityDisambiguationDPO> surfaceForm) {
		this.entitiesToDis = surfaceForm;
	}

	/**
	 * Assignment function to determine the used knowledge base
	 * 
	 * @param kbversion
	 * @param setting
	 */
	@Override
	public void setKbIdentifier(String kbversion, String setting) {
		if(setting == null) {
			this.kbIdentifier = KnowledgeBaseIdentifiers.Standard;
		} else if(setting.equalsIgnoreCase("DocumentCentric")) {
			if(kbversion.equalsIgnoreCase("default")) {
				this.kbIdentifier = KnowledgeBaseIdentifiers.DocumentCentricDefault;
			} else {
				this.kbIdentifier = KnowledgeBaseIdentifiers.DocumentCentricDefault;
			}
		} else if(setting.equalsIgnoreCase("EntityCentric")) {
			if(kbversion.equalsIgnoreCase("default")) {
				this.kbIdentifier = KnowledgeBaseIdentifiers.Standard;
			} else if(kbversion.equalsIgnoreCase("cstable")) {
				this.kbIdentifier = KnowledgeBaseIdentifiers.CSTable;
			} else if(kbversion.equalsIgnoreCase("biomed")) {
				this.kbIdentifier = KnowledgeBaseIdentifiers.Biomed;
			} else {
				this.kbIdentifier = KnowledgeBaseIdentifiers.Standard;
			}
		} else {
			this.kbIdentifier = KnowledgeBaseIdentifiers.Standard;
		}
	}	
}
