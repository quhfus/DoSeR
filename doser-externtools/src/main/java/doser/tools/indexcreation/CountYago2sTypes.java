package doser.tools.indexcreation;

import java.io.IOException;
import java.util.HashMap;

import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class CountYago2sTypes {
	
	private final static String TYPES = "/home/quh/HDT/yagoTypes.hdt";
	
	public static void main(String[] args) {
		HDT hdt = null;
		try {
			hdt = HDTManager.mapIndexedHDT(TYPES, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HDTGraph graph = new HDTGraph(hdt);
		Model m = ModelFactory.createModelForGraph(graph);
		StmtIterator iter = m.listStatements();
		HashMap<String, Integer> hash = new HashMap<String, Integer>();
		int number = 0;
		while (iter.hasNext()) {
			if (number % 50000 == 0) {
				System.out.println("Processed Entries: " + number);
			}
			Statement stmt = iter.next();
			RDFNode object = stmt.getObject();
			String s = null;
			s = object.asResource().getURI();
			hash.put(s, 0);
			number++;
		}
		System.out.println("Anzahl an Typen: " +hash.size());
	}
}
