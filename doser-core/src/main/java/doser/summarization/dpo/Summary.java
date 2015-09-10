package doser.summarization.dpo;

import java.util.List;

public class Summary {

	private String label;

	private String summary; // NOPMD by quh on 13.02.14 12:19

	private List<String> type;

	private String uri;

	public String getLabel() {
		return this.label;
	}

	public String getSummary() {
		return this.summary;
	}

	public List<String> getType() {
		return this.type;
	}

	public String getUri() {
		return this.uri;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public void setSummary(final String summary) {
		this.summary = summary;
	}

	public void setType(final List<String> type) {
		this.type = type;
	}

	public void setUri(final String uri) {
		this.uri = uri;
	}

}
