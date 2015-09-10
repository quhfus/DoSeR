package test;

import java.io.File;
import java.io.PrintWriter;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class BeselExporter {

	public static void main(String args[]) throws Exception{
		PrintWriter writer = new PrintWriter(new File("/home/quh/Arbeitsfläche/Besel.txt"));
		Directory dir = FSDirectory.open(new File("/home/quh/Arbeitsfläche/NewIndexTryout"));
		IndexReader iReader = DirectoryReader.open(dir);
		int maxDocs = iReader.maxDoc();
		for(int i = 0; i < maxDocs; ++i) {
			Document doc = iReader.document(i);
			String occs = doc.get("Occurrences");
			String entity = doc.get("Mainlink");
			writer.println(entity+"\t"+occs);
		}
		writer.close();
	}
	
}
