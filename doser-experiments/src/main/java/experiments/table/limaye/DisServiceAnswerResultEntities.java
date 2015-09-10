package experiments.table.limaye;

/**
 * Represents the entities of a result of a disservice-answer.
 */
public class DisServiceAnswerResultEntities {

    private String text;
    private String entityUri;
    private double confidence;

    /**
     * Returns the text.
     * 
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the entity-uri.
     * 
     * @return the entity-uri
     */
    public String getEntityUri() {
        return entityUri;
    }
    
    

    public void setEntityUri(String entityUri) {
		this.entityUri = entityUri;
	}

	/**
     * Returns the confidence.
     * 
     * @return the confidence
     */
    public double getConfidence() {
        return confidence;
    }
}
