package doser.entitydisambiguation.algorithms.collective;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Test1 {

	public static void main(String[] args) {
		Directory dir;
		try {
			dir = FSDirectory.open(new File(
					"/home/quh/Arbeitsfläche/NewIndexTryout"));
			IndexReader reader = IndexReader.open(dir);
			IndexSearcher searcher = new IndexSearcher(reader);
			Query q = new TermQuery(new Term("DBpediaUniqueLabel", "torso"));
			TopDocs tdocs = searcher.search(q, 130);
			ScoreDoc[] sdocs = tdocs.scoreDocs;
			for (int i = 0; i < sdocs.length; i++) {
				System.out.println(reader.document(sdocs[i].doc).get("Mainlink"));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
