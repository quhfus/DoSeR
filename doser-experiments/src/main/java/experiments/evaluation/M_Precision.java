package experiments.evaluation;

import java.util.HashMap;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public class M_Precision extends StatisticalMeasure {

	private SummaryStatistics sumStats;

	private HashMap<Integer, Integer> itemsRetrieved;

	private int correctHits;

	private double overallRes;

	private int overallQueries;

	private double queryVal;

	public M_Precision() {
		super();
		sumStats = new SummaryStatistics();
		correctHits = 0;
		queryVal = 0;
		overallRes = 0;
		overallQueries = 0;
		itemsRetrieved = new HashMap<Integer, Integer>();
		classname = new String[3];
		classname[0] = "Precision";
		classname[1] = "Precision_StandardDeviation";
		classname[2] = "Precision_Variance";
	}

	@Override
	public void workQuery(CorrectEntry ce, ResultEntry re) {
		if (ce.getDocName().equalsIgnoreCase(re.getDocName())) {
			correctHits++;
		}
		itemsRetrieved.put(re.getDocName().hashCode(), 0);
		
	}

	@Override
	public void finishQuery(int qryN) {
		queryVal = (double) correctHits / (double) itemsRetrieved.size();
		overallRes += queryVal;
		sumStats.addValue(queryVal);
		itemsRetrieved = new HashMap<Integer, Integer>();
		correctHits = 0;
		overallQueries++;
	}

	@Override
	public double[] getResult() {
		double[] result = new double[3];
		result[0] = overallRes / overallQueries;
		result[1] = sumStats.getStandardDeviation();
		result[2] = sumStats.getVariance();
		return result;
	}

	@Override
	public double[] getQueryResult() {
		double[] result = new double[3];
		result[0] = queryVal;
		result[1] = 0;
		result[2] = 0;
		return result;
	}

}
