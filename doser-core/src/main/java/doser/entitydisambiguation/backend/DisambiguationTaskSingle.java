package doser.entitydisambiguation.backend;

import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.knowledgebases.KnowledgeBaseIdentifiers;

public class DisambiguationTaskSingle extends AbstractDisambiguationTask {

	private EntityDisambiguationDPO entityToDis;

	public DisambiguationTaskSingle(final EntityDisambiguationDPO entityToDis) {
		super();
		this.entityToDis = entityToDis;
		this.retrieveDocClasses = false;
	}

	public EntityDisambiguationDPO getEntityToDisambiguate() {
		return this.entityToDis;
	}

	public void setSurfaceForm(final EntityDisambiguationDPO surfaceForm) {
		this.entityToDis = surfaceForm;
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
				this.kbIdentifier = KnowledgeBaseIdentifiers.Biomed;
			} else {
				this.kbIdentifier = KnowledgeBaseIdentifiers.Standard;
			}
		} else {
			this.kbIdentifier = KnowledgeBaseIdentifiers.Standard;
		}
	}
}
