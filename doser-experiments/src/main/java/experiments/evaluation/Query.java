package experiments.evaluation;

import java.util.LinkedList;
import java.util.List;

class Query {

	private int qryNr;
	
	private List<CorrectEntry> correctEntries;

	private List<ResultEntry> resultEntries;
	
	Query() {
		correctEntries = new LinkedList<CorrectEntry>();
		resultEntries = new LinkedList<ResultEntry>();
	}

	void addCorrectEntry(CorrectEntry entry) {
//		System.out.println("add Correctentry: "+entry.getDocName());
		this.correctEntries.add(entry);
	}
	
	void addResultEntry(ResultEntry entry) {
//		System.out.println("add Resultentry: "+entry.getDocName());
		this.resultEntries.add(entry);
	}
	
	List<CorrectEntry> getCorrectEntries() {
		return correctEntries;
	}
	
	List<ResultEntry> getResultEntries() {
		return resultEntries;
	}

	int getQryNr() {
		return qryNr;
	}

	void setQryNr(int qryNr) {
		this.qryNr = qryNr;
	}
	
	boolean hasTestResults() {
		if(resultEntries.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
}
