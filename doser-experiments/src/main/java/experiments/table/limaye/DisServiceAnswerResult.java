package experiments.table.limaye;

/**
 * Represents the result of a disservice-answer.
 */
public class DisServiceAnswerResult {

    private String selectedText;
    private DisServiceAnswerResultEntities[] disEntities;

    /**
     * Returns the selected text.
     * 
     * @return the selected text
     */
    public String getSelectedText() {
        return selectedText;
    }

    /**
     * Returns the disambiguated entities.
     * 
     * @return the disambiguated entities
     */
    public DisServiceAnswerResultEntities[] getDisEntities() {
        return disEntities;
    }
}
