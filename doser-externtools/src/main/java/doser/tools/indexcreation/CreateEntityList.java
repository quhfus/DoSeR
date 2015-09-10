package doser.tools.indexcreation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Needs old Lucene Index
 * 
 * @author quh
 *
 */
public class CreateEntityList {

	public static void main(String[] args) throws IOException {
		HashSet<String> set = new HashSet<String>();
//		Model m = ModelFactory.createDefaultModel();
//		m.read("/home/zwicklbauer/HDTGeneration/Dbpedia2014/instance_types_en.nt");
		PrintWriter writer = new PrintWriter(
				"/home/zwicklbauer/entityList_test.dat");
//		StmtIterator it = m.listStatements();

//		while (it.hasNext()) {
//			Statement s = it.next();
//			Resource subject = s.getSubject();
//			set.add(subject.getURI());
//		}

		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(
				"/mnt/storage/zwicklbauer/urls.txt"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] splitter = line.split("\t");
			if (splitter.length == 6 || splitter.length == 5) {
				String uncovertedUri = splitter[2].replaceAll(
						"http://en.wikipedia.org/wiki/", "");
				String convertedUri = WikiPediaUriConverter
						.createConformDBpediaUrifromEncodedString(uncovertedUri);
				set.add(convertedUri);
			} else {
				System.out.println(line);
			}
		}

		reader.close();
		for (String s : set) {
			writer.println(s);
		}
		writer.close();
	}
}
