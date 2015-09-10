package doser.entitydisambiguation.dpo;

import java.util.List;

/**
 * Represents surfaceform which should be disambiguated. Positions is used as an
 * intern id, which is necessary during feedback processing later.
 * 
 * Version 2.0 One position of a surface form might be not enough. Version 2
 * offers the possibility to send an array of position.
 * 
 * Version 3.0 A new field InterDisambiguationSetting flags the kind of
 * Disambiguation. This can be one of the following Types: - Standard Entity
 * Disambiguation with context - Standard Entity Disambiguation without context
 * - Entity Disambiguation without context on specialized domain (i.e. tables)
 * 
 * Version 4.0 KnowledgeBaseIdentifier allows to select a specific knowledge
 * base for each disambiguation algorithm. This option should only be used if
 * the user is aware of what he is doing. Additionally the user is able to get
 * the lucene documents of disambiguated entities.
 * 
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class EntityDisambiguationDPO {

	private String documentId;
	private List<Fact> factList;
	private String context;
	private List<Position> position;
	private String selectedText;
	private String setting;
	private String kbversion;

	public EntityDisambiguationDPO() {
		super();
	}

	public String getContext() {
		return this.context;
	}

	public List<Position> getPosition() {
		return this.position;
	}

	public String getSelectedText() {
		return this.selectedText;
	}

	public void setContext(final String context) {
		this.context = context;
	}

	public void setPosition(final List<Position> position) {
		this.position = position;
	}

	public void setSelectedText(final String selectedText) {
		this.selectedText = selectedText;
	}

	public void setSetting(final String setting) {
		this.setting = setting;
	}

	public String getSetting() {
		return setting;
	}
	
	public void setDocumentId(final String documentId) {
		this.documentId = documentId;
	}

	public void setFactList(final List<Fact> factList) {
		this.factList = factList;
	}
	
	public String getDocumentId() {
		return this.documentId;
	}

	public List<Fact> getFactList() {
		return this.factList;
	}

	public void setInternSetting(final String setting) {
		this.setting = setting;
	}
	
	public String getKbversion() {
		return kbversion;
	}

	public void setKbversion(String kbversion) {
		this.kbversion = kbversion;
	}
}