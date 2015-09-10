package doser.entitydisambiguation.table.logic;

import java.util.LinkedList;
import java.util.List;

import doser.entitydisambiguation.table.dpo.ColumnResponseItem;

public final class TableDisambiguationMainService {

	private static TableDisambiguationMainService instance = null;

	public static synchronized TableDisambiguationMainService getInstance() {
		if (instance == null) {
			instance = new TableDisambiguationMainService();
		}
		return instance;
	}

	private final DisambiguateTable disTable;

	private TableDisambiguationMainService() {
		this.disTable = new DisambiguateTable();
	}

	private Table convertToAlgorithmFormat(
			final List<TableDisambiguationTask> taskList) {
		final Table table = new Table();
		table.setName("Intern unnamed");
		for (int i = 0; i < taskList.size(); i++) {
			final List<doser.entitydisambiguation.table.dpo.TableCell> cells = taskList
					.get(i).getCellList();
			String header = taskList.get(i).getHeader();
			if ((header == null) || header.equalsIgnoreCase("")
					|| header.equalsIgnoreCase(" ")) {
				header = "";
			}
			final TableColumn col = table.addColumn(header);
			// final String gtString = taskList.get(i).getTypeGroundtruth();
			// if ((gtString != null) && !gtString.equalsIgnoreCase("")) {
			// final String gtSplitter[] = gtString.split(" ");
			// for (final String element : gtSplitter) {
			// col.addTypeGt(element);
			// }
			// }
			for (int j = 0; j < cells.size(); j++) {
				col.addCell(cells.get(j).getCellContent());
			}
		}
		return table;
	}

	private List<ColumnResponseItem> convertToResponseFormat(final Table table) {
		final List<ColumnResponseItem> res = new LinkedList<ColumnResponseItem>();
		final int amountCols = table.getNumberofColumns();
		for (int i = 0; i < amountCols; i++) {
			final ColumnResponseItem item = new ColumnResponseItem();
			item.setName(table.getColumn(i).getHeader());
			final List<TableCell> cellList = table.getColumn(i).getCellList();
			for (int j = 0; j < cellList.size(); j++) {
				item.setNewCellResponse(cellList.get(j)
						.getDisambigutedContentString(), cellList.get(j)
						.getDisambiguatedContent());
			}
			final List<String> typeList = new LinkedList<String>();
			final List<Type> types = table.getColumn(i).getColumnTypes();
			for (final Type type : types) {
				typeList.add(type.getUri());
			}
			item.setColumnTypes(typeList);
			res.add(item);
		}
		return res;
	}

	public List<ColumnResponseItem> disambiguate(
			final List<TableDisambiguationTask> taskList) {
		Table table = this.convertToAlgorithmFormat(taskList);
		final List<String> gtList = this.extractGroundtruth(taskList);
		this.disTable.setGroundtruth(gtList);
		table = this.disTable.disambiguateTable(table);
		return this.convertToResponseFormat(table);
	}

	private List<String> extractGroundtruth(
			final List<TableDisambiguationTask> taskList) {
		final List<String> gtList = new LinkedList<String>();
		for (final TableDisambiguationTask tableTask : taskList) {
			if ((tableTask.getTypeGroundtruth() == null)
					|| tableTask.getTypeGroundtruth().equalsIgnoreCase("")
					|| tableTask.getTypeGroundtruth().equalsIgnoreCase(" ")) {
				gtList.add("");
			} else {
				gtList.add(tableTask.getTypeGroundtruth());
			}
		}
		return gtList;
	}
}
