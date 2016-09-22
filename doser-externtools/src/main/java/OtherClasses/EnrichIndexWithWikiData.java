package OtherClasses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import doser.lucene.analysis.DoserIDAnalyzer;
import doser.lucene.query.TermQuery;

public class EnrichIndexWithWikiData {

	public static final String OLDINDEX = "/home/quh/Arbeitsfläche/Wikipedia_Default_Aida_Sigir";
	public static final String UPDATEINDEX = "/home/quh/Arbeitsfläche/Wikipedia_Default_Aida_Sigir_Update";
	public static final String DOC2VECFILE = "/home/quh/Arbeitsfläche/doc2vec_corpus.dat";

	public static int counter = 0;
	
	public static void main(String[] args) {
		EnrichIndexWithWikiData en = new EnrichIndexWithWikiData();
	}
	
	public EnrichIndexWithWikiData() {
		super();
		action();
	}

	public void action() {
		File oldIndexFile = new File(OLDINDEX);
		File newIndexFile = new File(UPDATEINDEX);
		IndexReader readerOldIndex = null;
		BufferedReader buffreader = null;
		IndexWriter newIndexWriter = null;
		try {
			Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
			analyzerPerField.put("Label", new DoserIDAnalyzer());
			analyzerPerField.put("PattyRelations", new DoserIDAnalyzer());
			analyzerPerField.put("PattyFreebaseRelations", new DoserIDAnalyzer());
			analyzerPerField.put("Relations", new DoserIDAnalyzer());
			analyzerPerField.put("Occurrences", new DoserIDAnalyzer());
			analyzerPerField.put("Type", new DoserIDAnalyzer());
			analyzerPerField.put("StringLabel", new DoserIDAnalyzer());
			analyzerPerField.put("Wikitext", new StandardAnalyzer());

			PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);

			final IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, aWrapper);
			final Directory oldDir = FSDirectory.open(oldIndexFile);
			readerOldIndex = DirectoryReader.open(oldDir);
			
			final Directory newDir = FSDirectory.open(newIndexFile);
			IndexSearcher searcher = new IndexSearcher(readerOldIndex);
			newIndexWriter = new IndexWriter(newDir, config);

			buffreader = new BufferedReader(new FileReader(new File(DOC2VECFILE)));
			String line = null;
			while ((line = buffreader.readLine()) != null) {
				counter++;
				String[] linecontent = line.split(" ");
				String uri = linecontent[0];

				Term term = new Term("Mainlink", uri);
				TermQuery q = new TermQuery(term);
				TopDocs topdocs = searcher.search(q, 1);
				ScoreDoc[] scoredocs = topdocs.scoreDocs;
				if (scoredocs.length > 0) {
//					System.out.println("Found: " + uri);
					int docNr = scoredocs[0].doc;
					Document olddoc = readerOldIndex.document(docNr);
					Document doc = new Document();
					// Add ID
					doc.add(new StringField("ID", "DBpedia_" + olddoc.get("ID"), Store.YES));
					// Add Mainlink
					doc.add(new StringField("Mainlink", olddoc.get("Mainlink"), Store.YES));
					// Add Labels
					IndexableField[] field = olddoc.getFields("Label");
					for (int i = 0; i < field.length; i++) {
						doc.add(new TextField("Label", field[i].stringValue(), Store.YES));
					}
					field = olddoc.getFields("StringLabel");
					for (int i = 0; i < field.length; i++) {
						doc.add(new StringField("StringLabel", field[i].stringValue(), Store.YES));
					}
					// Add ShortDescriptions
					doc.add(new TextField("ShortDescription", olddoc.get("ShortDescription"), Store.YES));
					// Add longDescriptions
					doc.add(new TextField("LongDescription", olddoc.get("LongDescription"), Store.YES));
					// Add Type
					doc.add(new StringField("Type", olddoc.get("Type"), Store.YES));
					// Add Occurrences
					doc.add(new StringField("Occurrences", olddoc.get("Occurrences"), Store.YES));
					// UniqueLabelStrings
					field = olddoc.getFields("UniqueLabel");
					for (int i = 0; i < field.length; i++) {
						doc.add(new StringField("UniqueLabel", field[i].stringValue(), Store.YES));
					}
					// Add DBPedia Facts
					doc.add(new TextField("Relations", olddoc.get("Relations"), Store.YES));
					// Add PattyFacts
					doc.add(new TextField("PattyRelations", olddoc.get("PattyRelations"), Store.YES));
					// Add PattyFreebaseFacts
					doc.add(new TextField("PattyFreebaseRelations", olddoc.get("PattyFreebaseRelations"), Store.YES));
					// Add DBpediaPriors
					String vdegree = olddoc.get("DbpediaVertexDegree");
					if(vdegree == null) {
						vdegree = "0";
					}
					doc.add(new TextField("DbpediaVertexDegree", vdegree, Store.YES));
					
					doc.add(new TextField("Wikitext", createText(linecontent), Store.YES));
					//System.out.println(createText(linecontent));
					newIndexWriter.addDocument(doc);
//					newIndexWriter.updateDocument(term, doc);
				} else {
//					System.out.println("NotFound: " + uri);
				}
				if (counter % 10000 == 0) {
					System.out.println(counter);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (readerOldIndex != null) {
				try {
					readerOldIndex.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (buffreader != null) {
				try {
					readerOldIndex.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (newIndexWriter != null) {
				try {
					newIndexWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String createText(String[] arr) {
		StringBuilder builder = new StringBuilder();
		for (int i = 1; i < arr.length; i++) {
			builder.append(arr[i]+" ");
		}
		return builder.toString().trim();
	}

}
