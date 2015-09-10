package experiments.evaluation;

class ResultEntry {
	
	private String docName;
	
	private double simScore;
	
	public ResultEntry(String docName, double simScore) {
		this.docName = docName;
		this.simScore = simScore;
	}

	public String getDocName() {
//		if(docName.contains("_")) {
//			return docName.split("_")[1];
//		}
		return docName;
	}

	public double getSimScore() {
		return simScore;
	}
}
