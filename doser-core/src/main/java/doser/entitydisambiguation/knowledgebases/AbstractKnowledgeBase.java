package doser.entitydisambiguation.knowledgebases;

import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Each knowledge base provides its own class with its respective properties.
 * These are the knowledge base index uri. IndexSearcher, IndexReader objects
 * and the dynamic property.
 * 
 * @author stefan zwicklbauer
 */
public abstract class AbstractKnowledgeBase extends TimerTask {

	private String indexUri;
	
	private boolean dynamic;

	private SearcherManager manager;

	private IndexSearcher searcher;
	
	AbstractKnowledgeBase(String uri, boolean dynamic) {
		this(uri, dynamic, new DefaultSimilarity());
	}

	AbstractKnowledgeBase(String uri, boolean dynamic, Similarity sim) {
		super();
		this.indexUri = uri;
		this.dynamic = dynamic;

		File indexDir = new File(indexUri);
		Directory dir;
		try {
			dir = FSDirectory.open(indexDir);
			this.manager = new SearcherManager(dir, new SearcherFactory());
		} catch (IOException e) {
			Logger.getRootLogger().error("Error: ", e);
		}
	}

	public String getIndexUri() {
		return indexUri;
	}


	public IndexSearcher getSearcher() {
		try {
			this.searcher = manager.acquire();
		} catch (IOException e) {
			Logger.getRootLogger().error(e.getLocalizedMessage());
		}
		return this.searcher;
	}

	public void release() {
		try {
			manager.release(searcher);
		} catch (IOException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
	}

	/**
	 * Periodically reopens the Indexreader, if and only if this is an dynamic
	 * knowledge base. The changed knowledge base will be live within a few moments.
	 */
	@Override
	public void run() {
		if (dynamic) {
			try {
				manager.maybeRefresh();
			} catch (IOException e) {
				Logger.getRootLogger().error(e.getStackTrace());
			}
		}
	}
	
	public abstract void initialize();
}
