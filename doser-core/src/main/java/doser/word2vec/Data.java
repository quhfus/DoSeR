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

//	public String getEntity() {
//		return entity;
//	}
//
//	public void setEntity(String entity) {
//		this.entity = entity;
//	}
//
//	@Override
//	public int hashCode() {
//		return surfaceForm.hashCode() + qryNr.hashCode() + context.hashCode()
//				+ entity.hashCode();
//
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		Data data = (Data) obj;
//		if (this.surfaceForm.equals(data.getSurfaceForm())
//				&& this.context.equals(data.getSurfaceForm())
//				&& this.qryNr.equals(data.getQryNr())
//				&& this.entity.equals(data.getEntity())) {
//			return true;
//		}
//		return false;
//	}
}
