package doser.sequencedetection.graph;

import java.util.LinkedList;
import java.util.List;

public class TemporalGraph<T extends Comparable<T>> extends AbstractGraph<T> {

	public TemporalGraph() {
		super();
		nodeLst = new LinkedList<Node<T>>();
	}

	public void changeVisibility(T t1, T t2, boolean visible) {
		for (Node<T> node : nodeLst) {
			if (node.getData().compareTo(t1) == 0) {
				List<Edge<T>> edgeLst = node.getOutgoingEdges();
				for (Edge<T> edge : edgeLst) {
					if (edge.getEndNode().getData().compareTo(t2) == 0) {
						if (visible) {
							edge.setVisible();
						} else {
							edge.setInvisible();
						}
					}
				}
			}
		}
	}

	public boolean checkConnection(T start, T end) {
		boolean backwardSearch = false;
		boolean exist = true;
		List<Node<T>> nodeLst = getNodeList();
		Node<T> startNode = null;
		Node<T> endNode = null;
		for (Node<T> node : nodeLst) {
			if (node.getData().compareTo(start) == 0) {
				startNode = node;
			} else if (node.getData().compareTo(end) == 0) {
				endNode = node;
			}
		}

		Dijkstra<T> dijkstra = new Dijkstra<T>(this);

		// Edge between nodes must be invisible
		try {
			dijkstra.calculateRoute(startNode, endNode);
		} catch (NoRouteFoundException e) {
			backwardSearch = true;
		}

		// Backward way must be tested
		if (backwardSearch) {
			try {
				dijkstra.calculateRoute(endNode, startNode);
			} catch (NoRouteFoundException e) {
				exist = false;
			}
		}

		return exist;
	}

	public List<GraphContent<T>> getAllObjectsInCircle(T t, boolean coloring) {
		List<GraphContent<T>> endObjects = new LinkedList<GraphContent<T>>();
		for (Node<T> node : nodeLst) {
			if (!node.getMarked() && node.getData().compareTo(t) == 0) {
				node.setMarked();
				if (coloring) {
					recursiveDepthFirstSearchColoring(node);
				} else {
					List<GraphContent<T>> unit = recursiveDepthFirstSearch(node);
					endObjects.addAll(unit);
				}
			}
		}
		return endObjects;
	}

	public void markShortestPath(List<T> path) {
		for (Node<T> t : nodeLst) {
			for (T t2 : path) {
				if (t.getData().compareTo(t2) == 0) {
					t.setType(NodeTypes.BLACK);
				}
			}
		}
	}

	private List<GraphContent<T>> recursiveDepthFirstSearch(Node<T> start) {
		List<GraphContent<T>> objects = new LinkedList<GraphContent<T>>();
		List<Edge<T>> edgeLst = start.getOutgoingEdges();
		boolean ambiguous = false;
		if (start.getType() == NodeTypes.RED) {
			ambiguous = true;
		}
		objects.add(new GraphContent<T>(start.getData(), ambiguous));
		start.setMarked();
		for (Edge<T> edge : edgeLst) {
			if (!edge.getVisibleStatus()) {
				Node<T> target = edge.getEndNode();

				// For further explanation look at algorithm in master thesis
				if (!target.getMarked()
						&& (((target.getType() == NodeTypes.WHITE)
								|| (target.getType() == NodeTypes.GREY) || target
								.getType() == NodeTypes.RED) || (start
								.getType() == NodeTypes.BLACK && target
								.getType() == NodeTypes.BLACK))) {
					objects.addAll(recursiveDepthFirstSearch(target));
				}
			}
		}
		return objects;
	}

	private void recursiveDepthFirstSearchColoring(Node<T> start) {
		List<Edge<T>> edgeLst = start.getOutgoingEdges();
		start.setMarked();
		for (Edge<T> edge : edgeLst) {
			if (!edge.getVisibleStatus()) {
				Node<T> target = edge.getEndNode();
				// For further explanation look at algorithm in master thesis
				if (!target.getMarked()
						&& (((target.getType() == NodeTypes.WHITE)
								|| (target.getType() == NodeTypes.GREY) || (target
								.getType() == NodeTypes.RED)) || (start
								.getType() == NodeTypes.BLACK && target
								.getType() == NodeTypes.BLACK))) {
					if (target.getType() == NodeTypes.GREY) {
						target.setType(NodeTypes.RED);
					} else if (target.getType() == NodeTypes.WHITE) {
						target.setType(NodeTypes.GREY);
					}
					recursiveDepthFirstSearchColoring(target);
				}
			}
		}
	}

	@Override
	public void resetDijkstraData() {
		for (Node<T> node : nodeLst) {
			node.setDijkstraData(null);
		}
	}

	public void unmarkAll() {
		for (Node<T> node : nodeLst) {
			node.setUnmarked();
		}
	}
}
