package experiments.table.limaye;

import java.util.List;

/**
 * Class representing a disambiguated entity consisting of the entity mention
 * (the text), the identified URI, a value representing the confidence for the
 * decision, and a entity description. Class is a POJO for automatic
 * (de-)serialization. 
 * TODO may not be complete (e.g. relevant terms may be
 * added)
 * 
 * @author sech
 * 
 */
public class DisambiguatedEntity {

	private String text;
	private String entityUri;
	private double confidence;
	private String description;
	private List<Category> categories;

	public DisambiguatedEntity() {
	}

	public DisambiguatedEntity(String text, String entityUri,
			double confidence, String description) {
		this.text = text;
		this.entityUri = entityUri;
		this.confidence = confidence;
		this.description = description;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getEntityUri() {
		return entityUri;
	}

	public void setEntityUri(String entityUri) {
		this.entityUri = entityUri;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
}
