package OtherClasses;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Test {

	public static void main(String[] args) throws Exception {
		Path oldIndexFile =  Paths.get("/home/quh/Arbeitsfläche/Lucene_Test");
		final Directory newDir = FSDirectory.open(new File("/home/quh/Arbeitsfläche/Lucene_Test"));
//		final Directory newDir = FSDirectory.open(oldIndexFile);

		
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new StandardAnalyzer());
		IndexWriter writer = new IndexWriter(newDir, config);
		Document doc = new Document();
		FieldType myFieldType = new FieldType(TextField.TYPE_STORED);
		myFieldType.setStoreTermVectors(true);
		Field f = new Field("Description", "Charles London (born August 12, 1975) is an American college football coach and former player. He is currently the running backs coach of the Houston Texans", myFieldType);
		doc.add(f);
		writer.addDocument(doc);
		
		Document doc1 = new Document();
		FieldType myFieldType1 = new FieldType(TextField.TYPE_STORED);
		myFieldType1.setStoreTermVectors(true);
		Field f1 = new Field("Description", "Londen (Engels: London) is de hoofdstad en grootste stad van zowel Engeland als het Verenigd Koninkrijk.In de regio Groot-Londen, waarvan de begrenzing tegenwoordig vaak gelijk wordt gesteld aan die van de stad Londen, wonen ongeveer 7,5 miljoen mensen. Hiermee is Londen de stad met de meeste inwoners", myFieldType);
		doc1.add(f1);	
		writer.addDocument(doc1);
		writer.commit();
		writer.close();
	}

}
