package experiments.collective.entdoccentric;

public class TrecEvalResultObject {

	private String[] values;
	
	private String[][] optimalValues;
	
	public TrecEvalResultObject() {
		values = new String[0];
		optimalValues = new String[0][0];
	}

	public void setResult(String[] values) {
		this.values = values;
	}

	public String[] getResult() {
		return values;
	}


	public void setOptimalResult(String[][] values) {
		this.optimalValues = values;		
	}


	public String[][] getOptimalResult() {
		return optimalValues;
	}
}
