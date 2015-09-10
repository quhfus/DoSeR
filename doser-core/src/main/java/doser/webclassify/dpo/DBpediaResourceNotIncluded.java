package doser.webclassify.dpo;

public class DBpediaResourceNotIncluded {

	private String URI;
	private Integer support;
	private String types;
	private String surfaceForm;
	private String offset;
	private String similarityScore;
	private String percentageOfSecondRank;
	
	public DBpediaResourceNotIncluded() {
		super();
	}
	
	public String getURI() {
		return URI;
	}
	public void setURI(String uRI) {
		URI = uRI;
	}
	public Integer getSupport() {
		return support;
	}
	public void setSupport(Integer support) {
		this.support = support;
	}
	public String getTypes() {
		return types;
	}
	public void setTypes(String types) {
		this.types = types;
	}
	public String getSurfaceForm() {
		return surfaceForm;
	}
	public void setSurfaceForm(String surfaceForm) {
		this.surfaceForm = surfaceForm;
	}
	public String getOffset() {
		return offset;
	}
	public void setOffset(String offset) {
		this.offset = offset;
	}
	public String getSimilarityScore() {
		return similarityScore;
	}
	public void setSimilarityScore(String similarityScore) {
		this.similarityScore = similarityScore;
	}
	public String getPercentageOfSecondRank() {
		return percentageOfSecondRank;
	}
	public void setPercentageOfSecondRank(String percentageOfSecondRank) {
		this.percentageOfSecondRank = percentageOfSecondRank;
	}
}
