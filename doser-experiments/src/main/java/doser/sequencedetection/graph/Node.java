package doser.sequencedetection.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class Node<T extends Comparable<T>> implements Comparable<Node<T>> {

	private T data;

	private DijkstraData<T> dijkstraData;

	private ConcurrentSkipListMap<Integer, Edge<T>> edgeMap;

	private int key;

	private boolean marked;

	private NodeTypes type;

	Node(T data) {
		this.key = 0;
		this.data = data;
		this.edgeMap = new ConcurrentSkipListMap<Integer, Edge<T>>();
		this.dijkstraData = null;
		this.marked = false;
		this.type = NodeTypes.WHITE;
	}

	public void addAdjacencyNode(Edge<T> edge) {
		edgeMap.put(key++, edge);
	}

	public void addAdjacencyNode(Node<T> node) {
		edgeMap.put(key++, new Edge<T>(node, -1));
	}

	public void addAdjacencyNode(Node<T> node, float weight) {
		edgeMap.put(key++, new Edge<T>(node, weight));
	}

	@Override
	public int compareTo(Node<T> o) {
		if (data.compareTo(o.getData()) == 0) {
			return 0;
		} else {
			return 1;
		}
	}

	public T getData() {
		return data;
	}

	/**
	 * Return the Dijkstra Data Object
	 * 
	 * @return Returns the Dijkstra Data Object
	 */
	DijkstraData<T> getDijkstraData() {
		return dijkstraData;
	}

	public ConcurrentSkipListMap<Integer, Edge<T>> getEdgeMap() {
		return edgeMap;
	}

	boolean getMarked() {
		return marked;
	}

	List<Edge<T>> getOutgoingEdges() {
		List<Edge<T>> edgeLst = new LinkedList<Edge<T>>();
		for (Map.Entry<Integer, Edge<T>> entry : edgeMap.entrySet()) {
			Edge<T> edge = entry.getValue();
			edgeLst.add(edge);
		}
		return edgeLst;
	}

	NodeTypes getType() {
		return type;
	}

	public Edge<T> hasEdgeToNode(Node<T> data) {
		for (Map.Entry<Integer, Edge<T>> entry : edgeMap.entrySet()) {
			Edge<T> edge = entry.getValue();
			if (edge.getEndNode().compareTo(data) == 0) {
				return edge;
			}
		}
		return null;
	}

	/**
	 * Sets the Dijkstra Data Object
	 * 
	 * @return Returns the Dijkstra Data Object
	 */
	void setDijkstraData(DijkstraData<T> data) {
		this.dijkstraData = data;
	}

	void setMarked() {
		marked = true;
	}

	void setType(NodeTypes type) {
		this.type = type;
	}

	void setUnmarked() {
		marked = false;
	}
}
