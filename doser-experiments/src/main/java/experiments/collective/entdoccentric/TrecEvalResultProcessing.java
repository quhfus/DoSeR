package experiments.collective.entdoccentric;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Ausgabeklasse welche die Ergebnisse in die jeweiligen Trec_Eval Dateien
 * schreibt.
 * 
 * @author zwicklbauer
 * 
 */
public class TrecEvalResultProcessing implements ResultProcessing {

	private File resultFile;

	private File optimalResultFile;

	private Writer outResult;

	private Writer outOptimalResult;

	public TrecEvalResultProcessing() {
		resultFile = new File(StartEvaluation.resultFile);
		optimalResultFile = new File(StartEvaluation.qrelsFile);
		try {
			outResult = new FileWriter(resultFile, false);
			outOptimalResult = new FileWriter(optimalResultFile, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processResult(TrecEvalResultObject object) {
		String[] resultMatrix = object.getResult();
		String line = "";
		for (int i = 0; i < resultMatrix.length; i++) {
			line += resultMatrix[i]+" ";
		}
		System.out.println(line);
		writeResultLine(line);

		// Write optimal solution
		String[][] optimalResultMatrix = object.getOptimalResult();
		for (int i = 0; i < optimalResultMatrix.length; i++) {
			line = "";
			for (int j = 0; j < optimalResultMatrix[i].length; j++) {
				line += optimalResultMatrix[i][j] + " ";
			}
			System.out.println("Musterlösung: "+line);
			writeOptimalResultLine(line);
		}
	}

	private void writeResultLine(String line) {
		try {
			outResult.write(line);
			outResult.write(System.getProperty("line.separator"));
			outResult.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeOptimalResultLine(String line) {
		try {
			outOptimalResult.write(line);
			outOptimalResult.write(System.getProperty("line.separator"));
			outOptimalResult.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		try {
			outResult.close();
			outOptimalResult.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
