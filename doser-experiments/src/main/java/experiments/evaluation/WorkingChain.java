package experiments.evaluation;

import java.util.LinkedList;
import java.util.List;

public class WorkingChain {

	private List<StatisticalMeasure> measures;

	private boolean queryOutput;
	
	private Output out;
	
	public WorkingChain(boolean queryOutput) {
		measures = new LinkedList<StatisticalMeasure>();
		this.queryOutput = queryOutput;
	}
	
	public WorkingChain(boolean queryOutput, Output out) {
		measures = new LinkedList<StatisticalMeasure>();
		this.queryOutput = queryOutput;
		this.out = out;
	}

	public void work(Query query) {
		List<CorrectEntry> ceList = query.getCorrectEntries();
		List<ResultEntry> reList = query.getResultEntries();
		for (int i = 0; i < ceList.size(); i++) {
			for (int j = 0; j < reList.size(); j++) {
				for (int j2 = 0; j2 < measures.size(); j2++) {
					StatisticalMeasure me = measures.get(j2);
					me.qrelsIeration = i + 1;
					me.resultIteration = j + 1;
					me.workQuery(ceList.get(i), reList.get(j));
				}
			}
		}

		if (query.hasTestResults()) {
			for (int i = 0; i < measures.size(); i++) {
				measures.get(i).finishQuery(query.getQryNr());
			}
			if(queryOutput) {
				out.writeSingleQuery(String.valueOf(query.getQryNr()));
			}
		}
	}

	public void addMeasurement(StatisticalMeasure measure) {
		this.measures.add(measure);
	}

	public List<StatisticalMeasure> getMeasures() {
		return measures;
	}

}
