package word2vec.corpuscreation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class CreateDBpediaEdgeList {

		public static final String INFOBOXPROPERTIES = "/home/zwicklbauer/HDTGeneration/infobox_properties_en.nt";
		public static final String MAPPINGPROPERTIES = "/home/zwicklbauer/HDTGeneration/mappingbased_properties_cleaned_en.nt";

		public static final String EDGELIST = "/home/zwicklbauer/dbpediagraph/deepwalk/dbpedia_edgelist.dat";
		public static final String INVERTEDLST = "/home/zwicklbauer/dbpediagraph/deepwalk/invertedEntityIdList.dat";
		
		private Model infoboxes = null;
		private Model mapping = null;
		
		private HashSet<String> hashnames;
		private HashMap<Integer, String> hashinverted;
		private HashMap<String, Integer> hash;
		
		private PrintWriter writer;
		
		public CreateDBpediaEdgeList() {
			super();
			try {
				this.writer = new PrintWriter(EDGELIST);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		public void createDBpediaGraph() {
			System.out.println("Generate Ids");
			generateIDHashMap();
			System.out.println("Add Facts");
			parseFacts();
			writer.flush();
			writer.close();
		}
		
		private void generateIDHashMap() {
			System.out.println("Read Model");
			this.hashnames = new HashSet<String>();
			infoboxes = ModelFactory.createDefaultModel();
			infoboxes.read(INFOBOXPROPERTIES);
			StmtIterator it = infoboxes.listStatements();
			System.out.println("Finished Model");
			while (it.hasNext()) {
				Statement s = it.next();
				Resource subject = s.getSubject();
				RDFNode object = s.getObject();
				this.hashnames.add(subject.getURI());
				if (object.isResource()) {
					Resource obj = object.asResource();
					if(obj.getURI().startsWith("http://dbpedia.org/resource/")) {
						this.hashnames.add(obj.getURI());
					}
				}
			}
			System.out.println("Read Model");
			mapping = ModelFactory.createDefaultModel();
			mapping.read(MAPPINGPROPERTIES);
			it = mapping.listStatements();
			System.out.println("Finished Model");
			while (it.hasNext()) {
				Statement s = it.next();
				Resource subject = s.getSubject();
				RDFNode object = s.getObject();
				this.hashnames.add(subject.getURI());
				if (object.isResource()) {
					Resource obj = object.asResource();
					if(obj.getURI().startsWith("http://dbpedia.org/resource/")) {
						this.hashnames.add(obj.getURI());
					}
				}
			}
			
			// Generate int ids
			this.hashinverted = new HashMap<Integer, String>();
			this.hash = new HashMap<String, Integer>();
			int counter = 0;
			for(String s : hashnames) {
				if(counter % 10000==0) {
					System.out.println(s);
					System.out.println("ID counter: "+counter);
				}
				hashinverted.put(counter, s);
				hash.put(s, counter);
				counter++;
			}
		}
		
		private void parseFacts() {
			StmtIterator it = infoboxes.listStatements();
			int counter = 0;
			while (it.hasNext()) {
				if (counter % 10000 == 0) {
					System.out.println(counter);
				}
				Statement s = it.next();
				Resource subject = s.getSubject();
				Property pra = s.getPredicate();
				RDFNode object = s.getObject();
				if (object.isResource()) {
					Resource obj = object.asResource();
					if (pra.isResource()
							&& obj.getURI().startsWith(
									"http://dbpedia.org/resource/")) {
						writer.println(hash.get(subject.getURI())+"\t"+hash.get(obj.getURI()));
						if (counter % 10000 == 0) {
							System.out.println(subject.getURI() + "    "+obj.getURI());
						}
						writer.flush();
					}
				}
				counter++;
			}

			it = mapping.listStatements();
			counter = 0;
			while (it.hasNext()) {
				if (counter % 10000 == 0) {
					System.out.println(counter);
				}
				Statement s = it.next();
				Resource subject = s.getSubject();
				Property pra = s.getPredicate();
				RDFNode object = s.getObject();
				if (object.isResource()) {
					Resource obj = object.asResource();
					if (pra.isResource()
							&& obj.getURI().startsWith(
									"http://dbpedia.org/resource/")) {
						writer.println(hash.get(subject.getURI())+"\t"+hash.get(obj.getURI()));
						writer.flush();
					}
				}
				counter++;
			}
		}
		
		public void outputIDUriMapping() {
			try {
				PrintWriter writer = new PrintWriter(INVERTEDLST);
				for(Map.Entry<Integer,String> entry : hashinverted.entrySet()) {
					writer.println(entry.getKey() + "\t"+entry.getValue());
				}
				writer.flush();
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		public static void main(String[] args) {
			CreateDBpediaEdgeList model = new CreateDBpediaEdgeList();
			model.createDBpediaGraph();
			model.outputIDUriMapping();
		}

	}
