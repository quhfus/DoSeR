package experiments.collective.entdoccentric;

public interface ResultProcessing {
	
	public void processResult(TrecEvalResultObject object);
	
	public void close();
}
