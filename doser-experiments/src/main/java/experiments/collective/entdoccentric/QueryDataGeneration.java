package experiments.collective.entdoccentric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;

import experiments.collective.entdoccentric.calbc.Concept;
import experiments.collective.entdoccentric.filter.Filter;
import experiments.evaluation.UnicodeBOMInputStream;

public abstract class QueryDataGeneration {

	protected File jsonFile;

	protected BufferedReader bufferedReader;

	protected Gson gson;

	protected Filter filter;

	public QueryDataGeneration() {
		initialize();
	}

	public QueryDataGeneration(Filter filter) {
		initialize();
		this.filter = filter;
	}

	private void initialize() {
		if (StartEvaluation.calbcJSON == null) {
			// StartEvaluation.calbcJSON =
			// "/home/quh/Arbeitsfläche/Code_Data/Calbc/wrongoutput_10p_corrected.json";
			StartEvaluation.calbcJSON = "/home/quh/Arbeitsfläche/Code_Data/Calbc/output.json";
		}
		jsonFile = new File(StartEvaluation.calbcJSON);
		gson = new Gson();
		try {
			FileInputStream fis = new FileInputStream(jsonFile);
			UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(fis);
			InputStreamReader isr = new InputStreamReader(ubis);
			bufferedReader = new BufferedReader(isr);
			ubis.skipBOM();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected LinkedList<String> setResultLinks(StandardQueryDataObject obj,
			List<Concept> concepts) {
		LinkedList<String> result = new LinkedList<String>();
		for (Concept concept : concepts) {
			String[] splitter = concept.getUrl().split(":");

			String link = "";
			if (splitter[1].equalsIgnoreCase("uniprot")
					&& !splitter[2].equalsIgnoreCase("") && splitter[2] != null) {
				link = "UN_" + splitter[2];
			} else if (splitter[1].equalsIgnoreCase("entrezgene")
					&& !splitter[2].equalsIgnoreCase("") && splitter[2] != null) {
				link = "NC_" + splitter[2];
			} else if (splitter[1].equalsIgnoreCase("umls")
					&& !splitter[2].equalsIgnoreCase("") && splitter[2] != null) {
				link = "LI_" + splitter[2];
			} else if (splitter[1].equalsIgnoreCase("ncbi")
					&& !splitter[2].equalsIgnoreCase("") && splitter[2] != null) {
				link = "NC_" + splitter[2];
			} else if (splitter[1].equalsIgnoreCase("disease")
					&& !splitter[2].equalsIgnoreCase("") && splitter[2] != null) {
				link = "LI_" + splitter[2];
			}
			if (!link.equalsIgnoreCase("")) {
				result.add(link);
			}

		}
		return result;
	}

	protected String extractText(int position, String text) {
		long startArea = position - StartEvaluation.contextArea;
		long endArea = position + StartEvaluation.contextArea;
		// System.out.println(StartEvaluation.contextArea);
		if (startArea < 0) {
			startArea = 0;
		}
		if (endArea > text.length() - 1) {
			endArea = text.length() - 1;
		}
		String tempText = text.substring((int) startArea, (int) endArea);
		String[] splitter = tempText.split(" ");
		String result = "";
		for (int i = 1; i < splitter.length - 1; i++) {
			result += splitter[i] + " ";
		}
		// System.out.println(result);
		return result;
	}

	public abstract StandardQueryDataObject hasNext();
}
