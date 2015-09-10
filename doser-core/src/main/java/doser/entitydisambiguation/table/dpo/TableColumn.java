package doser.entitydisambiguation.table.dpo;

import java.util.List;

public class TableColumn {

	private String cellheader;

	private List<TableCell> cellList;

	private String typeGroundtruth;

	public String getCellheader() {
		return this.cellheader;
	}

	public List<TableCell> getCellList() {
		return this.cellList;
	}

	public String getTypeGroundtruth() {
		return this.typeGroundtruth;
	}

	public void setCellheader(final String cellheader) {
		this.cellheader = cellheader;
	}

	public void setCellList(final List<TableCell> cellList) {
		this.cellList = cellList;
	}

	public void setTypeGroundtruth(final String typeGroundtruth) {
		this.typeGroundtruth = typeGroundtruth;
	}
}
