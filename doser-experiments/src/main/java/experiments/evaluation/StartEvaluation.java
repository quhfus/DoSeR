package experiments.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class StartEvaluation {

	@Option(name = "-s", usage = "outputfile of every single query (csv)")
	public static String singleQueryFile;

	@Option(name = "-csv", usage = "csv file")
	public static String csvFile;

	@Option(name = "-p", usage = "print every query")
	public static boolean printEveryQuery;

	@Option(name = "-qrels", usage = "filepath of qrels directory")
	public static String qrels;

	@Option(name = "-result", usage = "filepath of result directory")
	public static String result;

	@Option(name = "-m", usage = "Measures")
	public static String measures;

	private Output out;

	public StartEvaluation() {
	}

	public void initialize() {
		out = new Output();
		if (measures.equalsIgnoreCase("all")) {
			List<StatisticalMeasure> me = createMeasureList();
			for (int i = 0; i < me.size(); i++) {
				out.addMeasure(me.get(i));
			}
			out.printCSVHeader();
			if(printEveryQuery) {
				out.printSingleQueryHeader();
			}
			out.clearMeasures();
		}
	}

	private List<StatisticalMeasure> createMeasureList() {
		List<StatisticalMeasure> lst = new LinkedList<StatisticalMeasure>();
		if (measures.equalsIgnoreCase("all")) {
			lst.add(new M_ReciprocalRank());
			lst.add(new M_Recall());
			lst.add(new M_Precision());
			lst.add(new M_MAP());
			lst.add(new M_F1());
			lst.add(new M_Accuracy2());
		}
		return lst;
	}

	public void process() {
		File qrelDir = new File(qrels);
		String[] qrelFiles = qrelDir.list();
		for (int i = 0; i < qrelFiles.length; i++) {
			File resFile = new File(result + "" + qrelFiles[i]);
			if (!resFile.exists()) {
				System.err.println("File " + result + "" + qrelFiles[i]
						+ " not found!");
				break;
			}
			WorkingChain chain = null;
			if(printEveryQuery) {
				chain = new WorkingChain(true, out);
			} else {
				chain = new WorkingChain(false);
			}
			List<StatisticalMeasure> me = createMeasureList();
			for (int j = 0; j < me.size(); j++) {
				chain.addMeasurement(me.get(j));
				out.addMeasure(me.get(j));
			}
			try {
				FilePreProcessing proc = new FilePreProcessing(qrels
						+ qrelFiles[i], resFile.getAbsolutePath(), chain);
				proc.process();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (LineParsingException e) {
				e.printStackTrace();
			}
			out.printCSV(qrelFiles[i]);
		}
		out.close();
	}

	public static void main(String[] args) {
		StartEvaluation eval = new StartEvaluation();
		CmdLineParser parser = new CmdLineParser(eval);
		parser.setUsageWidth(80);

		try {
			parser.parseArgument(args);
			if (qrels == null || result == null || measures == null
					|| csvFile == null
					|| (printEveryQuery && singleQueryFile == null)) {
				throw new CmdLineException(parser, "No argument is given");
			}
		} catch (CmdLineException e) {
			System.err.println(e.getStackTrace());
			System.err.println("java SampleMain [options...] arguments...");
			parser.printUsage(System.err);
			System.err.println();
			return;
		}
		eval.initialize();
		eval.process();
	}
}
