package doser.entitydisambiguation.dpo;

import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a disambiguated surface form and contains all necessary
 * information about the disambiguation. Position is required because a
 * ColumnResponseItem has no unique primary key and assures the correct
 * assignment to the original item.
 * 
 * Version 2.0 offers a list of positions
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class Response {

	private List<DisambiguatedEntity> disEntities;
	private int startPosition;
	private String selectedText;

	public Response() {
		super();
		this.disEntities = new LinkedList<DisambiguatedEntity>();
	}

	public List<DisambiguatedEntity> getDisEntities() {
		return this.disEntities;
	}

	public String getSelectedText() {
		return this.selectedText;
	}

	public void setDisEntities(final List<DisambiguatedEntity> disEntities) {
		this.disEntities = disEntities;
	}

	public void setSelectedText(final String selectedText) {
		this.selectedText = selectedText;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}
}
