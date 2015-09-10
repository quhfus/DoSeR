package lda.wikievidence.modelcreation;

public class ConfigCreation {

	public static byte[] createStandardPLDAConfig(String datafilePath,
			String modelOutputPath) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("import scalanlp.io._;");
		buffer.append("import scalanlp.stage._;");
		buffer.append("import scalanlp.stage.text._;");
		buffer.append("import scalanlp.text.tokenize._;");
		buffer.append("import scalanlp.pipes.Pipes.global._;");
		buffer.append("import edu.stanford.nlp.tmt.stage._;");
		buffer.append("import edu.stanford.nlp.tmt.model.lda._;");
		buffer.append("import edu.stanford.nlp.tmt.model.llda._;");
		buffer.append("import edu.stanford.nlp.tmt.model.plda._;");

		buffer.append("val source = CSVFile(\"" + datafilePath
				+ "\") ~> IDColumn(1);");

		buffer.append("val tokenizer = {");
		buffer.append("SimpleEnglishTokenizer() ~>");
		buffer.append("CaseFolder() ~>");
		buffer.append("WordsAndNumbersOnlyFilter() ~>");
		buffer.append("MinimumLengthFilter(3)");
		buffer.append("}");

		buffer.append("val text = {");
		buffer.append("source ~>");
		buffer.append("Column(4) ~>");
		buffer.append("TokenizeWith(tokenizer) ~>");
		buffer.append("TermCounter() ~>");
		buffer.append("TermMinimumDocumentCountFilter(4) ~>");
		buffer.append("TermDynamicStopListFilter(30) ~>");
		buffer.append("DocumentMinimumLengthFilter(5)");
		buffer.append("}");

		buffer.append("val labels = {");
		buffer.append("source ~>");
		buffer.append("Column(2) ~>");
		buffer.append("TokenizeWith(WhitespaceTokenizer()) ~>");
		buffer.append("TermCounter() ~>");
		buffer.append("TermMinimumDocumentCountFilter(10)");
		buffer.append("}");

		buffer.append("val dataset = LabeledLDADataset(text, labels);");
		buffer.append("val numBackgroundTopics = 1;");
		buffer.append("val numTopicsPerLabel = SharedKTopicsPerLabel(1);");

		buffer.append("val modelParams = PLDAModelParams(dataset, numBackgroundTopics, numTopicsPerLabel, termSmoothing = 0.01, topicSmoothing = 0.01);");
		buffer.append("val modelPath = file(\"" + modelOutputPath + "\");");
		buffer.append("TrainCVB0PLDA(modelParams, dataset, output = modelPath, maxIterations = 50);");
		return buffer.toString().getBytes();
	}

	public static byte[] createEvidenceExtractionConfig(String datafilePath,
			String modelOutputPath, int nrTopTerms) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("import scalanlp.io._;");
		buffer.append(System.lineSeparator());
		buffer.append("import scalanlp.stage._;");
		buffer.append(System.lineSeparator());
		buffer.append("import scalanlp.stage.text._;");
		buffer.append(System.lineSeparator());
		buffer.append("import scalanlp.text.tokenize._;");
		buffer.append(System.lineSeparator());
		buffer.append("import scalanlp.pipes.Pipes.global._;");
		buffer.append(System.lineSeparator());
		buffer.append("import edu.stanford.nlp.tmt.stage._;");
		buffer.append(System.lineSeparator());
		buffer.append("import edu.stanford.nlp.tmt.model.lda._;");
		buffer.append(System.lineSeparator());
		buffer.append("import edu.stanford.nlp.tmt.model.llda._;");
		buffer.append(System.lineSeparator());
		buffer.append("import edu.stanford.nlp.tmt.model.plda._;");
		buffer.append(System.lineSeparator());

		buffer.append("val source = CSVFile(\"" + datafilePath
				+ "\") ~> IDColumn(1);");
		buffer.append(System.lineSeparator());

		buffer.append("val tokenizer = {");
		buffer.append(System.lineSeparator());
		buffer.append("SimpleEnglishTokenizer() ~>");
		buffer.append(System.lineSeparator());
		buffer.append("CaseFolder() ~>");
		buffer.append(System.lineSeparator());
		buffer.append("WordsAndNumbersOnlyFilter() ~>");
		buffer.append(System.lineSeparator());
		buffer.append("MinimumLengthFilter(3)");
		buffer.append(System.lineSeparator());
		buffer.append("}");
		buffer.append(System.lineSeparator());

		buffer.append("val text = {");
		buffer.append(System.lineSeparator());
		buffer.append("source ~>");
		buffer.append(System.lineSeparator());
		buffer.append("Column(3) ~>");
		buffer.append(System.lineSeparator());
		buffer.append("TokenizeWith(tokenizer) ~>");
		buffer.append(System.lineSeparator());
		buffer.append("TermCounter() ~>");
		buffer.append(System.lineSeparator());
		buffer.append("TermMinimumDocumentCountFilter(5) ~>");
		buffer.append(System.lineSeparator());
		buffer.append("TermStopListFilter(scala.io.Source.fromFile(\"stopwords.txt\").getLines().toList) ~>");
		buffer.append(System.lineSeparator());
		buffer.append("TermDynamicStopListFilter(50) ~>");
		buffer.append(System.lineSeparator());
		buffer.append("DocumentMinimumLengthFilter(5)");
		buffer.append(System.lineSeparator());
		buffer.append("}");
		buffer.append(System.lineSeparator());

		buffer.append("val labels = {");
		buffer.append(System.lineSeparator());
		buffer.append("source ~>");
		buffer.append(System.lineSeparator());
		buffer.append("Column(2) ~>");
		buffer.append(System.lineSeparator());
		buffer.append("TokenizeWith(WhitespaceTokenizer()) ~>");
		buffer.append(System.lineSeparator());
		buffer.append("TermCounter() ~>");
		buffer.append(System.lineSeparator());
		buffer.append("TermMinimumDocumentCountFilter(1)");
		buffer.append(System.lineSeparator());
		buffer.append("}");
		buffer.append(System.lineSeparator());

		buffer.append("val dataset = LabeledLDADataset(text, labels);");
		buffer.append(System.lineSeparator());
		buffer.append("val numBackgroundTopics = 2;");
		buffer.append(System.lineSeparator());
		buffer.append("val numTopicsPerLabel = SharedKTopicsPerLabel(1);");
		buffer.append(System.lineSeparator());

		buffer.append("val modelParams = PLDAModelParams(dataset, numBackgroundTopics, numTopicsPerLabel, termSmoothing = 0.01, topicSmoothing = 0.01);");
		buffer.append(System.lineSeparator());
		buffer.append("val modelPath = file(\"" + modelOutputPath + "\");");
		buffer.append(System.lineSeparator());
		buffer.append("TrainCVB0PLDA(modelParams, dataset, output = modelPath, maxIterations = 25);");
		return buffer.toString().getBytes();
	}
}
