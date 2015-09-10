package experiments.collective.entdoccentric;

public class LearntoRankOutputObject {

	private int qId;
	
	private double[] featureValues;
	
	private boolean relevant;
	
	private String misc;
	
	public LearntoRankOutputObject(boolean relevant, int qId, double[] featureValues) {
		this.qId = qId;
		this.relevant = relevant;
		this.featureValues = featureValues;
	}

	public int getqId() {
		return qId;
	}

	public double[] getFeatureValues() {
		return featureValues;
	}

	public boolean isRelevant() {
		return relevant;
	}

	public String getMisc() {
		return misc;
	}

	public void setMisc(String misc) {
		this.misc = misc;
	}
	
	
}
