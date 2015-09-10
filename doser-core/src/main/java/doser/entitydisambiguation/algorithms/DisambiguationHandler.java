package doser.entitydisambiguation.algorithms;

import doser.entitydisambiguation.algorithms.collective.EntityCentricAlgorithmCollective;
import doser.entitydisambiguation.backend.DisambiguationTask;
import doser.entitydisambiguation.backend.DisambiguationTaskSingle;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;

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

	public DisambiguationAlgorithm getAlgorithm(DisambiguationTask task) {
		DisambiguationAlgorithm algorithm = null;
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
			algorithm = new EntityCentricAlgorithmCollective();
		}
		return algorithm;
	}
}
