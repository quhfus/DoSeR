package doser.sequencedetection.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;

public class ConcurrentNCutAlgorithm<K extends Comparable<K> & NCutObject<K>> {

	private static final int SPLITTINGPOINT = 0;

	private Cluster<K> mainCluster;

	private double threshold;

	public ConcurrentNCutAlgorithm(Cluster<K> mainCluster) {
		this.mainCluster = mainCluster;
		double average = mainCluster.calcAverage();
		double standardDeviation = mainCluster.calcStandardDeviation(average);
		this.threshold = average + standardDeviation;
	}

	private DoubleMatrix1D getEigenvectorOfEigenValue(
			DoubleMatrix1D eigenvalues, DoubleMatrix2D eigenvectors) {
		// Sort eigenvalues to get the second smallest eigenvalue
		double[] evArr = new double[eigenvalues.size()];
		for (int i = 0; i < eigenvalues.size(); i++) {
			evArr[i] = eigenvalues.getQuick(i);
		}
		Arrays.sort(evArr);

		// Get second smallest eigenvalue
		double ssEigenvalue = evArr[1];
//		System.out.println("2t kleinster EIgenvalue: " + ssEigenvalue);
		int position = 0;
		for (int i = 0; i < eigenvalues.size(); i++) {
			if (ssEigenvalue == eigenvalues.getQuick(i)) {
				position = i;
				break;
			}
		}
		return eigenvectors.viewColumn(position);
	}

	private List<Cluster<K>> recursive2WayNCut(Cluster<K> main) {
		List<Cluster<K>> leafClusters = new LinkedList<Cluster<K>>();
		if (main.isUnderThreshold(threshold) || main.getObjectList().size() == 1) {
			leafClusters.add(main);
			return leafClusters;
		} else {
			NCutMatrix matrix = main.getMatrix();
			EigenvalueDecomposition eigen = new EigenvalueDecomposition(matrix);

			DoubleMatrix1D eigenvalues = eigen.getRealEigenvalues();
			DoubleMatrix2D eigenvectors = eigen.getV();
			DoubleMatrix1D useableEigenvector = getEigenvectorOfEigenValue(
					eigenvalues, eigenvectors);
			ArrayList<Cluster<K>> subClusters = splitUp(main,
					useableEigenvector);
			leafClusters.addAll(recursive2WayNCut(subClusters.get(0)));
			leafClusters.addAll(recursive2WayNCut(subClusters.get(1)));
			return leafClusters;
		}
	}

	private ArrayList<Cluster<K>> splitUp(Cluster<K> main,
			DoubleMatrix1D eigenvector) {
		ArrayList<Cluster<K>> clusters = new ArrayList<Cluster<K>>();
		List<K> lst = main.getObjectList();
		List<K> a = new LinkedList<K>();
		List<K> b = new LinkedList<K>();
		for (int i = 0; i < lst.size(); i++) {
			double vecVal = eigenvector.getQuick(i);
			if (vecVal < SPLITTINGPOINT) {
				a.add(lst.get(i));
			} else {
				b.add(lst.get(i));
			}
		}
		clusters.add(main.createSubCluster(a));
		clusters.add(main.createSubCluster(b));
		return clusters;
	}

	public List<Cluster<K>> startClustering() {
		List<Cluster<K>> clusters = recursive2WayNCut(mainCluster);
		return clusters;
	}
}