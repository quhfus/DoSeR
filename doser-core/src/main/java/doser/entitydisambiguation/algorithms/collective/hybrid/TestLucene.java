package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;

import doser.entitydisambiguation.algorithms.collective.SurfaceForm;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.lucene.query.LearnToRankClause;
import doser.lucene.query.LearnToRankQuery;
import doser.lucene.query.LearnToRankTermQuery;

public class TestLucene {
	//
	public static void main(String[] args) {

		IndexSearcher searcher;
		try {
			searcher = new IndexSearcher(IndexReader.open(FSDirectory
					.open(new File("/home/quh/Arbeitsfläche/NewIndexTryout/"))));
			final IndexReader reader = searcher.getIndexReader();
			LearnToRankQuery query = new LearnToRankQuery();
			List<LearnToRankClause> features = new LinkedList<LearnToRankClause>();
			final LearnToRankTermQuery q = new LearnToRankTermQuery(new Term(
					"Mainlink", "http://dbpedia.org/resource/North_Carolina"), new DefaultSimilarity());

			query.add(q, "Feature1", true);
			try {
				long time = System.currentTimeMillis();
				final TopDocs top = searcher.search(query, 3000);
				final ScoreDoc[] score = top.scoreDocs;
				for (int i = 0; i < score.length; i++) {

						System.out.println(reader.document(score[i].doc).get(
								"Mainlink"));
					
				}
				System.out.println(System.currentTimeMillis() - time);
			} catch (final IOException e) {
				Logger.getRootLogger().error("Lucene Searcher Error: ", e);
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
