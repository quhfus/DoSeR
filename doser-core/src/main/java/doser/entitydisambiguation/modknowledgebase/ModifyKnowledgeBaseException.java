package doser.entitydisambiguation.modknowledgebase;

public class ModifyKnowledgeBaseException extends Exception {

	private static final long serialVersionUID = 4216924773974862496L;

	private final transient Exception error;

	public ModifyKnowledgeBaseException(final String message,
			final Exception error) {
		super(message);
		this.error = error;
	}

	public Exception getError() {
		return this.error;
	}
}
