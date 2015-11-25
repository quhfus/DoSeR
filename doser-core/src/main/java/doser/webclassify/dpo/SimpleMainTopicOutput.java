package doser.webclassify.dpo;

import java.util.List;

public class SimpleMainTopicOutput {

	private String uri;
	
	private String label;
	
	private List<String> categories;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
}
