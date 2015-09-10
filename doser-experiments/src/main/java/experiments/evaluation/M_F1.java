package experiments.evaluation;

import java.util.HashMap;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public class M_F1 extends StatisticalMeasure {

	private SummaryStatistics sumStats;
	
	// Precision Vals
	
	private HashMap<Integer, Integer> itemsRetrieved;

	private int correctHits;

	private double overallRes;

	private int overallQueries;

	private double queryVal;
	
	// Recall Vals
	private HashMap<Integer, Integer> map;
	
	
	public M_F1() {
		super();
		sumStats = new SummaryStatistics();
		//Precision
		correctHits = 0;
		queryVal = 0;
		overallRes = 0;
		overallQueries = 0;
		itemsRetrieved = new HashMap<Integer, Integer>();
		
		//Recall
		map = new HashMap<Integer, Integer>();
		
		classname = new String[3];
		classname[0] = "F1";
		classname[1] = "F1_StandardDeviation";
		classname[2] = "F1_Variance";
	}
	
	@Override
	public void workQuery(CorrectEntry ce, ResultEntry re) {
		// Precision 
		if (ce.getDocName().equalsIgnoreCase(re.getDocName())) {
			correctHits++;
		}
		itemsRetrieved.put(re.getDocName().hashCode(), 0);
		map.put(ce.getDocName().hashCode(), 0);
	}

	@Override
	public void finishQuery(int qryN) {
		// Precision
		double precqueryVal = (double) correctHits / (double) itemsRetrieved.size();
		sumStats.addValue(queryVal);
		itemsRetrieved = new HashMap<Integer, Integer>();
		
		// Recall
		double queryValRec = (double) correctHits / (double) map.size();
		
		System.out.println(queryValRec + "       "+precqueryVal);
		
		// F1
		double f1 = 2 * ((precqueryVal * queryValRec) / (precqueryVal + queryValRec+0.000001));
		overallRes += f1;
		sumStats.addValue(f1);
		map = new HashMap<Integer, Integer>();
		
		correctHits = 0;
		overallQueries++;
	}

	@Override
	public double[] getResult() {
		// Precision
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
