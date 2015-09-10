package experiments.collective.entdoccentric.dpo;

import java.util.List;

/**
 * Represents surfaceform which should be disambiguated. Positions is used as an
 * intern id, which is necessary during feedback processing later.
 * 
 * Version 2.0 One position of a surface form might be not enough. Version 2
 * offers the possibility to send an array of position.
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class EntityToDisambiguate {

	private String selectedText;

	private String context;
	private List<Position> position;

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public List<Position> getPosition() {
		return position;
	}

	public void setPosition(List<Position> position) {
		this.position = position;
	}
	
	public String getSelectedText() {
		return selectedText;
	}

	public void setSelectedText(String selectedText) {
		this.selectedText = selectedText;
	}
}
