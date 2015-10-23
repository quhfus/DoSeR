package doser.entitydisambiguation.feedback.dpo;


/**
 * Class representing the user feedback for a disambiguated entity. The type of
 * feedback may either be (i) accept (correct suggestion), (ii) reject (wrong
 * suggestion), (iii) no feedback (either no viewed or not able to decide) or
 * (iv) a new concept is added. Thus, the feedback type will be represented by
 * integer values. Value 1 for correct, value -1 for wrong. A new concept is
 * represented by the value 2.
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class FeedbackItem {
	private String entityUri;
	private int startPosition;
	private String selectedText;
	private int typeOfFeedback;

	public String getEntityUri() {
		return this.entityUri;
	}

	public String getSelectedText() {
		return this.selectedText;
	}

	public int getTypeOfFeedback() {
		return this.typeOfFeedback;
	}

	public void setEntityUri(final String entityUri) {
		this.entityUri = entityUri;
	}

	public void setSelectedText(final String selectedText) {
		this.selectedText = selectedText;
	}

	public void setTypeOfFeedback(final int typeOfFeedback) {
		this.typeOfFeedback = typeOfFeedback;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}
}