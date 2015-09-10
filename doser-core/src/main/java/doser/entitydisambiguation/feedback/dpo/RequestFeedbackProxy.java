package doser.entitydisambiguation.feedback.dpo;

import java.util.List;

public class RequestFeedbackProxy {

	private String currentFamily;

	private String docId;

	private List<FeedbackItem> feedbackItems;

	private String operation;

	private String rowKey;

	private String tableName;

	private String[] uniqueEntityUri;

	private String context;
	
	private String surfaceForms;
	
	public RequestFeedbackProxy() {
		super();
		this.context = "";
	}
	
	public String getCurrentFamily() {
		return this.currentFamily;
	}

	public String getDocId() {
		return this.docId;
	}

	public List<FeedbackItem> getFeedbackItems() {
		return this.feedbackItems;
	}

	public String getOperation() {
		return this.operation;
	}

	public String getRowKey() {
		return this.rowKey;
	}

	public String getTableName() {
		return this.tableName;
	}

	public String[] getUniqueEntityUri() {
		return this.uniqueEntityUri;
	}

	public void setCurrentFamily(final String currentFamily) {
		this.currentFamily = currentFamily;
	}

	public void setDocId(final String docId) {
		this.docId = docId;
	}

	public void setFeedbackItems(final List<FeedbackItem> feedbackItems) {
		this.feedbackItems = feedbackItems;
	}

	public void setOperation(final String operation) {
		this.operation = operation;
	}

	public void setRowKey(final String rowKey) {
		this.rowKey = rowKey;
	}

	public void setTableName(final String tableName) {
		this.tableName = tableName;
	}

	public void setUniqueEntityUri(final String[] uniqueEntityUri) {
		this.uniqueEntityUri = uniqueEntityUri;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getSurfaceForms() {
		return surfaceForms;
	}

	public void setSurfaceForms(String surfaceForms) {
		this.surfaceForms = surfaceForms;
	}
	
	
}
