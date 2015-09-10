package doser.summarization.algorithm;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SummarizationSOLRIndex {

	public static void main(final String[] args) {
		final SummarizationSOLRIndex index = new SummarizationSOLRIndex();
		index.summarize("http://www.uniprot.org/uniprot/O35313");
	}

	public SummaryInfos summarize(final String uri) {
		// return summarize(uri, "/home/quh/Arbeitsfläche/Wissensbasen/Index/");
		return this
				.summarize(uri, "/home/quh/Arbeitsfläche/Wissensbasen/MMap/");
		// return summarize(uri, Properties.getInstance()
		// .getEntityCentricKBLocation());
	}

	public SummaryInfos summarize(final String uri, final String index) {
		SummaryInfos res = new SummaryInfos();
		final File indexDir = new File(index);
		try {
			final Directory dir = FSDirectory.open(indexDir);
			final IndexSearcher iSearcher = new IndexSearcher(
					DirectoryReader.open(dir));
			final IndexReader iReader = DirectoryReader.open(dir);
			final FuzzyQuery termq = new FuzzyQuery(new Term("Mainlink", uri));
			final TopDocs top = iSearcher.search(termq, 1);

			final ScoreDoc[] score = top.scoreDocs;

			final SummaryInfos infos = new SummaryInfos();
			if (score.length > 0) {
				final Document doc = iReader.document(score[0].doc);
				final String des = doc.get("Description");
				if (des != null) {
					infos.setSummary(des);
				}
				final String label = doc.get("Label");
				if (label != null) {
					infos.setLabel(label);
				}
			}
			infos.setUri(uri);
			iReader.close();
			res = infos;
		} catch (final IOException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return res;
	}
}
