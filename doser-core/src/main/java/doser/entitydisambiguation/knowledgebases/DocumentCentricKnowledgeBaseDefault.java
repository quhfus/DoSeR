package doser.entitydisambiguation.knowledgebases;

import org.apache.lucene.search.similarities.Similarity;

public class DocumentCentricKnowledgeBaseDefault extends AbstractKnowledgeBase  {

	public DocumentCentricKnowledgeBaseDefault(String uri, boolean dynamic,
			Similarity sim) {
		super(uri, dynamic, sim);
	}

	public DocumentCentricKnowledgeBaseDefault(String uri, boolean dynamic) {
		super(uri, dynamic);
	}

	@Override
	public void initialize() {
	}
}
