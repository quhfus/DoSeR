package doser.entitydisambiguation.table.logic;

import java.util.List;

import doser.entitydisambiguation.table.dpo.TableCell;

public class TableDisambiguationTask {

	private final List<TableCell> cellList;

	private final String documentId;

	private final String header;

	private final String typeGroundtruth;

	public TableDisambiguationTask(final String documentId,
			final List<TableCell> cellList, final String typeGroundtruth,
			final String header) {
		super();
		this.documentId = documentId;
		this.cellList = cellList;
		this.typeGroundtruth = typeGroundtruth;
		this.header = header;
	}

	public List<TableCell> getCellList() {
		return this.cellList;
	}

	public String getDocumentId() {
		return this.documentId;
	}

	public String getHeader() {
		return this.header;
	}

	public String getTypeGroundtruth() {
		return this.typeGroundtruth;
	}

}
