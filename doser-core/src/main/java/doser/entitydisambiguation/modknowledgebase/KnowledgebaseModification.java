package doser.entitydisambiguation.modknowledgebase;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Admin class for all knowledge base modifications. First, all knowledge base
 * modification affect the static lucene indexes. If dynamic knowledge bases are
 * subscribed, these changes have to be made at the live system via
 * DynamicKBAdminInterface.
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class KnowledgebaseModification {

	private static KnowledgebaseModification mod = null;

	public static synchronized KnowledgebaseModification getInstance() {
		if (mod == null) {
			mod = new KnowledgebaseModification();
		}
		return mod;
	}

	public void processNewKnowledgeOperation(
			final AbstractKnowledgebaseOperator operator)
			throws ModifyKnowledgeBaseException {
		final Analyzer analyzer = operator.getAnalyzer();
		final IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		final File kbdir = new File(operator.getKnowledgeBasePath());
		if (!kbdir.exists()) {
			throw new ModifyKnowledgeBaseException("Index not found", null);
		}
		try {
			final Directory dir = FSDirectory.open(kbdir);
			final IndexWriter writer = new IndexWriter(dir, config);
			boolean isAllowedToPro = false;
			IndexSearcher searcher = null;
			if (writer.maxDoc() == 0) {
				if (operator instanceof AddNewDocumentsOperator) {
					isAllowedToPro = true;
				}
			} else {
				isAllowedToPro = true;
				searcher = new IndexSearcher(DirectoryReader.open(dir));
			}
			if (isAllowedToPro) {
				try {
					operator.modifyIndex(writer, searcher);
				} catch (final ModifyKnowledgeBaseException e) {
					writer.close();
					throw e;
				}
				writer.commit();
				writer.close();
			}
		} catch (final IOException e) {
			throw new ModifyKnowledgeBaseException(
					"IOException in IndexWriter or IndexSearcher", e);
		}
	}
}
