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
	private List<Position> position;
	private String selectedText;

	public Response() {
		super();
		this.disEntities = new LinkedList<DisambiguatedEntity>();
		this.position = new LinkedList<Position>();
	}

	public List<DisambiguatedEntity> getDisEntities() {
		return this.disEntities;
	}

	public List<Position> getPosition() {
		return this.position;
	}

	public String getSelectedText() {
		return this.selectedText;
	}

	public void setDisEntities(final List<DisambiguatedEntity> disEntities) {
		this.disEntities = disEntities;
	}

	public void setPosition(final List<Position> position) {
		this.position = position;
	}

	public void setSelectedText(final String selectedText) {
		this.selectedText = selectedText;
	}
}
