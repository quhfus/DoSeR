package doser.webclassify.algorithm;

//import java.util.LinkedList;
//import java.util.List;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
//import org.apache.log4j.Logger;
//
//import doser.sequencedetection.algorithm.Cluster;
//import doser.sequencedetection.algorithm.ConcurrentNCutAlgorithm;
//import doser.sequencedetection.algorithm.Decomposition;
//import doser.sequencedetection.algorithm.Extraction;
//import doser.sequencedetection.algorithm.Sequence;
//import doser.sequencedetection.algorithm.TemporalGraphGeneration;
//import doser.sequencedetection.graph.Dijkstra;
//import doser.sequencedetection.graph.NoRouteFoundException;
//import doser.sequencedetection.graph.TemporalGraph;
//import doser.webclassify.dpo.WebSite;
//
//public class SessionBreakDetection {
//
//	public SessionBreakDetection() {
//		super();
//	}
//
//	public List<Sequence<WebSite>> sessionBreak(List<WebSite> websites) {
//		// Session decomposition
//		List<Cluster<WebSite>> clusterLst = doVideoDecomposition(websites);
//		// Temporal Graph Creation
//		TemporalGraph<Cluster<WebSite>> graph = doTemporalGraphGeneration(clusterLst);
//		// Shortest Path
//		List<Cluster<WebSite>> shortestPath = doShortestPath(graph);
//		// Extraction
//		return (doSessionExtraction(graph, shortestPath));
//	}
//
//	private List<Cluster<WebSite>> doVideoDecomposition(List<WebSite> lst) {
//		Decomposition<WebSite> decomp = new Decomposition<WebSite>(lst);
//		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1);
//		ThreadPoolExecutor ex = new ThreadPoolExecutor(1, 1, 100,
//				TimeUnit.SECONDS, queue);
//		ex.execute(decomp);
//		ex.shutdown();
//		try {
//			ex.awaitTermination(100, TimeUnit.SECONDS);
//		} catch (InterruptedException e) {
//			Logger.getRootLogger().warn(e.getStackTrace());
//		}
//
//		ConcurrentNCutAlgorithm<WebSite> nCutAlgorithm = new ConcurrentNCutAlgorithm<WebSite>(
//				decomp.getMainCluster());
//		List<Cluster<WebSite>> clusterList = nCutAlgorithm.startClustering();
//		for (Cluster<WebSite> cluster : clusterList) {
//			List<WebSite> list = cluster.getObjectList();
//			for (WebSite site : list) {
//				System.out.println("Site id: " + site.getObjectId());
//			}
//		}
//		return clusterList;
//	}
//
//	private TemporalGraph<Cluster<WebSite>> doTemporalGraphGeneration(
//			List<Cluster<WebSite>> lst) {
//		TemporalGraph<Cluster<WebSite>> graph = new TemporalGraphGeneration<WebSite>(
//				lst).startTemporalGraphGeneration();
//		return graph;
//	}
//
//	private List<Cluster<WebSite>> doShortestPath(
//			TemporalGraph<Cluster<WebSite>> graph) {
//		Dijkstra<Cluster<WebSite>> dijkstra = new Dijkstra<Cluster<WebSite>>(
//				graph);
//		List<Cluster<WebSite>> shortestPath = new LinkedList<Cluster<WebSite>>();
//		try {
//			shortestPath = dijkstra.calculateRoute(graph.getStartObject(),
//					graph.getEndObject());
//		} catch (NoRouteFoundException e) {
//			Logger.getRootLogger().error("Error:", e);
//			;
//		}
//		return shortestPath;
//	}
//
//	private List<Sequence<WebSite>> doSessionExtraction(
//			TemporalGraph<Cluster<WebSite>> graph,
//			List<Cluster<WebSite>> shortestPath) {
//		List<Sequence<WebSite>> sessions = new Extraction<WebSite>(graph,
//				shortestPath).generateSequences();
//		return sessions;
//	}
//}
