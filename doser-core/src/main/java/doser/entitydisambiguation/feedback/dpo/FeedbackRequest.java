package doser.entitydisambiguation.feedback.dpo;

import java.util.List;

/**
 * Class representing user feedback for multiple entities in one document.
 * 
 * @author sech
 * 
 */
public class FeedbackRequest {

	private String documentUri;
	private List<FeedbackItem> feedbackItems;

	public String getDocumentUri() {
		return this.documentUri;
	}

	public List<FeedbackItem> getFeedbackItems() {
		return this.feedbackItems;
	}

	public void setDocumentUri(final String documentUri) {
		this.documentUri = documentUri;
	}

	public void setFeedbackItems(final List<FeedbackItem> feedbackItems) {
		this.feedbackItems = feedbackItems;
	}

}
