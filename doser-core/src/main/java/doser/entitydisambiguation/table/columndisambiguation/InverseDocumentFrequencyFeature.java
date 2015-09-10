package doser.entitydisambiguation.table.columndisambiguation;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import doser.entitydisambiguation.properties.Properties;
import doser.entitydisambiguation.table.logic.Type;

public class InverseDocumentFrequencyFeature extends AbstractTypeDisFeatures {

	private IndexReader iReader;

	private IndexSearcher iSearcher;

	private final static float WEIGHT = 0.005f;

	public InverseDocumentFrequencyFeature(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> grah) {
		super(grah);
		final File file = new File(Properties.getInstance()
				.getTypeLuceneIndex());
		Directory dir;
		try {
			dir = FSDirectory.open(file);
			this.iReader = DirectoryReader.open(dir);
			this.iSearcher = new IndexSearcher(this.iReader);
		} catch (final IOException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
	}

	@Override
	public float computeFeature(final Type type) {
		float res = 0;
		if (type.getUri() != null) {
			final String uri = type.getUri();
			String correctedString = "";
			if (uri.contains("Category:")) {
				final String[] splitter = uri.split(":");
				correctedString = "http://yago-knowledge.org/resource/wikicategory_"
						+ splitter[2];
			} else {
				correctedString = uri;
			}
			final TermQuery tquery = new TermQuery(new Term("Type",
					correctedString.toLowerCase(Locale.US)));
			TopDocs docs;
			try {
				docs = this.iSearcher.search(tquery, 1);
				final ScoreDoc[] scoredocs = docs.scoreDocs;
				if (scoredocs.length == 0) {
					return 0;
				}
				final String number = this.iReader.document(scoredocs[0].doc).get("Number");

				final double val1 = (this.iReader.maxDoc() / Double
						.parseDouble(number)) + 1;
				res = (float) (WEIGHT * Math.sqrt(Math.log(val1)
						/ Math.log(2)));
			} catch (final IOException e) {
				Logger.getRootLogger().error(e.getStackTrace());
			}
			return 0;
		}
		return res;
	}
}
