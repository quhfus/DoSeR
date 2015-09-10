package doser.categorysuggestion.dpo;

public class CatSugRequest {

	private String input;

	private String language;

	public String getInput() {
		return this.input;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setInput(final String input) {
		this.input = input;
	}

	public void setLanguage(final String language) {
		this.language = language;
	}
}
