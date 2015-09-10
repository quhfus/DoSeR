package experiments.evaluation;

import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public class M_Recall extends StatisticalMeasure {

	private SummaryStatistics sumStats;
	
	private int correctHits;
	
	private int overallQueries;
	
	private HashMap<Integer, Integer> map;
	
	private double overallRes;
	
	private double queryVal;
	
	public M_Recall() {
		super();
		sumStats = new SummaryStatistics();
		queryVal = 0;
		overallQueries = 0;
		correctHits = 0;
		overallRes = 0;
		classname = new String[3];
		classname[0] = "Recall";
		classname[1] = "Recall_StandardDeviation";
		classname[2] = "Recall_Variance";
		map = new HashMap<Integer, Integer>();
	}
	
	@Override
	public void workQuery(CorrectEntry ce, ResultEntry re) {
		if (ce.getDocName().equalsIgnoreCase(re.getDocName())) {
			correctHits++;
		}
		map.put(ce.getDocName().hashCode(), 0);
	}

	@Override
	public void finishQuery(int qryN) {
		queryVal = (double) correctHits / (double) map.size();
		overallRes += queryVal;
		sumStats.addValue(queryVal);
		map = new HashMap<Integer, Integer>();
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
