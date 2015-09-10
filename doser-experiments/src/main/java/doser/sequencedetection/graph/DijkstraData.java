package doser.sequencedetection.graph;

public class DijkstraData<T extends Comparable<T>> {

	public static final double MAX_DISTANCE = Double.MAX_VALUE;

	/**
	 * The latest known distance from source to the node associated to this
	 * DijkstraData object. This distance is given by the Dijkstra algorithm in
	 * class Dijkstra.
	 */
	private double distanceToSource;

	/** The Type of the node associated to this DijkstraData-Object */
	private NodeTypes nodeType;

	/**
	 * The latest predecessor node of the node associated to this DijkstraData
	 * object.
	 */
	private Node<T> predecessor;

	DijkstraData() {
		predecessor = null;
		distanceToSource = MAX_DISTANCE;
		nodeType = NodeTypes.WHITE;
	}

	/**
	 * Returns distanceToSource.
	 * 
	 * @return distanceToSource
	 */
	double getDistanceToSource() {
		return distanceToSource;
	}

	/**
	 * Returns the nodeType.
	 * 
	 * @return nodeType the nodeType
	 */
	NodeTypes getNodeType() {
		return nodeType;
	}

	/**
	 * Returns the predecessor.
	 * 
	 * @return the predecessor
	 */
	Node<T> getPredecessor() {
		return predecessor;
	}

	/**
	 * Sets distanceToSource.
	 * 
	 * @param distanceToSource
	 *            the new distanceToSource (a non-negative value)
	 */
	void setDistanceToSource(double distanceToSource) {
		this.distanceToSource = distanceToSource;
	}

	/**
	 * Sets the nodeType.
	 * 
	 * @param nodeType
	 *            the new nodeType
	 */
	void setNodeType(NodeTypes nodeType) {
		this.nodeType = nodeType;
	}

	/**
	 * Sets the predecessor.
	 * 
	 * @param predecessor
	 *            the new predecessor
	 */
	void setPredecessor(Node<T> predecessor) {
		this.predecessor = predecessor;
	}

}