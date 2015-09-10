package doser.sequencedetection.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import doser.sequencedetection.graph.Edge;
import doser.sequencedetection.graph.Node;
import doser.sequencedetection.graph.UndirectedWeightedShotGraph;

/**
 * Still experimental and not tested yet.
 * 
 * @author Stefan Zwicklbauer
 * 
 * @param 
 */
public class Decomposition<K extends Comparable<K> & NCutObject<K>> extends
		Thread {

	private List<K> objLstOverall;

	private UndirectedWeightedShotGraph<K> graph;

	private List<FillAdjazencyMatrixWorker> adjacencyWorker;

	private double[][] similarityMatrix;

	private int amountWorker;

	public Decomposition(List<K> objLst) {
		this.objLstOverall = objLst;
		this.amountWorker = getAmountWorkers();
		this.adjacencyWorker = new ArrayList<Decomposition<K>.FillAdjazencyMatrixWorker>();
		this.similarityMatrix = new double[objLst.size()][objLst.size()];
		// Standard initialization with -1
		for (int i = 0; i < similarityMatrix.length; i++) {
			for (int j = 0; j < similarityMatrix[i].length; j++) {
				similarityMatrix[i][j] = -1;
			}
		}

		for (int i = 0; i < amountWorker; i++) {
			List<K> lst = createWorkerList(amountWorker, i);
			adjacencyWorker.add(new FillAdjazencyMatrixWorker(lst));
		}
	}

	@Override
	public void run() {
		// do some preprocessing
		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
				amountWorker);
		ThreadPoolExecutor ex = new ThreadPoolExecutor(amountWorker,
				amountWorker, 100, TimeUnit.SECONDS, queue);
		for (int i = 0; i < amountWorker; i++) {
			ex.execute(adjacencyWorker.get(i));
		}
		ex.shutdown();
		try {
			ex.awaitTermination(100, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		createUndirectedWeightedGraph();
	}

	/**
	 * @ Deprecated - Method only for test purpose
	 * 
	 * @param similarityMatrix
	 */
	@Deprecated
	public void setSimilarityMatrix(double[][] similarityMatrix) {
		this.similarityMatrix = similarityMatrix;
	}

	public Cluster<K> getMainCluster() {
		return new Cluster<K>(objLstOverall, graph);
	}

	private void createUndirectedWeightedGraph() {
		graph = new UndirectedWeightedShotGraph<K>();
		for (int i = 0; i < objLstOverall.size(); i++) {
			graph.setNode(objLstOverall.get(i));
		}

		List<Node<K>> lst = graph.getNodeList(objLstOverall);

		for (Node<K> node : lst) {
			for (int i = 0; i < lst.size(); i++) {
				Node<K> toNode = lst.get(i);
				if (node.compareTo(toNode) != 0) {
					Edge<K> edge = toNode.hasEdgeToNode(node);
					if (edge != null) {
						node.addAdjacencyNode(new Edge<K>(toNode, edge
								.getWeight()));
					} else {
						double val = similarityMatrix[node.getData()
								.getObjectId()][lst.get(i).getData()
								.getObjectId()];
						node.addAdjacencyNode(lst.get(i), (float) val);
					}
				} else {
					Edge<K> edge = toNode.hasEdgeToNode(node);
					if (edge != null) {
						node.addAdjacencyNode(new Edge<K>(toNode, edge
								.getWeight()));
					} else {
						node.addAdjacencyNode(lst.get(i), (float) 1.0);
					}
				}
			}
		}
	}

	private int getAmountWorkers() {
		return Runtime.getRuntime().availableProcessors();
	}

	private List<K> createWorkerList(int amountWorker, int worker) {
		int mainSplitter = (int) Math
				.floor(objLstOverall.size() / amountWorker);
		int rest = objLstOverall.size() % amountWorker;
		int start = 0;
		for (int i = 0; i < worker; i++) {
			start += mainSplitter;
			if (rest > 0) {
				start++;
				rest--;
			}
		}
		int end = start + mainSplitter;
		if (rest > 0) {
			end++;
		}
		List<K> result = objLstOverall.subList(start, end);
		return result;
	}

	class FillAdjazencyMatrixWorker extends Thread {

		private List<K> objList;

		FillAdjazencyMatrixWorker(List<K> objectList) {
			this.objList = objectList;
		}

		@Override
		public void run() {
			List<K> lst = objLstOverall;

			for (K objToDo : objList) {
				for (K obj : lst) {
					if (similarityMatrix[obj.getObjectId()][objToDo
							.getObjectId()] == -1) {
						similarityMatrix[objToDo.getObjectId()][obj
								.getObjectId()] = objToDo
								.computeSimilarity(obj);
					} else {
						similarityMatrix[objToDo.getObjectId()][obj
								.getObjectId()] = similarityMatrix[obj
								.getObjectId()][objToDo.getObjectId()];
					}
				}

			}
		}
	}
}
