package doser.general;

import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class Test {
	private IndexWriter writer;

	public void lucene() throws IOException, ParseException {
		// Build the index
		StandardAnalyzer analyzer = new StandardAnalyzer();
		Directory index = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST,
				analyzer);
		this.writer = new IndexWriter(index, config);

		// Add documents to the index
		addDoc("Spring", new String[] { "Java", "JSP", "DBPEDIA_56testdoc" });
		addDoc("Java", new String[] { "Oracle", "Annotation is cool too" });

		writer.close();

		// Search the index
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);

		TermQuery q = new TermQuery(new Term("keyword", "DBPEDIA_56testdoc"));
		// SpanQuery q = new SpanNearQuery(new SpanQuery[] {
		// new SpanTermQuery(new Term("keyword", "too")),
		// new SpanTermQuery(new Term("keyword", "cool"))},
		// 3,
		// true);

		// String[] s = {"cool", "too"};
		// for (int i = 0; i < s.length; i++) {
		// q.add(new Term("keyword", s[i]));
		// }

		// q.add(new PhraseQuery(new Term("keyword", "Annotation is cool")),
		// Occur.MUST);

		System.out.println(q.toString());

		int hitsPerPage = 10;
		TopScoreDocCollector collector = TopScoreDocCollector.create(
				hitsPerPage, true);

		searcher.search(q, collector);

		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document doc = searcher.doc(docId);
			System.out.println(hits[i].toString());
			System.out.println((i + 1) + ". \t" + doc.get("title"));
		}

		reader.close();
	}

	private void addDoc(String title, String[] keywords) throws IOException {
		// Create new document
		Document doc = new Document();

		// Add title
		doc.add(new TextField("title", title, Store.YES));

		// Add keywords
		for (int i = 0; i < keywords.length; i++) {
			doc.add(new StringField("keyword", keywords[i], Store.YES));
		}

		// Add document to index
		this.writer.addDocument(doc);
	}

	public static void main(String[] args) {
		Test test = new Test();
		try {
			test.lucene();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
