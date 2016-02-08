package doser.entitydisambiguation.algorithms;

import doser.entitydisambiguation.backend.AbstractDisambiguationTask;

public abstract class AbstractDisambiguationAlgorithm {

	protected AbstractDisambiguationTask task;

	public void disambiguate(AbstractDisambiguationTask task)
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

	public static String extractContext(int position, String text,
			int contextarea) {
		if(text == null || text.length() == 0) {
			return "";
		}
		
		long startArea = position - contextarea;
		long endArea = position + contextarea;
		if (startArea < 0) {
			startArea = 0;
		}
		if (endArea > text.length() - 1) {
			endArea = text.length() - 1;
		}
		String tempText = text.substring((int) startArea, (int) endArea);
		String[] splitter = tempText.split(" ");
		String result = "";
		for (int i = 1; i < splitter.length - 1; i++) {
			result += splitter[i] + " ";
		}
		return result;
	}

	protected abstract boolean checkAndSetInputParameter(AbstractDisambiguationTask task);

	protected abstract void processAlgorithm()
			throws IllegalDisambiguationAlgorithmInputException;

	protected abstract boolean preDisambiguation();
}