package doser.entitydisambiguation.table.logic;

import java.util.LinkedList;
import java.util.List;

import doser.entitydisambiguation.dpo.DisambiguatedEntity;

public class TableCell implements Comparable<TableCell> {

	private List<DisambiguatedEntity> candidateList;

	private String cellContent;

	private String disContent;

	private String disContentString;

	private String groundtruth;

	TableCell(final String content) {
		super();
		this.cellContent = content;
		if (content == null) {
			this.cellContent = "";
		}
		this.disContent = "";
		this.groundtruth = "";
		this.candidateList = new LinkedList<DisambiguatedEntity>();
	}

	@Override
	public int compareTo(final TableCell tablecell) {
		int res = 1;
		if (tablecell.getCellContent().equals(this.cellContent)
				&& tablecell.getGt().equals(this.groundtruth)
				&& tablecell.getDisambiguatedContent().equals(
						this.disContent)) {
			res = 0;
		}
		return res;
	}

	public String getCellContent() {
		return this.cellContent;
	}

	public String getDisambiguatedContent() {
		return this.disContent;
	}

	public List<DisambiguatedEntity> getDisambiguatedEntities() {
		return this.candidateList;
	}

	public String getDisambigutedContentString() {
		return this.disContentString;
	}

	public String getGt() {
		return this.groundtruth;
	}

	public void setDisambiguatedContent(final String content) {
		this.disContent = content;
	}

	public void setDisambiguatedEntities(final List<DisambiguatedEntity> ent) {
		this.candidateList = ent;
	}

	public void setDisambigutedContentString(final String disContentString) {
		this.disContentString = disContentString;
	}

	public void setGt(final String groundtruth) {
		this.groundtruth = groundtruth;
	}

}
