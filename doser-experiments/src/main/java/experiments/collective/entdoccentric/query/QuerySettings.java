package experiments.collective.entdoccentric.query;

public class QuerySettings {
	
	private boolean surfaceFormFuzzy;
	
	private boolean descriptionFuzzy;
	
	private boolean useDescription;
	
	private boolean useTFIDF;
	
	private boolean learnToRank;
	
	private boolean documentcentric;
	
	private String query;
	
	public QuerySettings() {
		query = "std";
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isSurfaceFormFuzzy() {
		return surfaceFormFuzzy;
	}

	public void setSurfaceFormFuzzy(boolean surfaceFormFuzzy) {
		this.surfaceFormFuzzy = surfaceFormFuzzy;
	}

	public boolean isDescriptionFuzzy() {
		return descriptionFuzzy;
	}

	public void setDescriptionFuzzy(boolean descriptionFuzzy) {
		this.descriptionFuzzy = descriptionFuzzy;
	}

	public boolean isUseDescription() {
		return useDescription;
	}

	public void setUseDescription(boolean useDescription) {
		this.useDescription = useDescription;
	}

	public boolean isUseTFIDF() {
		return useTFIDF;
	}

	public void setUseTFIDF(boolean useTFIDF) {
		this.useTFIDF = useTFIDF;
	}

	public boolean isLearnToRank() {
		return learnToRank;
	}

	public void setLearnToRank(boolean learnToRank) {
		this.learnToRank = learnToRank;
	}

	public boolean isDocumentcentric() {
		return documentcentric;
	}

	public void setDocumentcentric(boolean documentcentric) {
		this.documentcentric = documentcentric;
	}
}