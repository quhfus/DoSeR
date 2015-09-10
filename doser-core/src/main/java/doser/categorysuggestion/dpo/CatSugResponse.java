package doser.categorysuggestion.dpo;

import java.util.List;

import doser.entitydisambiguation.table.logic.Type;

public class CatSugResponse {

	private List<Type> categories;

	public List<Type> getCategories() {
		return this.categories;
	}

	public void setCategories(final List<Type> categories) {
		this.categories = categories;
	}
}
