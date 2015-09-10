package doser.sequencedetection.algorithm;

import java.util.LinkedList;
import java.util.List;

import doser.sequencedetection.graph.GraphContent;
import doser.sequencedetection.graph.TemporalGraph;

public class Extraction<K extends Comparable<K> & NCutObject<K>> {

	private int key;

	private TemporalGraph<Cluster<K>> graph;

	private List<Cluster<K>> shortestPath;

	public Extraction(TemporalGraph<Cluster<K>> graph,
			List<Cluster<K>> shortestPath) {
		this.key = 0;
		this.graph = graph;
		this.shortestPath = shortestPath;
		graph.markShortestPath(shortestPath);
		// Graph coloring!
		colorGraph();
	}

	public List<Sequence<K>> generateSequences() {
		List<Sequence<K>> seqList = new LinkedList<Sequence<K>>();
		int start = 0;
		if (shortestPath.size() == 1) {
			seqList.add(generateSequenceObject(getClusterScene(
					shortestPath.get(0), false)));
		} else {
			for (int i = 1; i < shortestPath.size(); i++) {
				setInvisible(i - 1);
				boolean check = graph.checkConnection(shortestPath.get(i - 1),
						shortestPath.get(i));
				if (!check) {
					seqList.add(generateSequenceObject(getClusterScene(
							shortestPath.get(start), false)));
					start = i;
					if (i == shortestPath.size() - 1) {
						seqList.add(generateSequenceObject(getClusterScene(
								shortestPath.get(start), false)));
					}
				} else {
					setVisible(i - 1);
					if (i == shortestPath.size() - 1) {
						seqList.add(generateSequenceObject(getClusterScene(
								shortestPath.get(start), false)));
					}
				}
				graph.unmarkAll();
			}
		}
		return seqList;
	}

	private List<GraphContent<Cluster<K>>> getClusterScene(Cluster<K> start,
			boolean coloring) {
		return graph.getAllObjectsInCircle(start, coloring);
	}

	private Sequence<K> generateSequenceObject(
			List<GraphContent<Cluster<K>>> lst) {
		Sequence<K> scene = new Sequence<K>(key);
		for (GraphContent<Cluster<K>> graphContent : lst) {
			List<K> clusterElements = graphContent.getContent().getObjectList();
			scene.addElements(clusterElements);
		}
		scene.setSettings();
		key++;
		return scene;
	}

	private void setVisible(int position) {
		graph.changeVisibility(shortestPath.get(position),
				shortestPath.get(position + 1), true);
	}

	private void setInvisible(int position) {
		graph.changeVisibility(shortestPath.get(position),
				shortestPath.get(position + 1), false);
	}

	private void colorGraph() {
		int start = 0;
		for (int i = 1; i < shortestPath.size(); i++) {
			setInvisible(i - 1);
			boolean check = graph.checkConnection(shortestPath.get(i - 1),
					shortestPath.get(i));
			if (!check) {
				getClusterScene(shortestPath.get(start), true);
				start = i;
				if (i == shortestPath.size() - 1) {
					getClusterScene(shortestPath.get(start), true);
				}
				setVisible(i - 1);
			} else {
				setVisible(i - 1);
				if (i == shortestPath.size() - 1) {
					getClusterScene(shortestPath.get(start), true);
				}
			}
			graph.unmarkAll();
		}
	}
}
