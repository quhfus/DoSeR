package doser.entitydisambiguation.algorithms;

public class IllegalDisambiguationAlgorithmInputException extends
		IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	IllegalDisambiguationAlgorithmInputException() {
		super("Wrong Knowledge base!");
	}

	IllegalDisambiguationAlgorithmInputException(String text) {
		super(text);
	}

}
