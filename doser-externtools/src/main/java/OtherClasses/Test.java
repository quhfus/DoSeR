package OtherClasses;

import java.io.File;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import doser.lucene.query.TermQuery;

public class Test {

	public static void main(String[] args) throws Exception {
		File oldIndexFile = new File("/home/quh/Arbeitsfläche/Wikipedia_Default_Aida_Sigir_Update");
		final Directory oldDir = FSDirectory.open(oldIndexFile);
		IndexReader readerOldIndex = DirectoryReader.open(oldDir);
		
		IndexSearcher searcher = new IndexSearcher(readerOldIndex);
		
		Term term = new Term("Mainlink", "http://dbpedia.org/resource/Anarchism");
		TermQuery q = new TermQuery(term);
		TopDocs topdocs = searcher.search(q, 1);
		ScoreDoc[] scoredocs = topdocs.scoreDocs;
		System.out.println(scoredocs.length);
	}

}
