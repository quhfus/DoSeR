package doser.tools.indexcreation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import doser.lucene.analysis.DoserIDAnalyzer;

public class AddFactsToIndex {
	public static final String NTFILE = "/home/zwicklbauer/HDTGeneration/mappingbased_properties_cleaned_en.nt";
	public static final String OLDINDEX = "/mnt/ssd1/disambiguation/MMapLuceneIndexStandard/";
	public static final String NEWINDEX = "/home/zwicklbauer/NewIndexTryout";

	public static void main(String[] args) {

		HashMap<String, LinkedList<String>> map = new HashMap<String, LinkedList<String>>();

		Model m = ModelFactory.createDefaultModel();

		m.read(NTFILE);

		StmtIterator it = m.listStatements();

		while (it.hasNext()) {
			Statement s = it.next();
			Resource subject = s.getSubject();
			Property pra = s.getPredicate();
			RDFNode object = s.getObject();
			if (object.isResource()) {
				Resource obj = object.asResource();
				if (pra.isResource()
						&& obj.getURI().startsWith(
								"http://dbpedia.org/resource/")) {
					if (!map.containsKey(subject.getURI())) {
						LinkedList<String> list = new LinkedList<String>();
						map.put(subject.getURI(), list);
					}
					LinkedList<String> l = map.get(subject.getURI());
					l.add(pra.getURI().replaceAll(
							"http://dbpedia.org/ontology/", "dbpediaOnt/")
							+ ":::"
							+ obj.getURI().replaceAll(
									"http://dbpedia.org/resource/",
									"dbpediaRes/"));

				}
			}
		}

		File oldIndexFile = new File(OLDINDEX);
		File newIndexFile = new File(NEWINDEX);
		try {
			final Directory oldDir = FSDirectory.open(oldIndexFile);
			final Directory newDir = FSDirectory.open(newIndexFile);
			final IndexWriterConfig config = new IndexWriterConfig(
					Version.LATEST, new DoserIDAnalyzer());
			final IndexReader readerOldIndex = DirectoryReader.open(oldDir);
			final IndexWriter newIndexWriter = new IndexWriter(newDir, config);
			int numDocs = readerOldIndex.maxDoc();
			for (int i = 0; i < numDocs; i++) {
				Document doc = readerOldIndex.document(i);
				String docurl = doc.get("Mainlink");
				LinkedList<String> l = map.get(docurl);
				StringBuilder builder = new StringBuilder();
				if (l != null) {
					for (String str : l) {
						builder.append(str);
						builder.append(";;;");
					}
				}
				String s = builder.toString();
				if (s.length() > 0) {
					s = s.substring(0, s.length() - 3);
				}
				doc.add(new TextField("Relations", s, Store.YES));
				newIndexWriter.addDocument(doc);
			}
			readerOldIndex.close();
			newIndexWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}