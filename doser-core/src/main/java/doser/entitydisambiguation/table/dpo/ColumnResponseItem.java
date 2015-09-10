package doser.entitydisambiguation.table.dpo;

import java.util.LinkedList;
import java.util.List;

public class ColumnResponseItem {

	private List<CellResponse> cells;

	private List<String> columnTypes;

	private String name;

	private String typeUrl;

	public List<CellResponse> getCells() {
		return this.cells;
	}

	public List<String> getColumnTypes() {
		return this.columnTypes;
	}

	// public String getTypeUrl() {
	// return typeUrl;
	// }
	//
	// public void setTypeUrl(String typeUrl) {
	// this.typeUrl = typeUrl;
	// }

	public String getName() {
		return this.name;
	}

	public void setCells(final List<CellResponse> cells) {
		this.cells = cells;
	}

	public void setColumnTypes(final List<String> columnTypes) {
		this.columnTypes = columnTypes;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setNewCellResponse(final String text, final String uri) {
		if (this.cells == null) {
			this.cells = new LinkedList<CellResponse>();
		}
		final CellResponse r = new CellResponse();
		r.setText(text);
		r.setUri(uri);
		this.cells.add(r);
	}

}
