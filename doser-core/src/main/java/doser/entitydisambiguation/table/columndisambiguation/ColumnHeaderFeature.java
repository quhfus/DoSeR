package doser.entitydisambiguation.table.columndisambiguation;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import doser.entitydisambiguation.properties.Properties;
import doser.entitydisambiguation.table.logic.Type;

public class ColumnHeaderFeature extends AbstractTypeDisFeatures {

	private IndexReader iReader;

	private IndexSearcher iSearcher;

	private final float weight = (float) Math.exp(-0.152491f);

	public ColumnHeaderFeature(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g) {
		super(g);
		final File file = new File(Properties.getInstance()
				.getTypeLuceneIndex());
		// File file = new File("/home/quh/Arbeitsfläche/index/");
		Directory dir;
		try {
			dir = FSDirectory.open(file);
			this.iReader = DirectoryReader.open(dir);
			this.iSearcher = new IndexSearcher(this.iReader);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public float computeFeature(final Type type) {
		return 1;
	}

	// private float computeTypeSim(Type type) {
	// BooleanQuery bq = new BooleanQuery();
	// TermQuery tq = new TermQuery(new Term("Type",
	// type.getUri().toLowerCase()));
	// bq.add(tq, Occur.MUST);
	// String splitter[] = cHeader.split(" ");
	// for (int i = 0; i < splitter.length; i++) {
	// String l = splitter[i].toLowerCase();
	// // Remove plural s
	// if (l.endsWith("s")) {
	// l = l.substring(0, l.length() - 1);
	// }
	// FuzzyQuery fq = new FuzzyQuery(new Term("PrefLabel", l));
	// bq.add(fq, Occur.MUST);
	// }
	// try {
	// TopDocs top = iSearcher.search(bq, 1);
	// ScoreDoc[] scoreValue = top.scoreDocs;
	// if (scoreValue.length > 0) {
	// System.out.println("HEADERVAL : "+scoreValue[0].score+ "URI :"+type.getUri());
	// return (weight * (float)(Math.sqrt(scoreValue[0].score)));
	// } else {
	// bq = new BooleanQuery();
	// tq = new TermQuery(new Term("Type",
	// type.getUri().toLowerCase()));
	// bq.add(tq, Occur.MUST);
	// splitter = cHeader.split(" ");
	// for (int i = 0; i < splitter.length; i++) {
	// String l = splitter[i].toLowerCase();
	// // Remove plural s
	// if (l.endsWith("s")) {
	// l = l.substring(0, l.length() - 1);
	// }
	// FuzzyQuery fq = new FuzzyQuery(new Term("Synonyms", l));
	// bq.add(fq, Occur.MUST);
	// }
	// top = iSearcher.search(bq, 1);
	// scoreValue = top.scoreDocs;
	// if (scoreValue.length > 0) {
	// return (weight * (float)(Math.sqrt(scoreValue[0].score)));
	// }
	// return 0;
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return 0;
	// }
}