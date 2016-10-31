package OtherClasses;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
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

public class AddMissingDocs {

	public static final String OLDINDEX = "/home/quh/Arbeitsfläche/Wikipedia_Default_Aida_Sigir";
	public static final String UPDATEINDEX = "/home/quh/Arbeitsfläche/Lucene_Test";

	public static void main(String[] args) {
		AddMissingDocs add = new AddMissingDocs();
	}
	
	AddMissingDocs() {
		action();
	}

	public void action() {
		Path oldIndexFile =  Paths.get(OLDINDEX);
		Path newIndexFile =  Paths.get(UPDATEINDEX);

		IndexWriter newIndexWriter = null;
		IndexReader readerOldIndex = null;
		IndexReader readerNewIndex = null;
		try {
			final Directory oldDir = FSDirectory.open(new File(OLDINDEX));
			final Directory newDir = FSDirectory.open(new File(UPDATEINDEX));
			readerOldIndex = DirectoryReader.open(oldDir);
			readerNewIndex = DirectoryReader.open(newDir);
			IndexSearcher searcher = new IndexSearcher(readerNewIndex);
			
			Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
			analyzerPerField.put("Label", new DoserIDAnalyzer());
			analyzerPerField.put("PattyRelations", new DoserIDAnalyzer());
			analyzerPerField.put("PattyFreebaseRelations", new DoserIDAnalyzer());
			analyzerPerField.put("Relations", new DoserIDAnalyzer());
			analyzerPerField.put("Occurrences", new DoserIDAnalyzer());
			analyzerPerField.put("Type", new DoserIDAnalyzer());
			analyzerPerField.put("StringLabel", new DoserIDAnalyzer());

			PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);
			IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, aWrapper);
			newIndexWriter = new IndexWriter(newDir, config);

			for (int i = 0; i < 10000; i++) {
				Document olddoc = readerOldIndex.document(i);
				String mainlink = olddoc.get("Mainlink");
				Term term = new Term("Mainlink", mainlink);
				TermQuery q = new TermQuery(term);
				TopDocs topdocs = searcher.search(q, 1);
				ScoreDoc[] scoredocs = topdocs.scoreDocs;
				if (scoredocs.length == 0) {
					Document doc = new Document();
					// Add ID
					doc.add(new StringField("ID", "DBpedia_" + olddoc.get("ID"), Store.YES));
					// Add Mainlink
					doc.add(new StringField("Mainlink", olddoc.get("Mainlink"), Store.YES));
					// Add Labels
					IndexableField[] field = olddoc.getFields("Label");
					for (int j = 0; j < field.length; j++) {
						doc.add(new TextField("Label", field[j].stringValue(), Store.YES));
					}
					field = olddoc.getFields("StringLabel");
					for (int j = 0; j < field.length; j++) {
						doc.add(new StringField("StringLabel", field[j].stringValue(), Store.YES));
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
					for (int j = 0; j < field.length; j++) {
						doc.add(new StringField("UniqueLabel", field[j].stringValue(), Store.YES));
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
					newIndexWriter.addDocument(doc);
//					System.out.println("Müsste adden: "+mainlink);
				} else {
//					System.out.println("Müsste nicht adden: "+mainlink);
				}

				if (i % 10000 == 0) {
					System.out.println(i);
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
			if (readerNewIndex != null) {
				try {
					readerNewIndex.close();
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

}
