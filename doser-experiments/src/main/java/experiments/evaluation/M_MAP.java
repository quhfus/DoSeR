package experiments.evaluation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class M_MAP extends StatisticalMeasure {
	
	private HashMap<String, Integer> retrievedDocsInQuery;
	
	private HashMap<String, Integer> correctRetrievedDocs;
	
	private HashMap<String, Integer> correctRelevantDocs;
	
	private List<String> docList;
	
	private double queryMap;
	
	private double overallAveragePrecision;
	
	private int amountQueries;
	
	public M_MAP() {
		super();
		retrievedDocsInQuery = new HashMap<String, Integer>();
		correctRetrievedDocs = new HashMap<String, Integer>();
		correctRelevantDocs = new HashMap<String, Integer>();
		docList = new LinkedList<String>();
		queryMap = 0;
		overallAveragePrecision = 0;
		amountQueries = 0;
		classname = new String[1];
		classname[0] = "MAP";
	}

	@Override
	public void workQuery(CorrectEntry ce, ResultEntry re) {
		if (ce.getDocName().equalsIgnoreCase(re.getDocName())) {
			correctRetrievedDocs.put(re.getDocName(), 0);
		}
		docList.add(re.getDocName());
		retrievedDocsInQuery.put(re.getDocName(), 0);
		correctRelevantDocs.put(ce.getDocName(), 0);
	}

	@Override
	public void finishQuery(int qryN) {
		amountQueries++;
		double averagePrecision = 0;	
		
		// Query Average Precision
		int it = 0;
		for (int i = 0; i < retrievedDocsInQuery.size(); i++) {
			String name = docList.get(i);
			for (Map.Entry<String, Integer> entry : correctRetrievedDocs.entrySet()) {
			    String key = entry.getKey();
			    if(name.equalsIgnoreCase(key)) {
			    	it++;
			    	averagePrecision += ((double) it) / ((double)(i + 1)); 
			    	break;
			    }
			}
		}
		if(it == 0) {
			averagePrecision = 0;
		} else {
			averagePrecision /= (double) correctRelevantDocs.size();
		}
		queryMap = averagePrecision;
		overallAveragePrecision += averagePrecision;
		averagePrecision = 0;
		retrievedDocsInQuery = new HashMap<String, Integer>();
		correctRetrievedDocs = new HashMap<String, Integer>();
		correctRelevantDocs = new HashMap<String, Integer>();
		docList = new LinkedList<String>();
	}

	@Override
	public double[] getResult() {
		double[] result = new double[1];
		result[0] = overallAveragePrecision / (double)amountQueries;
		return result;
	}

	@Override
	public double[] getQueryResult() {
		double[] result = new double[1];
		result[0] = queryMap;
		return result;
	}
}