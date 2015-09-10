package experiments.collective.entdoccentric;

import java.util.LinkedList;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import experiments.collective.entdoccentric.StandardQueryDataObject.EntityObject;

/**
 * Startklasse zur Evaluierung der Disambiguierungen
 * 
 * @author zwicklbauer
 * 
 */
public class StartEvaluation {

	@Option(name = "-json", usage = "Sets the calbc json file")
	public static String calbcJSON;

	@Option(name = "-qrels", usage = "Output path of qrel file")
	public static String qrelsFile;

	@Option(name = "-result", usage = "Output path of result file")
	public static String resultFile;
 
	public static int returnAmount = 1;

	public static int contextArea;

	private QueryDataGeneration dataGeneration;

	private ResultProcessing resultProcessing;

	private int queryNumber;
	
	private CollectiveTestApproach coll;

	public StartEvaluation(boolean fuzzy, boolean withDescription,
			boolean standardSearcher, boolean entitybased) {
		queryNumber = 0;
		StartupInformationLoader.initializeDisambiguationFramework();
		dataGeneration = new CompleteCalbCSGeneration();
		coll = new CollectiveTestApproach(fuzzy,
				standardSearcher, withDescription);
		
		this.resultProcessing = new TrecEvalResultProcessing();
	}

	public void start() {
		StandardQueryDataObject object;
		int counter = 0;
		while ((object = dataGeneration.hasNext()) != null) {
			long time = System.currentTimeMillis();
			workChain(object);
			counter++;
			System.out.println("Querytime: "
					+ (System.currentTimeMillis() - time));
		}
	}

	@SuppressWarnings("deprecation")
	@Option(name = "-area", usage = "Contextarea of description")
	public void setContextArea(String area) throws CmdLineException {
		try {
			contextArea = Integer.parseInt(area);
		} catch (NumberFormatException e) {
			throw new CmdLineException(
					"Not able to parse contextArea. Must be an int.");
		}
	}

	@SuppressWarnings("deprecation")
	@Option(name = "-r", usage = "Contextarea of description")
	public void setReturnType(String amountReturns) throws CmdLineException {
		try {
			returnAmount = Integer.parseInt(amountReturns);
		} catch (NumberFormatException e) {
			throw new CmdLineException(
					"Not able to parse contextArea. Must be an int.");
		}
	}

	private void workChain(StandardQueryDataObject object) {
		coll.search(object, queryNumber);
		List<EntityObject> l = object.getEnts(); 
		List<TrecEvalResultObject> resultObjectList = new LinkedList<TrecEvalResultObject>();
		for (int i = 0; i < l.size(); i++) {
			resultObjectList.add(new TrecEvalResultObject());
		}
		coll.configureResultObject(resultObjectList, l);

		for (int i = 0; i < l.size(); i++) {
			resultProcessing.processResult(resultObjectList.get(i));
		}
		queryNumber += resultObjectList.size();
	}

	public static void main(String[] args) {
		// Zu Testzwecken
		StartEvaluation.calbcJSON = "/home/quh/Arbeitsfläche/Entpackung/Arbeitsfläche/Code_Data/Calbc/output.json";
		StartEvaluation.qrelsFile = "/home/quh/Arbeitsfläche/Misc/Evaluation/qrels/qrels.test";
		StartEvaluation.resultFile = "/home/quh/Arbeitsfläche/Misc/Evaluation/result/qrels.test";
		StartEvaluation evaluation = new StartEvaluation(false, true, true,
				true);
		CmdLineParser parser = new CmdLineParser(evaluation);
		parser.setUsageWidth(80);
		try {
			parser.parseArgument(args);
			if (calbcJSON == null || qrelsFile == null || resultFile == null) {
				throw new CmdLineException(parser, "No argument is given");
			}
		} catch (CmdLineException e) {
			System.err.println(e.getStackTrace());
			System.err.println("java SampleMain [options...] arguments...");
			parser.printUsage(System.err);
			System.err.println();

			return;
		}
		evaluation.start();
	}

}
