package doser.summarization.algorithm;

import java.util.LinkedList;
import java.util.List;

public class SummaryInfos {

	private String label;

	private String summary;

	private final List<String> types;

	private String uri;

	public SummaryInfos() {
		this.types = new LinkedList<String>();
	}

	public void addType(final String type) {
		this.types.add(type);
	}

	public String getLabel() {
		return this.label;
	}

	public String getSummary() {
		return this.summary;
	}

	public List<String> getTypes() {
		return this.types;
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

	public void setUri(final String uri) {
		this.uri = uri;
	}
}
