package doser.sequencedetection.graph;

import java.util.List;

abstract class AbstractGraph<T extends Comparable<T>> {

	private Node<T> endObject;

	protected List<Node<T>> nodeLst;

	private Node<T> startObject;

	public Node<T> getEndObject() {
		return endObject;
	}

	public List<Node<T>> getNodeList() {
		return nodeLst;
	}

	public Node<T> getStartObject() {
		return startObject;
	}

	public void removeNode(Node<T> node) {
		for (int i = 0; i < nodeLst.size(); i++) {
			Node<T> no = nodeLst.get(i);
			if (node.compareTo(no) == 0) {
				nodeLst.remove(i);
				break;
			}
		}
	}

	/**
	 * Sets the AlgorithmData object of all nodes in adjArray to null.
	 */
	abstract void resetDijkstraData();

	public void setEndObject(Node<T> endObject) {
		this.endObject = endObject;
	}

	public void setNode(T data) {
		nodeLst.add(new Node<T>(data));
	}

	public void setStartObject(Node<T> startObject) {
		this.startObject = startObject;
	}
}
