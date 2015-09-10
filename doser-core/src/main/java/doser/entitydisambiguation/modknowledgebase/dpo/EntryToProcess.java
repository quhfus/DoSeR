package doser.entitydisambiguation.modknowledgebase.dpo;

public class EntryToProcess {

	private String fieldName;

	private String value;

	public String getFieldName() {
		return this.fieldName;
	}

	public String getValue() {
		return this.value;
	}

	public void setFieldName(final String fieldName) {
		this.fieldName = fieldName;
	}

	public void setValue(final String value) {
		this.value = value;
	}
}
