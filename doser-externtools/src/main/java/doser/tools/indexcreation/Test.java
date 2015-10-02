package doser.tools.indexcreation;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import doser.lucene.query.TermQuery;

public class Test {

	public static void main(String[] args) {
		try {
			IndexReader reader = IndexReader.open(FSDirectory.open(new File(
					"/home/quh/Arbeitsfläche/NewIndexTryout/")));
			IndexSearcher searcher = new IndexSearcher(reader);
			Query q = new TermQuery(new Term("Label", "Washington"));
			TopDocs td = searcher.search(q, 1000);
			ScoreDoc[] sd = td.scoreDocs;
			for (int i = 0; i < sd.length; i++) {
				int docNr = sd[i].doc;
				Document doc = reader.document(docNr);
				System.out.println(doc.get("Mainlink"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
