package doser.word2vec.dbpediaGraphThinning;

import doser.word2vec.Word2VecModel;

public class TestNegativeCosine {
	public static void main(String[] args) {
		Word2VecModel model = Word2VecModel.createWord2VecModel("/mnt/ssd1/disambiguation/word2vec/wikientitymodel_min5.seq");
		model.evaluateNegativeValues("Rome");
	}
	
}
