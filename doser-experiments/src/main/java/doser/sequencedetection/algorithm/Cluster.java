package doser.sequencedetection.algorithm;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import doser.sequencedetection.graph.Edge;
import doser.sequencedetection.graph.Node;
import doser.sequencedetection.graph.UndirectedWeightedShotGraph;

public class Cluster<K extends Comparable<K> & NCutObject<K>> implements
		Comparable<Cluster<K>> {

	private NCutMatrix dMatrix;

	private UndirectedWeightedShotGraph<K> graph;

	private NCutMatrix matrix;

	private List<K> objList;

	private NCutMatrix weightMatrix;

	Cluster(List<K> objList, UndirectedWeightedShotGraph<K> graph) {
		this.objList = objList;
		this.graph = graph;
		this.weightMatrix = createMatrix();
		this.dMatrix = createDMatrix();
		this.matrix = calcFinalMatrix();
		// for (int i = 0; i < shotList.size(); i++) {
		// System.out.println("In diesem Cluster Shot Nr: "+shotList.get(i).getShotId());
		// }
	}

	double calcAverage() {
		double sum = 0;
		double[][] values = weightMatrix.getValues();
		int amount = 0;
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < i + 1; j++) {
				if (i != j) {
//					System.out.println(values[i][j]);
					sum += values[i][j];
					amount++;
					// System.out.println("sum: "+sum + "Value: "+values[i][j]);
				}
			}
		}
		// System.out.println("--------------------------------------------------------------");
		// System.out.println("Summe:"+sum+" Amount: "+amount);
		return (sum / amount);
	}

	private NCutMatrix calcFinalMatrix() {
		NCutMatrix dSQRT = dMatrix.SQRTMatrix();
		NCutMatrix subtraction = dMatrix.subtractMatrix(weightMatrix);
		NCutMatrix product = mult(dSQRT, subtraction);
		NCutMatrix result = mult(product, dSQRT);

		return result;
	}

	double calcStandardDeviation(double average) {
		double sum = 0;
		double[][] values = weightMatrix.getValues();
		int amount = 0;
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < i + 1; j++) {
				if (i != j) {
					sum += Math.pow(Math.abs((average - values[i][j])), 2);
					amount++;
				}
			}
		}
		sum = (sum / amount);
		return Math.sqrt(sum);
	}

	boolean clusterHasLinkTo(Cluster<K> target) {
		List<K> targetLst = target.getObjectList();
		for (K k : objList) {
			K shot = k;
			for (K k2 : targetLst) {
				K targetShot = k2;
				if (shot.getObjectId() == (targetShot.getObjectId() + 1)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int compareTo(Cluster<K> o) {
		if (objList.size() != o.getObjectList().size()) {
			return 1;
		}
		for (int i = 0; i < objList.size(); i++) {
			if (objList.get(i).compareTo(o.getObjectList().get(i)) == 0) {
				return 0;
			}
		}
		return 1;
	}

	private NCutMatrix createDMatrix() {
		double[][] values = new double[objList.size()][objList.size()];

		for (int i = 0; i < objList.size(); i++) {
			for (int j = 0; j < objList.size(); j++) {
				values[i][j] = 0;
				if (i == j) {
					double sum = 0;
					for (int j2 = 0; j2 < objList.size(); j2++) {
						sum += weightMatrix.getQuick(i, j2);
					}
					values[i][j] = sum;
				}
			}
		}

		NCutMatrix dMatrix = new NCutMatrix(values);
		return dMatrix;
	}

	private NCutMatrix createMatrix() {
		double[][] values = new double[objList.size()][objList.size()];
		int xkey = 0;
		int ykey = 0;
		List<Node<K>> lst = graph.getNodeList();
		for (Node<K> node2 : lst) {
			Node<K> node = node2;
			int matchingId = isInObjectList(node.getData());
			ykey = 0;
			if (matchingId != -1) {
				ConcurrentSkipListMap<Integer, Edge<K>> map = node.getEdgeMap();
				for (Map.Entry<Integer, Edge<K>> entry : map.entrySet()) {
					Edge<K> edge = entry.getValue();
					int innerMatchingId = isInObjectList(edge.getEndNode()
							.getData());
					if (innerMatchingId != -1) {
						values[xkey][ykey] = edge.getWeight();
						ykey++;
					}
				}
				xkey++;
			}
		}
		NCutMatrix matrix = new NCutMatrix(values);

		return matrix;
	}

	Cluster<K> createSubCluster(List<K> shotList) {
		return new Cluster<K>(shotList, graph);
	}

	NCutMatrix getMatrix() {
		return matrix;
	}

	public List<K> getObjectList() {
		return objList;
	}

	private int isInObjectList(K shot) {
		int id = -1;
		for (K k : objList) {
			K currentShot = k;
			if (currentShot.compareTo(shot) == 0) {
				id = currentShot.getObjectId();
				break;
			}
		}
		return id;
	}

	boolean isUnderThreshold(double threshold) {
		double[][] values = weightMatrix.getValues();
		for (double[] value : values) {
			for (int j = 0; j < value.length; j++) {
				if (value[j] < threshold) {
					return false;
				}
			}
		}
		return true;
	}

	private NCutMatrix mult(NCutMatrix matrix1, NCutMatrix matrix2) {
		DoubleMatrix2D result = Algebra.DEFAULT.mult(matrix1, matrix2);
		double[][] values = new double[matrix1.rows()][matrix1.columns()];
		for (int i = 0; i < result.rows(); i++) {
			for (int j = 0; j < result.columns(); j++) {
				values[i][j] = result.getQuick(i, j);
			}
		}
		return new NCutMatrix(values);
	}

	void sortClusterShots() {
		Collections.sort(objList);
	}
}
