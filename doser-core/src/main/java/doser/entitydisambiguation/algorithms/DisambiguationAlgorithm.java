package doser.entitydisambiguation.algorithms;

import doser.entitydisambiguation.backend.DisambiguationTask;

public abstract class DisambiguationAlgorithm {

	protected DisambiguationTask task;

	public void disambiguate(DisambiguationTask task)
			throws IllegalDisambiguationAlgorithmInputException {
		if (checkAndSetInputParameter(task)) {
			if (preDisambiguation()) {
				processAlgorithm();
			}
		} else {
			throw new IllegalDisambiguationAlgorithmInputException(
					"Check your input knowledge base and disambiguation task");
		}
	}

	protected abstract boolean checkAndSetInputParameter(DisambiguationTask task);

	protected abstract void processAlgorithm()
			throws IllegalDisambiguationAlgorithmInputException;

	protected abstract boolean preDisambiguation();
}