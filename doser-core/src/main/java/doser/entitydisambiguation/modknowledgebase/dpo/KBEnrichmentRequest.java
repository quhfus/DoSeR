package doser.entitydisambiguation.modknowledgebase.dpo;

import java.util.List;

public class KBEnrichmentRequest {

	private String command;

	private List<DocumentToProcess> docList;

	private String fieldAction;

	private String kburi;

	private String primaryKeyField;

	public String getCommand() {
		String res = this.command;
		if (command == null) {
			res = "";
		}
		return res;
	}

	public List<DocumentToProcess> getDocList() {
		return this.docList;
	}

	public String getFieldAction() {
		return this.fieldAction;
	}

	public String getKburi() {
		return this.kburi;
	}

	public String getPrimaryKeyField() {
		return this.primaryKeyField;
	}

	public void setCommand(final String command) {
		this.command = command;
	}

	public void setDocList(final List<DocumentToProcess> docList) {
		this.docList = docList;
	}

	public void setFieldAction(final String fieldAction) {
		this.fieldAction = fieldAction;
	}

	public void setKburi(final String kburi) {
		this.kburi = kburi;
	}

	public void setPrimaryKeyField(final String primaryKeyField) {
		this.primaryKeyField = primaryKeyField;
	}
}
