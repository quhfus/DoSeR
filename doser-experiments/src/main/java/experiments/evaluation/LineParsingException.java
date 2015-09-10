package experiments.evaluation;

class LineParsingException extends Throwable {

	private static final long serialVersionUID = 1L;

	private String line;
	
	LineParsingException(String line) {
		super();
		this.line = line;
	}
	
	void printLine() {
		System.err.println(line);
	}
}
