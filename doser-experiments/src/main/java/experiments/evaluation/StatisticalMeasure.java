package experiments.evaluation;

public abstract class StatisticalMeasure {

	protected int qrelsIeration;

	protected int resultIteration;

	protected String[] classname;

	public StatisticalMeasure() {
		this.qrelsIeration = 1;
		this.resultIteration = 1;
	}

	public String[] getNames() {
		return classname;
	}

	public abstract void workQuery(CorrectEntry ce, ResultEntry re);

	public abstract void finishQuery(int qryN);

	public abstract double[] getResult();

	public abstract double[] getQueryResult();
}
