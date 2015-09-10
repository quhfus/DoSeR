package doser.entitydisambiguation.modknowledgebase;

import java.util.Observable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

public abstract class AbstractKnowledgebaseOperator extends Observable {

	protected Analyzer analzyer;

	protected String kbPath;

	public AbstractKnowledgebaseOperator(final String path,
			final Analyzer analyzer) {
		super();
		this.kbPath = path;
		this.analzyer = analyzer;
	}

	public Analyzer getAnalyzer() {
		return this.analzyer;
	}

	public String getKnowledgeBasePath() {
		return this.kbPath;
	}

	public abstract void modifyIndex(IndexWriter writer, IndexSearcher searcher)
			throws ModifyKnowledgeBaseException;
}
