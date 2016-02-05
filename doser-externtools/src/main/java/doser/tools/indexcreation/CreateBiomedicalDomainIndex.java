package doser.tools.indexcreation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import doser.lucene.analysis.DoserIDAnalyzer;

public class CreateBiomedicalDomainIndex {

	private String oldIndexPath = "/home/quh/Arbeitsfläche/MMapLuceneIndexStandard/";

	private String newIndexPath = "/home/quh/Arbeitsfläche/BiomedicalIndex";

	CreateBiomedicalDomainIndex() {
		super();
	}

	private void readOldIndex() {
		File oldIndexFile = new File(oldIndexPath);
		File newIndexFile = new File(newIndexPath);
		IndexReader readerOldIndex = null;
		IndexWriter newIndexWriter = null;
		try {
			final Directory newDir = FSDirectory.open(newIndexFile);

			Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
			analyzerPerField.put("Label", new DoserIDAnalyzer());
			analyzerPerField.put("Occurrences", new DoserIDAnalyzer());
			analyzerPerField.put("Type", new DoserIDAnalyzer());

			PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);

			final IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, aWrapper);
			newIndexWriter = new IndexWriter(newDir, config);

			final Directory oldDir = FSDirectory.open(oldIndexFile);
			readerOldIndex = DirectoryReader.open(oldDir);
			for (int j = 0; j < readerOldIndex.maxDoc(); ++j) {
				Document oldDoc = readerOldIndex.document(j);
				String link = oldDoc.get("Mainlink");
				if (!link.startsWith("http://dbpedia.org/resource/")) {
					Document doc = new Document();

					doc.add(new StringField("Mainlink", oldDoc.get("Mainlink"), Store.YES));

					doc.add(new TextField("LongDescription", oldDoc.get("Description"), Store.YES));

					doc.add(new StringField("Occurrences", oldDoc.get("Occurrences"), Store.YES));

					doc.add(new StringField("ID", oldDoc.get("ID"), Store.YES));

					doc.add(new TextField("Label", oldDoc.get("Label").toLowerCase(), Store.YES));
					// Generate UniqueLabelStrings
					HashSet<String> uniqueLabelStrings = new HashSet<String>();
					uniqueLabelStrings.add(oldDoc.get("Label").toLowerCase());
					String s = oldDoc.get("Occurrences");
					String[] splitter1 = s.split(";;;");
					for (int i = 0; i < splitter1.length; i++) {
						String[] splitter2 = splitter1[i].split(":::");
						for (int k = 0; k < splitter2.length; k++) {
							uniqueLabelStrings.add(splitter2[0]);
						}
					}
					for (String uniqueString : uniqueLabelStrings) {
						doc.add(new StringField("UniqueLabel", uniqueString, Store.YES));
					}
					newIndexWriter.addDocument(doc);
				}
			}
			readerOldIndex.close();
			newIndexWriter.close();
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
			if (newIndexWriter != null) {
				try {
					newIndexWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		CreateBiomedicalDomainIndex indexCreation = new CreateBiomedicalDomainIndex();
		indexCreation.readOldIndex();
	}
}
