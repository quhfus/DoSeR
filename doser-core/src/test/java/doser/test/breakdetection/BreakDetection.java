package doser.test.breakdetection;

public class BreakDetection {

//	@SuppressWarnings("deprecation")
//	public static void main(String[] args) {
//
//		List<WebSite> shotList = new LinkedList<WebSite>();
//		WebSite shot1 = new WebSite();
//		shot1.setName("1");
//		shot1.setText("Text1");
//		shot1.setObjectId(0);
//		WebSite shot2 = new WebSite();
//		shot2.setObjectId(1);
//		shot2.setName("2");
//		shot2.setText("Text2");
//		WebSite shot3 = new WebSite();
//		shot3.setObjectId(2);
//		shot3.setName("3");
//		shot3.setText("Text3");
//		WebSite shot4 = new WebSite();
//		shot4.setObjectId(3);
//		shot4.setName("4");
//		shot4.setText("Text4");
//		WebSite shot5 = new WebSite();
//		shot5.setObjectId(4);
//		shot5.setName("5");
//		shot5.setText("Text5");
//		WebSite shot6 = new WebSite();
//		shot6.setObjectId(5);
//		shot6.setName("6");
//		shot6.setText("Text6");
//
//		shotList.add(shot1);
//		shotList.add(shot2);
//		shotList.add(shot3);
//		shotList.add(shot4);
//		shotList.add(shot5);
//		shotList.add(shot6);
//		Decomposition<WebSite> decomp = new Decomposition<WebSite>(shotList);
//		
//		double[][] similarityMatrix = new double[6][6];
//		similarityMatrix[0][0] = 1;
//		similarityMatrix[0][1] = 0.5;
//		similarityMatrix[0][2] = 0.5;
//		similarityMatrix[0][3] = 0.8;
//		similarityMatrix[0][4] = 0.4;
//		similarityMatrix[0][5] = 0.8;
//
//		similarityMatrix[1][0] = 0.5;
//		similarityMatrix[1][1] = 1.0;
//		similarityMatrix[1][2] = 0.5;
//		similarityMatrix[1][3] = 0.5;
//		similarityMatrix[1][4] = 0.5;
//		similarityMatrix[1][5] = 0.5;
//
//		similarityMatrix[2][0] = 0.5;
//		similarityMatrix[2][1] = 0.5;
//		similarityMatrix[2][2] = 1;
//		similarityMatrix[2][3] = 0.5;
//		similarityMatrix[2][4] = 0.5;
//		similarityMatrix[2][5] = 0.5;
//
//		similarityMatrix[3][0] = 0.8;
//		similarityMatrix[3][1] = 0.5;
//		similarityMatrix[3][2] = 0.5;
//		similarityMatrix[3][3] = 1;
//		similarityMatrix[3][4] = 0.5;
//		similarityMatrix[3][5] = 0.8;
//
//		similarityMatrix[4][0] = 0.5;
//		similarityMatrix[4][1] = 0.5;
//		similarityMatrix[4][2] = 1;
//		similarityMatrix[4][3] = 0.5;
//		similarityMatrix[4][4] = 0.5;
//		similarityMatrix[4][5] = 0.5;
//
//		similarityMatrix[5][0] = 0.8;
//		similarityMatrix[5][1] = 0.5;
//		similarityMatrix[5][2] = 0.5;
//		similarityMatrix[5][3] = 0.8;
//		similarityMatrix[5][4] = 0.5;
//		similarityMatrix[5][5] = 1;
//
//		decomp.setSimilarityMatrix(similarityMatrix);
//		decomp.start();
//		try {
//			decomp.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		ConcurrentNCutAlgorithm<WebSite> nCutAlgorithm = new ConcurrentNCutAlgorithm<WebSite>(decomp.getMainCluster());
//
//		List<Cluster<WebSite>> clusterList = nCutAlgorithm.startClustering();
//		for (Cluster<WebSite> cluster : clusterList) {
//			List<WebSite> list = cluster.getObjectList();
//			for (WebSite site : list) {
//				System.out.println("Site id: " + site.getObjectId());
//			}
//		}
//		
//		
////		decomp.createUndirectedWeightedGraph();
//		// Third Step: VideoDecomposition
////		List<Cluster<WebSite>> clusterLst = doVideoDecomposition(decomp, shotList);
//
////		// Step Four: Temporal Graph Creation
////		TemporalGraph<Cluster<WebSite>> tempGraph = doTemporalGraphGeneration(clusterLst);
////
////		// Step Five: Shortest Path
////		List<Cluster> shortestPath = doShortestPath(tempGraph);
////
////		// Step Six: Scene Extraction
////		doSceneExtraction(tempGraph, shortestPath);
//	}

	
}
