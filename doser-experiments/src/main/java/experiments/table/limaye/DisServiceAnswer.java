package experiments.table.limaye;

/**
 * Represents a disservice-answer.
 */
public class DisServiceAnswer {
    
    private String documentUri;
    private DisServiceAnswerResult[] disambiguatedSurfaceforms;
    
    /**
     * Returns the documentUri.
     * @return the documentUri
     */
    public String getDocumentUri() {
        return documentUri;
    }
    
    /**
     * Returns the disambiguated surface forms.
     * @return the disambiguated surface forms.
     */
    public DisServiceAnswerResult[] getDisambiguatedSurfaceforms() {
        return disambiguatedSurfaceforms;
    }

}
