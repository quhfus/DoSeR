package doser.sequencedetection.graph;

public class Edge<T extends Comparable<T>> {

	private Node<T> endNode;

	private boolean invisible;

	private float weight;

	public Edge(Node<T> node, float weight) {
		this.endNode = node;
		this.weight = weight;
		this.invisible = false;
	}

	float getDijkstraWeight() {
		return 1f;
	}

	public Node<T> getEndNode() {
		return endNode;
	}

	boolean getVisibleStatus() {
		return invisible;
	}

	public float getWeight() {
		return weight;
	}

	void setInvisible() {
		invisible = true;
	}

	void setVisible() {
		invisible = false;
	}
}
