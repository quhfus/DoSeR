package doser.lucene.query;

/**
 * Not in use so far.
 * 
 * @author quh
 */
public class LearnToRankFeatureDefaultValueManager {

	private static LearnToRankFeatureDefaultValueManager man;

	public static LearnToRankFeatureDefaultValueManager getInstance() {
		return man;
	}

	public static void setInstance(
			final LearnToRankFeatureDefaultValueManager manager) {
		man = manager;
	}

	private int amountQueries;

	private final float[] maxVals;

	private final float[] sums;

	public LearnToRankFeatureDefaultValueManager(final int pos) {
		maxVals = new float[pos];
		sums = new float[pos];
		for (int j = 0; j < sums.length; j++) {
			sums[j] = 0;
		}
		amountQueries = 0;
	}

	public float[] getAverageResults() {
		final float[] results = new float[maxVals.length];
		for (int i = 0; i < sums.length; i++) {
			results[i] = sums[i] / amountQueries;
		}
		return results;
	}

	public void newQuery() {
		for (int i = 0; i < maxVals.length; i++) {
			sums[i] += maxVals[i];
		}
		amountQueries++;
	}

	public synchronized void setValue(final int position, final float value) {
		if (maxVals[position] < value) {
			maxVals[position] = value;
		}
	}
}
