package experiments.evaluation;

public class CorrectEntry {

	private String docName;

	private int priority;
	
	public CorrectEntry(String docName, int priority) {
		this.priority = priority;
		this.docName = docName;
	}
	
	public String getDocName() {
//		if(docName.contains("_")) {
//			return docName.split("_")[1];
//		}
		return docName;
	}

	public int getPriority() {
		return priority;
	}

}
