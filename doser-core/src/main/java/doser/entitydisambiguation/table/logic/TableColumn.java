package doser.entitydisambiguation.table.logic;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public class TableColumn implements Cloneable {

	private List<TableCell> cellList;

	private final int columnNr;

	private final String header;

	private Type leadingType;

	private final Set<Type> types;

	public TableColumn(final String header, final int columnNr) {
		super();
		this.header = header;
		this.cellList = new LinkedList<TableCell>();
		this.columnNr = columnNr;
		this.types = new LinkedHashSet<Type>();
	}

	public void addCell(final String content) {
		this.cellList.add(new TableCell(content));
	}

	public void addCellGroundTruth(final String groundtruth, final int cellNr) {
		this.cellList.get(cellNr).setGt(groundtruth);
	}

	public void addPossibleType(final Type type) {
		this.types.add(type);
		if (this.leadingType == null) {
			this.leadingType = type;
		} else {
			for (final Type cType : this.types) {
				if (this.leadingType.getWeightedScore() < cType
						.getWeightedScore()) {
					this.leadingType = cType;
				}
			}
		}
	}

	@Override
	public TableColumn clone() throws CloneNotSupportedException {
		TableColumn clone = null;
		try {
			clone = (TableColumn) super.clone();
			final List<TableCell> newL = new LinkedList<TableCell>();
			for (final TableCell cell : this.cellList) {
				final TableCell tcell = new TableCell(new String(
						cell.getCellContent()));
				tcell.setDisambiguatedContent(new String(cell
						.getDisambiguatedContent()));
				tcell.setDisambigutedContentString(new String(cell
						.getDisambigutedContentString()));
				tcell.setGt(new String(cell.getGt()));
				newL.add(tcell);
			}
			clone.setLeadingType(this.leadingType.clone());
			for (final Type cType : this.types) {
				clone.addPossibleType(cType.clone());
			}
		} catch (final CloneNotSupportedException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return clone;
	}

	public List<TableCell> getCellList() {
		return this.cellList;
	}

	public int getColumnNr() {
		return this.columnNr;
	}

	public List<Type> getColumnTypes() {
		final List<Type> res = new LinkedList<Type>();
		for (final Type type : this.types) {
			res.add(type);
		}
		return res;
	}

	public String getHeader() {
		return this.header;
	}

	public double getScoreOfLeadingType() {
		return this.leadingType.getWeightedScore();
	}

	public String getURIOfLeadingType() {
		return this.leadingType.getUri();
	}

	public void resetTypes() {
		this.types.clear();
		this.leadingType = null;
	}

	public void setCellList(final List<TableCell> cellList) {
		this.cellList = cellList;
	}

	private void setLeadingType(final Type type) {
		this.leadingType = type;
	}

	public void setNewLeadingType(final Type type) {
		for (final Type cType : this.types) {
			if (cType.equals(cType)) {
				this.leadingType = cType;
			}
		}
	}

}
