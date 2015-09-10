package doser.entitydisambiguation.table.dpo;

import java.util.List;

public class TableDisambiguationResponse {

	private List<ColumnResponseItem> columns;

	private String documentId;

	public List<ColumnResponseItem> getColumns() {
		return this.columns;
	}

	public String getDocumentId() {
		return this.documentId;
	}

	public void setColumns(final List<ColumnResponseItem> columns) {
		this.columns = columns;
	}

	public void setDocumentId(final String documentId) {
		this.documentId = documentId;
	}
}
