package doser.entitydisambiguation.table.logic;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Responsible class for representing table data in algorithm
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class Table {

	private final List<TableColumn> columnList;

	private String name;

	private int nrcols;

	public Table() {
		super();
		this.columnList = new LinkedList<TableColumn>();
		this.nrcols = 0;
	}

	public TableColumn addColumn(final String columnHeader) {
		final TableColumn col = new TableColumn(columnHeader, this.nrcols++);
		this.columnList.add(col);
		return col;
	}

	public TableColumn getColumn(final int columnNr) {
		TableColumn res = null;
		try {
			res = this.columnList.get(columnNr);
		} catch (final IndexOutOfBoundsException e) {
			Logger.getRootLogger().warn(e.getStackTrace());
		}
		return res;
	}

	public String getName() {
		return this.name;
	}

	public int getNumberofColumns() {
		return this.columnList.size();
	}

	public void setName(final String name) {
		this.name = name;
	}
}
