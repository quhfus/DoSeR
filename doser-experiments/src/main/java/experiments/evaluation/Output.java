package experiments.evaluation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class Output {

	private List<StatisticalMeasure> measures;

	private CSVWriter writer;
	
	private CSVWriter writeSingleQuery;

	public Output() {
		try {
			writer = new CSVWriter(new FileWriter(StartEvaluation.csvFile), '\t');
			if(StartEvaluation.singleQueryFile != null) {
				writeSingleQuery = new CSVWriter(new FileWriter(StartEvaluation.singleQueryFile), '\t');
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		measures = new LinkedList<StatisticalMeasure>();
	}

	public void print() {
		System.out.println("---SYSTEM OUTPUT---");
		for (int i = 0; i < measures.size(); i++) {
			String[] names = measures.get(i).getNames();
			for (int j = 0; j < names.length; j++) {
				double[] vals = measures.get(i).getResult();
				System.out.println(names[j] + ": " + vals[j]);
			}
		}
	}

	public void printCSVHeader() {
		List<String> strings = new LinkedList<String>();
		strings.add("Experiment");
		for (int i = 0; i < measures.size(); i++) {
			String[] names = measures.get(i).getNames();
			for (int j = 0; j < names.length; j++) {
				strings.add(names[j]);
			}
		}
		String[] arr = new String[strings.size()];
		strings.toArray(arr);
		writer.writeNext(arr);
	}

	public void printCSV(String experiment) {
		List<String> vals = new LinkedList<String>();
		vals.add(experiment);
		for (int i = 0; i < measures.size(); i++) {
			double[] numbers = measures.get(i).getResult();
			for (int j = 0; j < numbers.length; j++) {
				vals.add(String.valueOf(numbers[j]));
			}
		}
		String[] arr = new String[vals.size()];
		vals.toArray(arr);
		writer.writeNext(arr);
		measures.clear();
	}

	public void addMeasure(StatisticalMeasure me) {
		measures.add(me);
	}
	
	public void printSingleQueryHeader() {
		List<String> strings = new LinkedList<String>();
		strings.add("Querynumber");
		for (int i = 0; i < measures.size(); i++) {
			String[] names = measures.get(i).getNames();
			for (int j = 0; j < names.length; j++) {
				strings.add(names[j]);
			}
		}
		String[] arr = new String[strings.size()];
		strings.toArray(arr);
		writeSingleQuery.writeNext(arr);
		measures.clear();
	}

	public void writeSingleQuery(String query) {
		List<String> lst = new LinkedList<String>();
		lst.add(query);
		for (int i = 0; i < measures.size(); i++) {
			double[] numbers = measures.get(i).getQueryResult();
			for (int j = 0; j < numbers.length; j++) {
				lst.add(String.valueOf(numbers[j]));
			}
		}
		String[] arr = new String[lst.size()];
		lst.toArray(arr);
		writeSingleQuery.writeNext(arr);
	}
	
	public void clearMeasures() {
		measures.clear();
	}
	
	public void close() {
		try {
			writer.close();
			if(writeSingleQuery != null) {
				writeSingleQuery.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
