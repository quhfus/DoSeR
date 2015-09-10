package DBpediaCategoryCorrection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class ExtractRelevantDBpediaCategories {

	public static void main(String[] args) {
		Model m = ModelFactory.createDefaultModel();
		m.read(args[0]);
		StmtIterator iter = m.listStatements();
		HashSet<String> hash = new HashSet<String>();
		while (iter.hasNext()) {
			Statement stmt = iter.next();
			RDFNode node = stmt.getObject();
			String uri = node.asResource().getURI();
			hash.add(uri);
		}
		File output = new File(args[1]);
		try {
			PrintWriter writer = new PrintWriter(output);
			for(String s : hash) {
				writer.println(s);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
