package doser.entitydisambiguation.table.columndisambiguation;

public class LearntoRankOutputObject {

	private final double[] featureValues;

	private final int qId;

	private final boolean relevant;

	public LearntoRankOutputObject(final boolean relevant, final int qId,
			final double[] featureValues) {
		this.qId = qId;
		this.relevant = relevant;
		this.featureValues = featureValues;
	}

	public double[] getFeatureValues() {
		return this.featureValues;
	}

	public int getqId() {
		return this.qId;
	}

	public boolean isRelevant() {
		return this.relevant;
	}
}
