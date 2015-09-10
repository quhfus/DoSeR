package experiments.evaluation;

public class M_Accuracy2 extends StatisticalMeasure {

	private boolean isIn;
	
	private double queryVal;
	
	private double qryAmount;
	
	public M_Accuracy2() {
		super();
		this.isIn = false;
		this.queryVal = 0;
		this.qryAmount = 0;
		classname = new String[1];
		classname[0] = "Accuracy2";
	}
	
	@Override
	public void workQuery(CorrectEntry ce, ResultEntry re) {
		if (ce.getDocName().equalsIgnoreCase(re.getDocName())) {
			isIn = true;
		}
	}

	@Override
	public void finishQuery(int qryN) {
		if(isIn) {
			queryVal++;
		}
		isIn = false;
		qryAmount++;
	}

	@Override
	public double[] getResult() {
		double[] result = new double[1];
		result[0] = queryVal / qryAmount;
		System.out.println(queryVal);
		System.out.println(qryAmount);
		return result;
	}

	@Override
	public double[] getQueryResult() {
		double[] result = new double[1];
		result[0] = queryVal;
		return result;
	}
}
