package experiments.evaluation;

import java.util.HashMap;
import java.util.Map;

public class M_Accuracy extends StatisticalMeasure {

	// Accuracy computation needs the true negativ value. Therefore the overall
	// amount of possible documents is needed
	// tn + fp + tp + fn
	public static final int amountOverallDocs = 132;

	// tp
	private double relevantDocsinQuery;

	// tp + fn
	private HashMap<Integer, Integer> amountCorrectEntries;

	// fp
	private HashMap<Integer, Integer> falsePositives;

	private double queryacc = 0;

	private double overallAcc;

	private int amountQueries;

	public M_Accuracy() {
		super();
		relevantDocsinQuery = 0;
		amountCorrectEntries = new HashMap<Integer, Integer>();
		falsePositives = new HashMap<Integer, Integer>();
		overallAcc = 0;
		queryacc = 0;
		amountQueries = 0;
		classname = new String[1];
		classname[0] = "Accuracy";
	}

	@Override
	public void workQuery(CorrectEntry ce, ResultEntry re) {
		if (ce.getDocName().equalsIgnoreCase(re.getDocName())) {
			relevantDocsinQuery++;
			falsePositives.put(re.hashCode(), 1);
		} else {
			if (falsePositives.get(re.hashCode()) == null) {
				falsePositives.put(re.hashCode(), 0);
			}
		}
		amountCorrectEntries.put(ce.hashCode(), 0);
	}

	@Override
	public void finishQuery(int qryN) {
		double falsePositivesValues = 0;
		for (Map.Entry<Integer, Integer> item : falsePositives.entrySet()) {
			if (item.getValue() == 0) {
				falsePositivesValues++;
			}
		}

		double tn = amountOverallDocs - amountCorrectEntries.size()
				- falsePositivesValues;
		queryacc = (relevantDocsinQuery + tn) / (double)(amountOverallDocs);
		overallAcc += queryacc;
		amountQueries++;
		amountCorrectEntries = new HashMap<Integer, Integer>();
		falsePositives = new HashMap<Integer, Integer>();
		relevantDocsinQuery = 0;
	}

	@Override
	public double[] getResult() {
		double[] result = new double[1];
		result[0] = (overallAcc / (double) amountQueries);
		return result;
	}

	@Override
	public double[] getQueryResult() {
		double result[] = new double[1];
		result[0] = queryacc;
		return result;
	}

}
