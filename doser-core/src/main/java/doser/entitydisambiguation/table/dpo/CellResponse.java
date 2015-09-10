package doser.entitydisambiguation.table.dpo;

public class CellResponse {

	private String text;

	private String uri;

	public String getText() {
		return this.text;
	}

	public String getUri() {
		return this.uri;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public void setUri(final String uri) {
		this.uri = uri;
	}
}
