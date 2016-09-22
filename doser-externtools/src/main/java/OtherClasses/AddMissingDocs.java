package OtherClasses;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import doser.lucene.query.TermQuery;

public class AddMissingDocs {

	public static final String OLDINDEX = "/home/quh/Arbeitsfläche/Wikipedia_Default_Aida_Sigir";
	public static final String UPDATEINDEX = "/home/quh/Arbeitsfläche/Wikipedia_Default_Aida_Sigir_Update";

	public static void main(String[] args) {
		AddMissingDocs add = new AddMissingDocs();
	}
	
	AddMissingDocs() {
		action();
	}

	public void action() {
		File oldIndexFile = new File(OLDINDEX);
		File newIndexFile = new File(UPDATEINDEX);
		IndexWriter newIndexWriter = null;
		IndexReader readerOldIndex = null;
		IndexReader readerNewIndex = null;
		try {
			final Directory oldDir = FSDirectory.open(oldIndexFile);
			final Directory newDir = FSDirectory.open(newIndexFile);
			readerOldIndex = DirectoryReader.open(oldDir);
			readerNewIndex = DirectoryReader.open(newDir);
			IndexSearcher searcher = new IndexSearcher(readerNewIndex);
			IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new StandardAnalyzer());
			newIndexWriter = new IndexWriter(newDir, config);

			for (int i = 0; i < readerOldIndex.numDocs(); i++) {
				Document doc = readerOldIndex.document(i);
				String mainlink = doc.get("Mainlink");
				System.out.println(mainlink);
				Term term = new Term("Mainlink", mainlink);
				TermQuery q = new TermQuery(term);
				TopDocs topdocs = searcher.search(q, 1);
				ScoreDoc[] scoredocs = topdocs.scoreDocs;
				System.out.println(scoredocs.length);
				if (scoredocs.length == 0) {
				//	newIndexWriter.addDocument(doc);
					System.out.println("Müsste adden: "+mainlink);
				} else {
					System.out.println("Müsste nicht adden: "+mainlink);
				}

				if (i % 10000 == 0) {
					System.out.println(i);
				}
				break;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (readerOldIndex != null) {
				try {
					readerOldIndex.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (readerNewIndex != null) {
				try {
					readerNewIndex.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (newIndexWriter != null) {
				try {
					newIndexWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
