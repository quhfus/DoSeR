package doser.sequencedetection.graph;

/**
 * An exception which is thrown if no route was found (e.g. between two nodes)
 */
public class NoRouteFoundException extends Exception {

	/** default serial version id */
	private static final long serialVersionUID = 1L;

	/**
	 * The standard constructor.
	 */
	public NoRouteFoundException() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return "No route could be calculated!";
	}
}