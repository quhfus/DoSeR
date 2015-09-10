package experiments.collective.entdoccentric.calbc;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Entity {

	private String keyword;

	private List<Concept> conceptList;
	
	private boolean isTitle;

	private int position;
	
	public Entity() {
		conceptList = new LinkedList<Concept>();
		position = 0;
	}
	
	public Entity(String keyword, boolean isTitle, int position) {
		this.keyword = keyword;
		this.isTitle = isTitle;
		this.position = position;
		conceptList = new LinkedList<Concept>();
	}
	
	public void addConcept(Concept concept) {
		conceptList.add(concept);
	}
	
	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<Concept> getConceptList() {
		return conceptList;
	}

	public void setConceptList(List<Concept> conceptList) {
		this.conceptList = conceptList;
	}

	public boolean isTitle() {
		return isTitle;
	}

	public void setTitle(boolean isTitle) {
		this.isTitle = isTitle;
	}
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	///////////////// Experiment Methode //////////////////////////////
	public boolean hasNCBIConcepts () {
		boolean hasNCBI = false;
		for (Iterator<Concept> iterator = conceptList.iterator(); iterator.hasNext();) {
			Concept con = iterator.next();
			if(con.getUrl().contains("ncbi")) {
				hasNCBI = true;
				break;
			}
		}
		return hasNCBI;
	}
}
