package doser.lda;

public class LDAJsonFormat {

	private String query;
	private String[] documents;

	public LDAJsonFormat() {
		super();
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String[] getDocuments() {
		return documents;
	}

	public void setDocuments(String[] documents) {
		this.documents = documents;
	}
}