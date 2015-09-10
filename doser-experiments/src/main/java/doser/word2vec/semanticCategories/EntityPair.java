package doser.word2vec.semanticCategories;

public class EntityPair {

	private String entity1;
	private String entity2;
	
	private String category1;
	private String category2;
	
	private double sim;
	
	public EntityPair(String entity1, String entity2, String cat1, String cat2) {
		super();
		this.entity1 = entity1;
		this.entity2 = entity2;
		this.category1 = cat1;
		this.category2 = cat2;
	}

	void setEntity1(String entity1) {
		this.entity1 = entity1;
	}

	void setEntity2(String entity2) {
		this.entity2 = entity2;
	}

	String getEntity1() {
		return entity1;
	}

	String getEntity2() {
		return entity2;
	}

	String getCategory1() {
		return category1;
	}

	void setCategory1(String category1) {
		this.category1 = category1;
	}

	String getCategory2() {
		return category2;
	}

	void setCategory2(String category2) {
		this.category2 = category2;
	}

	double getSim() {
		return sim;
	}

	void setSim(double sim) {
		this.sim = sim;
	}
}
