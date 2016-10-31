package doc2vec.corpuscreation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class CreateDBpediaDoc2VecCorpus {

	public static final String index = "/home/quh/Arbeitsfläche/Wikipedia_Default_Aida_Sigir_Update";
	public static final String output = "/home/quh/Arbeitsfläche/doc2vec_input_dbpedia.dat";

	public static void main(String[] args) {
		CreateDBpediaDoc2VecCorpus c = new CreateDBpediaDoc2VecCorpus();
		try {
			c.action();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void action() throws Exception {
		PrintWriter writer = new PrintWriter(new File(output));
		Directory dir = FSDirectory.open(new File(index));
		IndexReader reader = DirectoryReader.open(dir);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < reader.numDocs(); i++) {
			Document doc = reader.document(i);
			String desc = doc.get("LongDescription");
			String uri = doc.get("Mainlink");
			if (desc != null && !desc.equalsIgnoreCase("")) {
				builder.append(uri.replaceAll("http://dbpedia.org/resource/", "") + "_0\t");
				String d = analyze(desc);
//				System.out.println(d);
				String t = builder.toString();
				if(t != null && !t.equalsIgnoreCase("") && !t.equalsIgnoreCase(" ")) {
					
				} else {
					System.out.println("blub");
				}
				
				if(d != null && !d.equalsIgnoreCase("") && !d.equalsIgnoreCase(" ")) {
					builder.append(analyze(desc));
				} else {
					System.out.println("das ist doof");
				}
				writer.println(builder.toString());
				writer.flush();
				builder = new StringBuilder();
			}
		}
		writer.close();
		reader.close();
	}
	private String analyze(String text) {
		Analyzer ana = new StandardAnalyzer();
		StringBuilder builder = new StringBuilder();
		try {
			TokenStream stream = ana.tokenStream("", new StringReader(text));

			stream.reset();
			while (stream.incrementToken()) {
				builder.append(stream.getAttribute(CharTermAttribute.class).toString()+" ");
//				result.add(stream.getAttribute(CharTermAttribute.class).toString());
			}
		} catch (IOException e) {
			// not thrown b/c we're using a string reader...
		}
		ana.close();
		return builder.toString().trim();
	}

}
