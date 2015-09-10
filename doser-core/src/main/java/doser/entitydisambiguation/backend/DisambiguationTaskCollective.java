package doser.entitydisambiguation.backend;

import java.util.List;

import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.knowledgebases.KnowledgeBaseIdentifiers;

public class DisambiguationTaskCollective extends DisambiguationTask {

	private List<EntityDisambiguationDPO> entitiesToDis;

	public DisambiguationTaskCollective(final List<EntityDisambiguationDPO> entityToDis) {
		super();
		this.entitiesToDis = entityToDis;
	}

	public List<EntityDisambiguationDPO> getEntityToDisambiguate() {
		return this.entitiesToDis;
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
			} else if(kbversion.equalsIgnoreCase("biomedcopy")) {
				this.kbIdentifier = KnowledgeBaseIdentifiers.DbPediaBiomedCopy;
			} else {
				this.kbIdentifier = KnowledgeBaseIdentifiers.Standard;
			}
		} else {
			this.kbIdentifier = KnowledgeBaseIdentifiers.Standard;
		}
	}	
}
