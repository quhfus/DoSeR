package doser.webclassify.dpo;

import java.util.List;

public class Document {

	/**
	 * Maybe some more metadata
	 */
	private List<Paragraph> paragraphs;

	public List<Paragraph> getParagraphs() {
		return paragraphs;
	}

	public void setParagraphs(List<Paragraph> paragraphs) {
		this.paragraphs = paragraphs;
	}
}
