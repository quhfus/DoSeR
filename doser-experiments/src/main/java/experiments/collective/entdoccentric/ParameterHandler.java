package experiments.collective.entdoccentric;

import java.io.BufferedReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import com.google.gson.Gson;

public class ParameterHandler {

//	public static final int[] contextLength = { 150, 300, 750, 1500 };
//
//	public static final int[] amountReturnVals = { 5, 10, 20, 50 };

//	public static int currentAmountReturnVal = 5;

	private Gson gson;

	private BufferedReader bufferedReader;

	public static final String file = "/home/zwicklbauer/infos";

	private static ParameterHandler instance;
	
	private RAMDirectory ramDir;
	
	private IndexWriter iWriter;

	// public static final String file =
	// "/home/quh/Arbeitsfläche/Code_Data/Calbc/output.json";

	private ParameterHandler() {
//		gson = new Gson();
//		try {
//			FileInputStream fis = new FileInputStream(file);
//			UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(fis);
//			InputStreamReader isr = new InputStreamReader(ubis);
//			bufferedReader = new BufferedReader(isr);
//			ubis.skipBOM();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (NullPointerException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

//		try {
//			FileInputStream fis = new FileInputStream(file);
//			UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(fis);
//			InputStreamReader isr = new InputStreamReader(ubis);
//			bufferedReader = new BufferedReader(isr);
//			ubis.skipBOM();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (NullPointerException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		CalbCAnalyzer analyzer = new CalbCAnalyzer(Version.LUCENE_40);
//
////		File id = new File(indexDirectory);
//		ramDir = new RAMDirectory();
//		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40,
//				analyzer);
//		if (ramDir != null) {
//			try {
//				iWriter = new IndexWriter(ramDir, config);
//			} catch (CorruptIndexException e) {
//				e.printStackTrace();
//			} catch (LockObtainFailedException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}
//
//	public static void main(String[] args) {
////		ParameterHandler handler = ParameterHandler.getInstance();
////		handler.setupLucene();
//		StartEvaluation eval = null;
//		// StartEvaluation.calbcJSON =
//		// "/home/quh/Arbeitsfläche/Code_Data/Calbc/output.json";
//		StartEvaluation.calbcJSON = "/home/zwicklbauer/output.json";
//		// String filenameQrels = "/home/quh/Tests/qrels/eval_";
//		String filenameQrels = "/home/zwicklbauer/disTest/qrels/eval_";
//		// String filenameResult = "/home/quh/Tests/result/eval_";
//		String filenameResult = "/home/zwicklbauer/disTest/results/eval_";
//		// for (int i = 0; i < contextLength.length; i++) {
//		for (int j = 0; j < amountReturnVals.length; j++) {
//			// 1////////////////////////////////////////////////
////			String tempfile = ParameterHandler.generateFileName(
////					contextLength[2], amountReturnVals[j], false, false, false,
////					false);
////			String nameQrels = filenameQrels + tempfile;
////			String nameResult = filenameResult + tempfile;
////			StartEvaluation.contextArea = contextLength[2];
////			StartEvaluation.qrelsFile = nameQrels;
////			StartEvaluation.resultFile = nameResult;
////			eval = new StartEvaluation(false, false, false, false);
////			currentAmountReturnVal = amountReturnVals[j];
////			eval.start();
//
//			// 2////////////////////////////////////////////////
//			String tempfile = ParameterHandler.generateFileName(contextLength[3],
//					amountReturnVals[j], false, false, false, true);
//			String nameQrels = filenameQrels + tempfile;
//			String nameResult = filenameResult + tempfile;
//			StartEvaluation.contextArea = contextLength[3];
//			StartEvaluation.qrelsFile = nameQrels;
//			StartEvaluation.resultFile = nameResult;
//			eval = new StartEvaluation(false, false, false, true);
//			currentAmountReturnVal = amountReturnVals[j];
//			eval.start();
//
//			// 3////////////////////////////////////////////////
////			tempfile = ParameterHandler.generateFileName(contextLength[2],
////					amountReturnVals[j], false, false, true, false);
////			nameQrels = filenameQrels + tempfile;
////			nameResult = filenameResult + tempfile;
////			StartEvaluation.contextArea = contextLength[2];
////			StartEvaluation.qrelsFile = nameQrels;
////			StartEvaluation.resultFile = nameResult;
////			eval = new StartEvaluation(false, false, true, false);
////			currentAmountReturnVal = amountReturnVals[j];
////			eval.start();
//
//			// 4////////////////////////////////////////////////
//			tempfile = ParameterHandler.generateFileName(contextLength[3],
//					amountReturnVals[j], false, false, true, true);
//			nameQrels = filenameQrels + tempfile;
//			nameResult = filenameResult + tempfile;
//			StartEvaluation.contextArea = contextLength[3];
//			StartEvaluation.qrelsFile = nameQrels;
//			StartEvaluation.resultFile = nameResult;
//			eval = new StartEvaluation(false, false, true, true);
//			currentAmountReturnVal = amountReturnVals[j];
//			eval.start();
//
//			// 5////////////////////////////////////////////////
////			tempfile = ParameterHandler.generateFileName(contextLength[2],
////					amountReturnVals[j], false, true, false, false);
////			nameQrels = filenameQrels + tempfile;
////			nameResult = filenameResult + tempfile;
////			StartEvaluation.contextArea = contextLength[2];
////			StartEvaluation.qrelsFile = nameQrels;
////			StartEvaluation.resultFile = nameResult;
////			eval = new StartEvaluation(false, true, false, false);
////			currentAmountReturnVal = amountReturnVals[j];
////			eval.start();
//
//			// 6////////////////////////////////////////////////
//			tempfile = ParameterHandler.generateFileName(contextLength[3],
//					amountReturnVals[j], false, true, false, true);
//			nameQrels = filenameQrels + tempfile;
//			nameResult = filenameResult + tempfile;
//			StartEvaluation.contextArea = contextLength[3];
//			StartEvaluation.qrelsFile = nameQrels;
//			StartEvaluation.resultFile = nameResult;
//			eval = new StartEvaluation(false, true, false, true);
//			currentAmountReturnVal = amountReturnVals[j];
//			eval.start();
//
//			// 7////////////////////////////////////////////////
////			tempfile = ParameterHandler.generateFileName(contextLength[2],
////					amountReturnVals[j], false, true, true, false);
////			nameQrels = filenameQrels + tempfile;
////			nameResult = filenameResult + tempfile;
////			StartEvaluation.contextArea = contextLength[1];
////			StartEvaluation.qrelsFile = nameQrels;
////			StartEvaluation.resultFile = nameResult;
////			eval = new StartEvaluation(false, true, true, false);
////			currentAmountReturnVal = amountReturnVals[j];
////			eval.start();
//
//			// 8////////////////////////////////////////////////
//			tempfile = ParameterHandler.generateFileName(contextLength[3],
//					amountReturnVals[j], false, true, true, true);
//			nameQrels = filenameQrels + tempfile;
//			nameResult = filenameResult + tempfile;
//			StartEvaluation.contextArea = contextLength[3];
//			StartEvaluation.qrelsFile = nameQrels;
//			StartEvaluation.resultFile = nameResult;
//			eval = new StartEvaluation(false, true, true, true);
//			currentAmountReturnVal = amountReturnVals[j];
//			eval.start();
//
//			// 9////////////////////////////////////////////////
////			tempfile = ParameterHandler.generateFileName(contextLength[1],
////					amountReturnVals[j], true, false, false, false);
////			nameQrels = filenameQrels + tempfile;
////			nameResult = filenameResult + tempfile;
////			StartEvaluation.contextArea = contextLength[1];
////			StartEvaluation.qrelsFile = nameQrels;
////			StartEvaluation.resultFile = nameResult;
////			eval = new StartEvaluation(true, false, false, false);
////			currentAmountReturnVal = amountReturnVals[j];
////			eval.start();
//
//			// 10////////////////////////////////////////////////
//			 tempfile =
//			ParameterHandler.generateFileName(contextLength[3],
//					amountReturnVals[j], true, false, false, true);
//			nameQrels = filenameQrels + tempfile;
//			nameResult = filenameResult + tempfile;
//			StartEvaluation.contextArea = contextLength[3];
//			StartEvaluation.qrelsFile = nameQrels;
//			StartEvaluation.resultFile = nameResult;
//			eval = new StartEvaluation(true, false, false, true);
//			currentAmountReturnVal = amountReturnVals[j];
//			eval.start();
//
//			// 11////////////////////////////////////////////////
////			tempfile = ParameterHandler.generateFileName(contextLength[1],
////					amountReturnVals[j], true, false, true, false);
////			nameQrels = filenameQrels + tempfile;
////			nameResult = filenameResult + tempfile;
////			StartEvaluation.contextArea = contextLength[1];
////			StartEvaluation.qrelsFile = nameQrels;
////			StartEvaluation.resultFile = nameResult;
////			eval = new StartEvaluation(true, false, true, false);
////			currentAmountReturnVal = amountReturnVals[j];
////			eval.start();
//
//			// 12////////////////////////////////////////////////
//			 tempfile =
//			ParameterHandler.generateFileName(contextLength[3],
//					amountReturnVals[j], true, false, true, true);
//			nameQrels = filenameQrels + tempfile;
//			nameResult = filenameResult + tempfile;
//			StartEvaluation.contextArea = contextLength[3];
//			StartEvaluation.qrelsFile = nameQrels;
//			StartEvaluation.resultFile = nameResult;
//			eval = new StartEvaluation(true, false, true, true);
//			currentAmountReturnVal = amountReturnVals[j];
//			eval.start();
//
//			// 13////////////////////////////////////////////////
////			tempfile = ParameterHandler.generateFileName(contextLength[1],
////					amountReturnVals[j], true, true, false, false);
////			nameQrels = filenameQrels + tempfile;
////			nameResult = filenameResult + tempfile;
////			StartEvaluation.contextArea = contextLength[1];
////			StartEvaluation.qrelsFile = nameQrels;
////			StartEvaluation.resultFile = nameResult;
////			eval = new StartEvaluation(true, true, false, false);
////			currentAmountReturnVal = amountReturnVals[j];
////			eval.start();
//
//			// 14////////////////////////////////////////////////
//			tempfile = ParameterHandler.generateFileName(contextLength[3],
//					amountReturnVals[j], true, true, false, true);
//			nameQrels = filenameQrels + tempfile;
//			nameResult = filenameResult + tempfile;
//			StartEvaluation.contextArea = contextLength[3];
//			StartEvaluation.qrelsFile = nameQrels;
//			StartEvaluation.resultFile = nameResult;
//			eval = new StartEvaluation(true, true, false, true);
//			currentAmountReturnVal = amountReturnVals[j];
//			eval.start();
//
//			// 15////////////////////////////////////////////////
////			tempfile = ParameterHandler.generateFileName(contextLength[1],
////					amountReturnVals[j], true, true, true, false);
////			nameQrels = filenameQrels + tempfile;
////			nameResult = filenameResult + tempfile;
////			StartEvaluation.contextArea = contextLength[1];
////			StartEvaluation.qrelsFile = nameQrels;
////			StartEvaluation.resultFile = nameResult;
////			eval = new StartEvaluation(true, true, true, false);
////			currentAmountReturnVal = amountReturnVals[j];
////			eval.start();
//
//			// 16////////////////////////////////////////////////
//			tempfile = ParameterHandler.generateFileName(contextLength[3],
//					amountReturnVals[j], true, true, true, true);
//			nameQrels = filenameQrels + tempfile;
//			nameResult = filenameResult + tempfile;
//			StartEvaluation.contextArea = contextLength[3];
//			StartEvaluation.qrelsFile = nameQrels;
//			StartEvaluation.resultFile = nameResult;
//			eval = new StartEvaluation(true, true, true, true);
//			currentAmountReturnVal = amountReturnVals[j];
//			eval.start();
//		}
//		// }
//	}
//
//	public static String generateFileName(int contextLength,
//			int amountReturnVals, boolean keyword, boolean fuzzy,
//			boolean standardSeacher, boolean entitybased) {
//		String result = "";
//		result += "c" + contextLength + "_";
//		result += "r" + amountReturnVals + "_";
//		result += (keyword) ? "nodesc_" : "desc_";
//		result += (fuzzy) ? "fuzzy_" : "term_";
//		result += (standardSeacher) ? "TFIDF_" : "BM25_";
//		result += (entitybased) ? "entitybased" : "documentbased";
//		result += ".test";
//		// System.out.println(result);
//		return result;
//	}
}
