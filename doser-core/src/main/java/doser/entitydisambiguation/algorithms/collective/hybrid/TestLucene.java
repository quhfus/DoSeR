package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import doser.lucene.query.TermQuery;

public class TestLucene {

	public static void main(String[] args) {
		IndexSearcher searcher;
		try {
			searcher = new IndexSearcher(IndexReader.open(FSDirectory.open(new File("/home/quh/Arbeitsfläche/NewIndexTryout/"))));
			final IndexReader reader = searcher.getIndexReader();
			try {
				Query query = new TermQuery(new Term("Label", "Washington"));
				final TopDocs top = searcher.search(query, 25000);
				final ScoreDoc[] score = top.scoreDocs;
				System.out.println(score.length);
				for (int i = 0; i < score.length; i++) {
					if(reader.document(score[i].doc).get("Mainlink").equals("http://dbpedia.org/resource/Washington,_D.C.")) {
	//					System.out.println(reader.document(score[i].doc).get("Mainlink"));
					}
						System.out.println(reader.document(score[i].doc).get("Mainlink"));
				}
			} catch (IOException e) {
				Logger.getRootLogger().error("Lucene Searcher Error: ", e);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
