package doser.sequencedetection.graph;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Class of a concrete implementation of the standard Dijkstra algorithm for
 * Node<T> Object.
 */
public class Dijkstra<T extends Comparable<T>> {

	private AbstractGraph<T> graph;

	/**
	 * The heap used by the dijkstra algorithm
	 */
	private PriorityQueue<Node<T>> heap;

	/**
	 * Constructs a Dijkstra object.
	 */
	public Dijkstra(AbstractGraph<T> graph) {
		this.graph = graph;
	}

	/**
	 * Searches for all neighbours of the the node in the graph and puts them
	 * into the heap. Further the distance to source and the nodetype are
	 * updated
	 * 
	 * @param current
	 *            the node of current calculation
	 */
	private void addNeighboursToHeap(Node<T> current) {
		DijkstraData<T> currentNodeData = getData(current);

		for (Edge<T> edge : current.getOutgoingEdges()) {
			// Edge must be visible
			if (!edge.getVisibleStatus()) {
				Node<T> neighbour = edge.getEndNode();
				if (getData(neighbour) == null) {
					neighbour.setDijkstraData(new DijkstraData<T>());
				}
				DijkstraData<T> neighbourNodeData = getData(neighbour);
				if (!isBlack(neighbour)) {
					double newDist = currentNodeData.getDistanceToSource()
							+ edge.getDijkstraWeight();
					if (newDist < neighbourNodeData.getDistanceToSource()) {
						neighbourNodeData.setDistanceToSource(newDist);
						neighbourNodeData.setPredecessor(current);
						if (newDist < Double.MAX_VALUE
								&& neighbourNodeData.getNodeType() == NodeTypes.WHITE) {
							heap.add(neighbour);
							neighbourNodeData.setNodeType(NodeTypes.GREY);
						}
					}
				}
			}
		}
	}

	/**
	 * Builds the calculated shortest route (in respect to the RoutePlaner).
	 * 
	 * @param source
	 *            the source of the route
	 * @param target
	 *            the target of the route
	 * @return the calculated shortest route (in respect to the RoutePlaner)
	 */
	private List<T> buildRoute(Node<T> source, Node<T> target) {
		LinkedList<Node<T>> route = new LinkedList<Node<T>>();
		Node<T> current = target;
		while (current != source) {
			DijkstraData<T> data = getData(current);
			route.addFirst(current);

			current = data.getPredecessor();
		}
		route.addFirst(source);
		return extractNodeData(route);
	}

	public List<T> calculateRoute(Node<T> startNode, Node<T> endNode)
			throws NoRouteFoundException {
		if (startNode == null || endNode == null) {
			throw new NoRouteFoundException();
		}

		heap = new PriorityQueue<Node<T>>(1, new Comparator<Node<T>>() {

			@Override
			public int compare(Node<T> o1, Node<T> o2) {
				double length1 = getData(o1).getDistanceToSource();
				double length2 = getData(o2).getDistanceToSource();
				return (int) Math.signum(length1 - length2);
			}
		});

		graph.resetDijkstraData();

		startNode.setDijkstraData(new DijkstraData<T>());
		DijkstraData<T> data = getData(startNode);
		data.setDistanceToSource(0.0);
		data.setNodeType(NodeTypes.GREY);
		heap.add(startNode);

		Node<T> current;

		do {
			current = heap.poll();
			setBlack(current);
			addNeighboursToHeap(current);
		} while (current != endNode && heap.size() != 0);

		// No route found
		if (current != endNode) {
			throw new NoRouteFoundException();
		} else {
			return buildRoute(startNode, endNode);
		}
	}

	private List<T> extractNodeData(List<Node<T>> nodeLst) {
		List<T> result = new LinkedList<T>();
		for (Node<T> node2 : nodeLst) {
			Node<T> node = node2;
			T t = node.getData();
			result.add(t);
		}
		return result;
	}

	/**
	 * Returns the DijkstraData object of the node
	 * 
	 * @param node
	 * 
	 * @return the DijkstraData object of the node
	 */
	private DijkstraData<T> getData(Node<T> node) {
		return node.getDijkstraData();
	}

	/**
	 * Cecks if the node is set black.
	 * 
	 * @param node
	 *            the node that is checked
	 * @return true if the node is black, else false
	 */
	private boolean isBlack(Node<T> node) {
		DijkstraData<T> data = node.getDijkstraData();
		if (data == null) {
			return false;
		}
		if (data.getNodeType() == NodeTypes.BLACK) {
			return true;
		}
		return false;
	}

	/**
	 * Sets the current node black.
	 * 
	 * @param node
	 *            the node which is set black
	 */
	private void setBlack(Node<T> node) {
		if (node.getDijkstraData() == null) {
			node.setDijkstraData(new DijkstraData<T>());
		}

		DijkstraData<T> data = node.getDijkstraData();
		data.setNodeType(NodeTypes.BLACK);
	}
}