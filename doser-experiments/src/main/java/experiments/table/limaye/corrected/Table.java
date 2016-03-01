package experiments.table.limaye.corrected;

import java.util.LinkedList;
import java.util.List;

import DisambiguationApproachDPO.DisambiguatedEntity;


/**
 * Responsible class for representing table data in algorithm
 * 
 * @author Stefan Zwicklbauer
 *
 */
public class Table {

	private List<Column> columnList;

	private String name;
	
	public Table() {
		super();
		this.columnList = new LinkedList<Table.Column>();
	}

	public Column addColumn(String columnHeader) {
		Column c = new Column(columnHeader);
		this.columnList.add(c);
		return c;
	}

	public Column getColumn(int columnNr) {
		try {
			return this.columnList.get(columnNr);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public int getNumberofColumns() {
		return columnList.size();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public class Column {

		private String header;

		private List<Cell> cellList;

		private List<String> majorTypes;
		
		public Column(String header) {
			super();
			this.header = header;
			this.cellList = new LinkedList<Table.Column.Cell>();
		}

		public void addCell(String content) {
			cellList.add(new Cell(content));
		}

		public void addCellGroundTruth(String gt, int cellNr) {
			cellList.get(cellNr).setGt(gt);
		}

		public String getHeader() {
			return this.header;
		}
		
		public void addLastCellGT(String gt) {
			cellList.get(cellList.size() - 1).setGt(gt);
		}

		public List<Cell> getCellList() {
			return this.cellList;
		}

		public List<String> getMajorTypes() {
			return majorTypes;
		}

		public void setMajorTypes(List<String> majorTypes) {
			this.majorTypes = majorTypes;
		}

		public void setCellList(List<Cell> c ) {
			this.cellList = c;
		}

		public class Cell implements Comparable<Cell> {

			private String cellContent;

			private String gt;

			private String disambiguatedContent;
			
			private String disambigutedContentString;
			
			private List<DisambiguatedEntity> disEntities;

			public Cell(String content) {
				super();
				this.cellContent = content;
				this.disambiguatedContent = "";
				this.gt = "";
			}

			public String getCellContent() {
				return cellContent;
			}

			public String getGt() {
				return gt;
			}

			public void setGt(String gt) {
				this.gt = gt;
			}

			public String getDisambiguatedContent() {
				return disambiguatedContent;
			}

			public void setDisambiguatedContent(String c) {
				this.disambiguatedContent = c;
			}
			
			public void setDisambigutedEntities(List<DisambiguatedEntity> e) {
				this.disEntities = e;
			}
			
			public List<DisambiguatedEntity> getDisambiguatedEntities() {
				return this.disEntities;
			}

			public String getDisambigutedContentString() {
				return disambigutedContentString;
			}

			public void setDisambigutedContentString(String disambigutedContentString) {
				this.disambigutedContentString = disambigutedContentString;
			}

			@Override
			public int compareTo(Cell o) {
				if (o.getCellContent().equals(this.cellContent)
						&& o.getGt().equals(this.gt)
						&& o.getDisambiguatedContent().equals(
								this.disambiguatedContent)) {
					return 0;
				} else {
					return 1;
				}
			}
		}
	}
}
