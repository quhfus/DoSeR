package doser.tools;

import java.io.File;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import doser.lucene.query.TermQuery;

public class LuceneTest {

	public static void main(String [] args) throws Exception{
		IndexReader reader = IndexReader.open(FSDirectory.open(new File("/home/stefan/Arbeitsfläche/mnt/ssd1/disambiguation/LuceneIndex/Wikipedia_Default_AidaNew/")));
		IndexSearcher searcher = new IndexSearcher(reader);
		TermQuery q = new TermQuery(new Term("UniqueLabel", "o'donnell"));
		TopDocs docs = searcher.search(q, 20);
		ScoreDoc[] doc = docs.scoreDocs;
		for (int i = 0; i < doc.length; i++) {
			Document document = reader.document(doc[i].doc);
			System.out.println(document.get("Mainlink"));
		}
	}
	
}
