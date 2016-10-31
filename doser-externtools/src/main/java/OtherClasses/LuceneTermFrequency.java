package OtherClasses;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import doser.lucene.query.TermQuery;

public class LuceneTermFrequency {

	public static void main(String[] args) {
		LuceneTermFrequency term = new LuceneTermFrequency();
		try {
			term.action();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void action() throws Exception {
		final Directory oldDir = FSDirectory.open(new File("/home/quh/Arbeitsfläche/Wikipedia_Default_Aida_Sigir"));
		IndexReader reader = DirectoryReader.open(oldDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		TermQuery query = new TermQuery(new Term("UniqueLabel", "united states"));
		TopDocs td = searcher.search(query, 1000);
		ScoreDoc[] sd = td.scoreDocs;
		for(int i = 0; i < sd.length; i++) {
			Document doc = reader.document(sd[i].doc);
			System.out.println(doc.get("Mainlink"));
		}
		System.out.println(sd.length);
	}
}
