package AidaDatasetEvaluation;


import java.util.HashSet;
import java.util.Set;

/**
 * Class representing a disambiguated entity consisting of the entity mention
 * (the text), the identified URI, a value representing the confidence for the
 * decision, and a entity description. Class is a POJO for automatic
 * (de-)serialization. TODO may not be complete (e.g. relevant terms may be
 * added)
 * 
 * @author zwicklbauer
 * 
 */
public class DisambiguatedEntity {

	private String text;
	private String entityUri;
	private String description;
	private double confidence;
	private Set<Type> categories;

	public DisambiguatedEntity() {
		super();
		this.categories = new HashSet<Type>();
		this.confidence = -1;
		this.description = "";
		this.entityUri = "";
		this.text = "";
	}

	public DisambiguatedEntity(final String text, final String entityUri,
			final double confidence, final String description) {
		this.text = text;
		this.entityUri = entityUri;
		this.confidence = confidence;
		this.description = description;
	}

	public Set<Type> getCategories() {
		return this.categories;
	}

	public double getConfidence() {
		return this.confidence;
	}

	public String getDescription() {
		return this.description;
	}

	public String getEntityUri() {
		return this.entityUri;
	}

	public String getText() {
		return this.text;
	}

	public void setCategories(final Set<Type> categories) {
		this.categories = categories;
	}

	public void setConfidence(final double confidence) {
		this.confidence = confidence;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setEntityUri(final String entityUri) {
		this.entityUri = entityUri;
	}

	public void setText(final String text) {
		this.text = text;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DisambiguatedEntity) {
			DisambiguatedEntity compObj = (DisambiguatedEntity) obj;
			if (this.confidence == compObj.confidence
					&& this.description.equalsIgnoreCase(compObj
							.getDescription())
					&& this.text.equalsIgnoreCase(compObj.getText())
					&& this.entityUri.equalsIgnoreCase(compObj.getEntityUri())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ((int) this.confidence + this.entityUri.hashCode()
				+ this.text.hashCode() + this.description.hashCode());
	}
}
