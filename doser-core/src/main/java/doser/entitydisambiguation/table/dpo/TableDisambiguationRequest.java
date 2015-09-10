package doser.entitydisambiguation.table.dpo;

import java.util.List;

public class TableDisambiguationRequest {

	private List<TableColumn> columnList;

	private String documentId;

	private String tableName;

	public List<TableColumn> getColumnList() {
		return this.columnList;
	}

	public String getDocumentId() {
		return this.documentId;
	}

	public String getTableName() {
		return this.tableName;
	}

	public void setColumnList(final List<TableColumn> columnList) {
		this.columnList = columnList;
	}

	public void setDocumentId(final String documentId) {
		this.documentId = documentId;
	}

	public void setTableName(final String tableName) {
		this.tableName = tableName;
	}
}
