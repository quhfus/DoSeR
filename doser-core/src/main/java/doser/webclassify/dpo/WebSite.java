package doser.webclassify.dpo;

import java.util.Set;

import doser.entitydisambiguation.table.logic.Type;
import doser.webclassify.algorithm.PageSimilarity;

public class WebSite implements Cloneable, Comparable<WebSite> {

	private String name;
	private String text;
	private int objectId;
	private Set<Type> types;
	private PageSimilarity similarity;

	public WebSite() {
		super();
		this.name = "";
		this.text = "";
	}

	public String getName() {
		return name;
	}

	public PageSimilarity getSimilarity() {
		return similarity;
	}

	public void setSimilarity(PageSimilarity similarity) {
		this.similarity = similarity;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Set<Type> getTypes() {
		return types;
	}

	public void setTypes(Set<Type> types) {
		this.types = types;
	}

	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}


	@Override
	public int compareTo(WebSite o) {
		// System.out.println(this.name + "    "+o.getName());
		if (this.name.equalsIgnoreCase(o.getName())
				&& this.getText().hashCode() == o.getText().hashCode()) {
			return 0;
		} else {
			return -1;
		}
	}
}