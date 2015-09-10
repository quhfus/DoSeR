package experiments.collective.entdoccentric.calbc;

import java.util.LinkedList;
import java.util.List;

public class CalbCPubMedID {

	private String title;

	private String abs;

	private Metadata metadata;

	private List<Entity> entityList;

	private String id;

	public CalbCPubMedID() {
		abs = "";
		entityList = new LinkedList<Entity>();
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public String getId() {
		return id;
	}

	public String getAbs() {
		return abs;
	}

	public void setAbs(String abs) {
		this.abs = abs;
	}

	public List<Entity> getEntityList() {
		return entityList;
	}

	public void addConcept(Entity entity) {
		entityList.add(entity);
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public void setEntityList(List<Entity> entityList) {
		this.entityList = entityList;
	}
	
	public void concatAbstract(String newabs) {
		abs += newabs;
	}
	
}
