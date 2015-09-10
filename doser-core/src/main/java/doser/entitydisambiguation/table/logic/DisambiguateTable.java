package doser.entitydisambiguation.table.logic;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import doser.entitydisambiguation.table.celldisambiguation.CellDisAlgorithm_Standard;
import doser.entitydisambiguation.table.celldisambiguation.CellDisambiguationInterface;
import doser.entitydisambiguation.table.columndisambiguation.ColumnDisAlgorithm;

/**
 * This class organizes the table disambiguation algorithms.
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class DisambiguateTable {

	private static final float NUMBERSTHRESHOLD = 0.5f;

	private List<String> groundtruth;

	public DisambiguateTable() {
		super();
	}

	public Table disambiguateTable(final Table table) {
		final int nrOfCols = table.getNumberofColumns();
		for (int i = 0; i < nrOfCols; i++) {
			final TableColumn col = table.getColumn(i);
			final CellDisambiguationInterface cellDisAlgo = decideCellDisambiguationAlgorithm(col);
			cellDisAlgo.disambiguateCells(col);
		}
		final ColumnDisAlgorithm colDisAlgo = new ColumnDisAlgorithm();
		if (this.groundtruth == null) {
			colDisAlgo.disambiguateTypes(table, null);
		} else {
			colDisAlgo.disambiguateTypes(table, this.groundtruth);
		}
		return table;
	}

	/**
	 * Bad heuristic to detect the neccessary algorithm . If more than 50
	 * percent numbers are available we use the computer science cell
	 * disambiguation algorithm.
	 * 
	 * @param col
	 *            The respective table column
	 * @return the cell disambiguation algorithm
	 */
	private CellDisambiguationInterface decideCellDisambiguationAlgorithm(
			final TableColumn col) {
		final List<TableCell> cellList = col.getCellList();
		int amountOfNrs = 0;
		for (final TableCell cell : cellList) {
			final String content = cell.getCellContent();
			if(isNumber(content)) {
				amountOfNrs++;
			}
		}
		CellDisambiguationInterface res = null;
		if(((float)amountOfNrs / (float) cellList.size()) > NUMBERSTHRESHOLD) {
			// ToDo Wieder umschreiben. Allerdings wird das momentan für den Datenextraktor zurückgegesetz 
//			res = CellDisAlgorithm_CSDomain.getInstance();
			res = CellDisAlgorithm_Standard.getInstance();
		} else {
			res = CellDisAlgorithm_Standard.getInstance();
		}
		return res;
	}

	private boolean isNumber(final String str) {
		boolean numberFound = false;
		final Pattern pattern = Pattern.compile("^\\d*[.,]?\\d*$");
		final Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			numberFound = true;
		}
		return numberFound;
	}

	public void setGroundtruth(final List<String> columnGroundTruth) {
		this.groundtruth = columnGroundTruth;
	}

}
