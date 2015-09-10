package experiments.evaluation;

public class M_ReciprocalRank extends StatisticalMeasure {

	private double overallRank;

	private long callups;

	private int firstCorrectAnswer;

	private double queryValue;

	public M_ReciprocalRank() {
		super();
		overallRank = 0;
		callups = 0;
		queryValue = 0;
		firstCorrectAnswer = Integer.MAX_VALUE;
		classname = new String[1];
		classname[0] = "ReciprocalRank";
	}

	@Override
	public void workQuery(CorrectEntry ce, ResultEntry re) {
		if (ce.getDocName().equalsIgnoreCase(re.getDocName())
				&& firstCorrectAnswer > resultIteration) {
			firstCorrectAnswer = resultIteration;
		}
	}

	@Override
	public void finishQuery(int qryNr) {
		callups++;
		if (firstCorrectAnswer != Integer.MAX_VALUE) {
			queryValue = 1.0 / firstCorrectAnswer;
			overallRank += queryValue;
		} else {
			queryValue = 0;
		}
		firstCorrectAnswer = Integer.MAX_VALUE;
	}

	@Override
	public double[] getResult() {
		double result[] = new double[1];
		result[0] = overallRank / callups;
		return result;
	}

	@Override
	public double[] getQueryResult() {
		double result[] = new double[1];
		result[0] = queryValue;
		return result;
	}

}
