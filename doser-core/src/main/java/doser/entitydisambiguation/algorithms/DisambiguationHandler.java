package doser.entitydisambiguation.algorithms;

import doser.entitydisambiguation.algorithms.collective.dbpedia.CollectiveDisambiguationDBpediaEntities;
import doser.entitydisambiguation.algorithms.collective.general.CollectiveDisambiguationGeneralEntities;
import doser.entitydisambiguation.backend.AbstractDisambiguationTask;
import doser.entitydisambiguation.backend.DisambiguationTaskSingle;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.knowledgebases.KnowledgeBaseIdentifiers;

public class DisambiguationHandler {

	private static final DisambiguationHandler instance;

	static {
		try {
			instance = new DisambiguationHandler();
		} catch (Exception e) {
			throw new RuntimeException("An error occurred!", e);
		}
	}

	private DisambiguationHandler() {
		super();
	}

	public static DisambiguationHandler getInstance() {
		return instance;
	}

	public AbstractDisambiguationAlgorithm getAlgorithm(AbstractDisambiguationTask task) {
		AbstractDisambiguationAlgorithm algorithm = null;
		if (task instanceof DisambiguationTaskSingle) {
			DisambiguationTaskSingle t = (DisambiguationTaskSingle) task;
			EntityDisambiguationDPO dpo = t.getEntityToDisambiguate();
			if (dpo.getSetting() != null
					&& (dpo.getSetting().equalsIgnoreCase("CSTable"))) {
				algorithm = new EntityCentricAlgorithmCSTable();
			} else if ((dpo.getSetting() != null
					&& (dpo.getSetting().equalsIgnoreCase("NoContext"))
					|| dpo.getContext() == null || dpo.getContext().equals("") || dpo
					.getContext().equals(" "))) {
				algorithm = new EntityCentricAlgorithmTableDefault();
			} else if ((dpo.getSetting() != null)
					&& (dpo.getSetting().equalsIgnoreCase("DocumentCentric"))) {
				algorithm = new DocumentCentricAlgorithmDefault();
			} else {
				algorithm = new EntityCentricAlgorithmDefault();
			}
		} else {
			if (task.getKbIdentifier().equals(KnowledgeBaseIdentifiers.Biomed)) {
				algorithm = new CollectiveDisambiguationGeneralEntities();
			} else {
				algorithm = new CollectiveDisambiguationDBpediaEntities();
			}
		}
		return algorithm;
	}
}
