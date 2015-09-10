package doser.sequencedetection.algorithm;

import java.util.List;

import doser.sequencedetection.graph.Node;
import doser.sequencedetection.graph.TemporalGraph;

public class TemporalGraphGeneration<K extends Comparable<K> & NCutObject<K>> {

	private List<Cluster<K>> clusterLst;

	private TemporalGraph<Cluster<K>> graph;

	public TemporalGraphGeneration(List<Cluster<K>> clusterLst) {
		this.clusterLst = clusterLst;
		this.graph = new TemporalGraph<Cluster<K>>();
	}

	// Testing
	public void checking(Cluster<K> cluster) {
		List<K> objLst = cluster.getObjectList();
		for (K k : objLst) {
			System.out.println("shotid: " + k.getObjectId());
		}
		System.out.println("-------------------------------------------------");
	}

	private void createGraphNodes() {
		for (Cluster<K> cluster : clusterLst) {
			graph.setNode(cluster);
		}
	}

	private Node<Cluster<K>> getFirst() {
		List<Node<Cluster<K>>> lst = graph.getNodeList();
		Node<Cluster<K>> result = lst.get(0);
		for (Node<Cluster<K>> node : lst) {
			Cluster<K> cluster = node.getData();
			List<K> shotList = cluster.getObjectList();
			for (K k : shotList) {
				if (k.getObjectId() == 0) {
					result = node;
					System.out.println("StartId" + k.getObjectId());
					break;
				}
			}
		}
		return result;
	}

	private Node<Cluster<K>> getLast() {
		List<Node<Cluster<K>>> lst = graph.getNodeList();
		Node<Cluster<K>> result = lst.get(0);
		int id = 0;
		for (Node<Cluster<K>> node : lst) {
			Cluster<K> cluster = node.getData();
			List<K> shotList = cluster.getObjectList();
			for (K k : shotList) {
				if (k.getObjectId() > id) {
					result = node;
					id = k.getObjectId();
					System.out.println("EndIdHighest" + k.getObjectId());
				}
			}
		}
		return result;
	}

	public TemporalGraph<Cluster<K>> startTemporalGraphGeneration() {
		// Set graph nodes
		createGraphNodes();

		List<Node<Cluster<K>>> nodeLst = graph.getNodeList();
		Node<Cluster<K>> startCluster = getFirst();
		Node<Cluster<K>> endCluster = getLast();
		checking(startCluster.getData());
		checking(endCluster.getData());
		for (Node<Cluster<K>> node : nodeLst) {
			Cluster<K> sourceCluster = node.getData();
			for (Node<Cluster<K>> targetNode : nodeLst) {
				Cluster<K> targetCluster = targetNode.getData();
				if (sourceCluster.clusterHasLinkTo(targetCluster)) {
					targetNode.addAdjacencyNode(node, 1);
				}
			}
		}
		graph.setStartObject(startCluster);
		graph.setEndObject(endCluster);
		return graph;
	}
}
