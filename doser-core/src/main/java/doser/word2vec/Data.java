package doser.word2vec;

public class Data {

	private String surfaceForm;
	private String qryNr;
	private String[] candidates;
	private String context;
//	private String entity;

	public String getSurfaceForm() {
		return surfaceForm;
	}

	public void setSurfaceForm(String surfaceForm) {
		this.surfaceForm = surfaceForm;
	}

	public String getQryNr() {
		return qryNr;
	}

	public void setQryNr(String qryNr) {
		this.qryNr = qryNr;
	}

	public String[] getCandidates() {
		return candidates;
	}

	public void setCandidates(String[] candidates) {
		this.candidates = candidates;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
}
