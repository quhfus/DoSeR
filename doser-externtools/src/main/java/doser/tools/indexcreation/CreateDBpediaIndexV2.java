package doser.tools.indexcreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
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
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import doser.lucene.analysis.DoserIDAnalyzer;

public class CreateDBpediaIndexV2 {

	public static final String OLDINDEX = "/mnt/ssd1/disambiguation/MMapLuceneIndexStandard/";
	public static final String NEWINDEX = "/home/zwicklbauer/NewIndexTryout";

	public static final String ENTITYLIST = "/home/zwicklbauer/WikipediaEntities/entityList_Default_HoffartNew.dat";

	public static final String MAPPINGPROPERTIES = "/home/zwicklbauer/HDTGeneration/mappingbased_properties_cleaned_en.nt";
	public static final String PERSONDATAHDT = "/mnt/ssd1/disambiguation/HDT/persondata_en.hdt";
	public static final String INFOBOXPROPERTIES = "/home/zwicklbauer/HDTGeneration/infobox_properties_en.nt";
	public static final String DISAMBIGUATIONWIKILINKS = "/home/zwicklbauer/HDTGeneration/disambiguations_en.nt";
	public static final String PATTYWIKIPATTERN = "/home/zwicklbauer/Patty/patty-dataset-WikiTypes/wikipedia-patterns.txt";
	public static final String PATTYWIKIINSTANCE = "/home/zwicklbauer/Patty/patty-dataset-WikiTypes/wikipedia-instances.txt";
	public static final String PATTYFREEBASEPATTERN = "/home/zwicklbauer/Patty/patty-dataset-freebase/wikipedia-patterns.txt";
	public static final String PATTYFREEBASEINSTANCE = "/home/zwicklbauer/Patty/patty-dataset-freebase/wikipedia-instances.txt";

	public static final String EVIDENCEFILE = "/home/zwicklbauer/word2vec/evidences.dat";

	public static final String WEBOCCURRENCESDIRECTORY = "/home/zwicklbauer/WikipediaEntities/EntitiesWebContext/";

	public static final String LINKTEXT = "/home/zwicklbauer/WikipediaEntities/enwiki-latest/linktext";
	public static final String ENTITIES = "/home/zwicklbauer/WikipediaEntities/entities_StandardParse_threshold12";
	public static final String REDIRECTS = "/home/zwicklbauer/WikipediaEntities/enwiki-latest/redirects";

	public static final String ARTICLECATEGORIES = "/home/zwicklbauer/HDTGeneration/article_categories_en.nt";
	public static final String LABELHDT = "/home/zwicklbauer/WikipediaIndexGeneration/rdffiles/labels_en.hdt";
	public static final String SHORTDESCHDT = "/home/zwicklbauer/WikipediaIndexGeneration/rdffiles/short_abstracts_en.hdt";
	public static final String LONGDESCHDT = "/home/zwicklbauer/WikipediaIndexGeneration/rdffiles/long_abstracts_en.hdt";
	public static final String INSTANCEMAPPINGTYPES = "/mnt/ssd1/disambiguation/HDT/instance_types_en.hdt";
	public static final String INSTANCEMAPPINGTYPES_NT = "/mnt/ssd1/disambiguation/HDT/instance_types_en.nt";
	public static final String SKOSBROADER = "/home/zwicklbauer/HDTGeneration/skos_categories_en.nt";

	public static final String EXTERNSFDIRECTORY = "/home/zwicklbauer/SurfaceForms/";

	private HashMap<String, HashSet<String>> LABELS;

	private HashSet<String> entities;

	private HashMap<String, LinkedList<String>> relationmap;
	private HashMap<String, LinkedList<String>> pattymap;
	private HashMap<String, LinkedList<String>> pattyfreebasemap;
	private HashMap<String, String> evidences;
	private HashSet<String> teams;

	private HashMap<String, HashSet<String>> UNIQUELABELSTRINGS;
	private HashMap<String, HashMap<String, Integer>> OCCURRENCES;
	private HashMap<String, Integer> DBPEDIAGRAPHINLINKS;

	private int counter;

	private Model labelmodel;
	private Model shortdescmodel;
	private Model longdescmodel;
	private Model persondata;
	private Model instancemappingtypes;

	public CreateDBpediaIndexV2() {
		super();
		this.relationmap = new HashMap<String, LinkedList<String>>();
		this.pattymap = new HashMap<String, LinkedList<String>>();
		this.pattyfreebasemap = new HashMap<String, LinkedList<String>>();

		this.OCCURRENCES = new HashMap<String, HashMap<String, Integer>>();

		this.LABELS = new HashMap<String, HashSet<String>>();
		this.UNIQUELABELSTRINGS = new HashMap<String, HashSet<String>>();
		this.DBPEDIAGRAPHINLINKS = new HashMap<String, Integer>();
		this.evidences = new HashMap<String, String>();
		this.teams = new HashSet<String>();

		this.entities = new HashSet<String>();

		this.counter = 0;

		HDT labelhdt;
		HDT shortdeschdt;
		HDT longdeschdt;
		HDT mappingbasedproperties;
		HDT instancemappingtypeshdt;
		try {
			labelhdt = HDTManager.mapIndexedHDT(LABELHDT, null);
			shortdeschdt = HDTManager.mapIndexedHDT(SHORTDESCHDT, null);
			longdeschdt = HDTManager.mapIndexedHDT(LONGDESCHDT, null);
			mappingbasedproperties = HDTManager.mapIndexedHDT(PERSONDATAHDT,
					null);
			instancemappingtypeshdt = HDTManager.mapIndexedHDT(
					INSTANCEMAPPINGTYPES, null);
			final HDTGraph labelhdtgraph = new HDTGraph(labelhdt);
			final HDTGraph shortdeschdtgraph = new HDTGraph(shortdeschdt);
			final HDTGraph longdeschdtgraph = new HDTGraph(longdeschdt);
			final HDTGraph instancepersondata = new HDTGraph(
					mappingbasedproperties);
			final HDTGraph instancemappingtypesgraph = new HDTGraph(
					instancemappingtypeshdt);
			this.labelmodel = ModelFactory.createModelForGraph(labelhdtgraph);
			this.shortdescmodel = ModelFactory
					.createModelForGraph(shortdeschdtgraph);
			this.longdescmodel = ModelFactory
					.createModelForGraph(longdeschdtgraph);
			this.persondata = ModelFactory
					.createModelForGraph(instancepersondata);
			this.instancemappingtypes = ModelFactory
					.createModelForGraph(instancemappingtypesgraph);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadEvidences() {
		File file = new File(EVIDENCEFILE);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String splitter[] = line.split("\\t");
				this.evidences.put(splitter[0], splitter[1]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void createDBpediaPriors() {
		UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(
				DefaultEdge.class);
		Model m = ModelFactory.createDefaultModel();
		m.read(INFOBOXPROPERTIES);
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
					if (!subject.getURI().equalsIgnoreCase(obj.getURI())) {
						graph.addVertex(subject.getURI());
						graph.addVertex(obj.getURI());
						graph.addEdge(subject.getURI(), obj.getURI());
					}
				}
			}
			counter++;
		}
		m = ModelFactory.createDefaultModel();
		m.read(MAPPINGPROPERTIES);
		it = m.listStatements();
		counter = 0;
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
					if (!subject.getURI().equalsIgnoreCase(obj.getURI())) {
						graph.addVertex(subject.getURI());
						graph.addVertex(obj.getURI());
						graph.addEdge(subject.getURI(), obj.getURI());
					}
				}
			}
		}

		m = ModelFactory.createDefaultModel();
		m.read(SKOSBROADER);
		it = m.listStatements();
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
					if (!subject.getURI().equalsIgnoreCase(obj.getURI())) {
						graph.addVertex(subject.getURI());
						graph.addVertex(obj.getURI());
						graph.addEdge(subject.getURI(), obj.getURI());
					}
				}
			}
		}

		m = ModelFactory.createDefaultModel();
		m.read(ARTICLECATEGORIES);
		it = m.listStatements();
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
					if (!subject.getURI().equalsIgnoreCase(obj.getURI())) {
						graph.addVertex(subject.getURI());
						graph.addVertex(obj.getURI());
						graph.addEdge(subject.getURI(), obj.getURI());
					}
				}
			}
		}

		Set<String> set = graph.vertexSet();
		for (String s : set) {
			DBPEDIAGRAPHINLINKS.put(s, graph.degreeOf(s));
		}
	}

	public void fillPropertiesIndex() {
		Model m = ModelFactory.createDefaultModel();

		m.read(INFOBOXPROPERTIES);

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
					if (!relationmap.containsKey(subject.getURI())) {
						LinkedList<String> list = new LinkedList<String>();
						relationmap.put(subject.getURI(), list);
					}
					LinkedList<String> l = relationmap.get(subject.getURI());
					l.add(pra.getURI().replaceAll(
							"http://dbpedia.org/property/", "dbpediaOnt/")
							+ ":::"
							+ obj.getURI().replaceAll(
									"http://dbpedia.org/resource/",
									"dbpediaRes/"));

				}
			}
		}
	}

	public void fillRelationsIndex() {
		Model m = ModelFactory.createDefaultModel();

		m.read(MAPPINGPROPERTIES);

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
					if (!relationmap.containsKey(subject.getURI())) {
						LinkedList<String> list = new LinkedList<String>();
						relationmap.put(subject.getURI(), list);
					}
					LinkedList<String> l = relationmap.get(subject.getURI());
					l.add(pra.getURI().replaceAll(
							"http://dbpedia.org/ontology/", "dbpediaOnt/")
							+ ":::"
							+ obj.getURI().replaceAll(
									"http://dbpedia.org/resource/",
									"dbpediaRes/"));

				}
			}
		}
	}

	public void fillPattyRelationIndex(String pattern, String instance) {
		File patternFile = new File(pattern);
		HashMap<Integer, String> patternMap = new HashMap<Integer, String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(patternFile));
			reader.readLine();
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] splitter = line.split("\\t");
				Integer i = null;
				try {
					i = new Integer(Integer.valueOf(splitter[0]));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				patternMap.put(i, splitter[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Read Instancefile - either WikiTypes or Freebase Types
		File instanceFile = new File(instance);
		reader = null;
		try {
			reader = new BufferedReader(new FileReader(instanceFile));
			reader.readLine();
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] splitter = line.split("\\t");

				Integer j = null;
				try {
					j = new Integer(Integer.valueOf(splitter[0]));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				String subject = WikiPediaUriConverter
						.createConformDBpediaURI(splitter[1]);
				String object = WikiPediaUriConverter.createConformDBpediaURI(
						splitter[2]).replaceAll("http://dbpedia.org/resource/",
						"");

				if (!pattymap.containsKey(subject)) {
					LinkedList<String> list = new LinkedList<String>();
					pattymap.put(subject, list);
				}
				LinkedList<String> l = pattymap.get(subject);
				l.add("patty/" + patternMap.get(j) + ":::" + "dbpediaRes/"
						+ object);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void fillPattyFreebaseRelationIndex(String pattern, String instance) {
		File patternFile = new File(pattern);
		HashMap<Integer, String> patternMap = new HashMap<Integer, String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(patternFile));
			reader.readLine();
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] splitter = line.split("\\t");
				Integer i = null;
				try {
					i = new Integer(Integer.valueOf(splitter[0]));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				patternMap.put(i, splitter[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Read Instancefile - either WikiTypes or Freebase Types
		File instanceFile = new File(instance);
		reader = null;
		try {
			reader = new BufferedReader(new FileReader(instanceFile));
			reader.readLine();
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] splitter = line.split("\\t");

				Integer j = null;
				try {
					j = new Integer(Integer.valueOf(splitter[0]));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				String subject = WikiPediaUriConverter
						.createConformDBpediaURI(splitter[1]);
				String object = WikiPediaUriConverter.createConformDBpediaURI(
						splitter[2]).replaceAll("http://dbpedia.org/resource/",
						"");

				if (!pattyfreebasemap.containsKey(subject)) {
					LinkedList<String> list = new LinkedList<String>();
					pattyfreebasemap.put(subject, list);
				}
				LinkedList<String> l = pattyfreebasemap.get(subject);
				l.add("patty/" + patternMap.get(j) + ":::" + "dbpediaRes/"
						+ object);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void workLinkText() {
		File f = new File(LINKTEXT);

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String split[] = line.split("\\t");
				if (split.length > 2) {
					for (int i = 2; i < split.length; ++i) {
						String ent = split[i];
						String[] occ = ent.split(":");

						// Bugfix: Fix Wrong splitting
						StringBuilder builder = new StringBuilder();
						for (int j = 0; j < occ.length - 1; j++) {
							builder.append(occ[j] + ":");
						}
						String nr = occ[occ.length - 1];
						String entity = builder.toString();
						entity = entity.substring(0, entity.length() - 1);

						String uri = WikiPediaUriConverter
								.createConformDBpediaURI(entity);
						if (!uri.contains("(Disambiguation)")) {
							// UniqueLabelStrings
							if (UNIQUELABELSTRINGS.containsKey(uri)) {
								HashSet<String> set = UNIQUELABELSTRINGS
										.get(uri);
								set.add(split[0].toLowerCase());
								addUniqueCandidateWithoutSpecialChars(set,
										split[0]);
							} else {
								HashSet<String> set = new HashSet<String>();
								set.add(split[0].toLowerCase());
								addUniqueCandidateWithoutSpecialChars(set,
										split[0]);
								UNIQUELABELSTRINGS.put(uri, set);
							}

							// Occurrences
							if (!OCCURRENCES.containsKey(uri)) {
								HashMap<String, Integer> map = new HashMap<String, Integer>();
								OCCURRENCES.put(uri, map);
							}
							addOccurrence(uri, split[0].toLowerCase(),
									Integer.valueOf(nr));
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void getUniqueLabelsFromOldIndex() {

		File oldIndexFile = new File(OLDINDEX);
		IndexReader readerOldIndex = null;
		try {
			final Directory oldDir = FSDirectory.open(oldIndexFile);
			readerOldIndex = DirectoryReader.open(oldDir);
			for (int j = 0; j < readerOldIndex.maxDoc(); ++j) {
				Document oldDoc = readerOldIndex.document(j);
				String[] oldUniqueLabels = oldDoc
						.getValues("UniqueLabelString");
				String oldResource = oldDoc.get("Mainlink");

				// Transform old to new Namespace
				oldResource = oldResource.replaceAll(
						"http://dbpedia.org/resource/", "");
				oldResource = URLDecoder.decode(oldResource, "UTF-8");
				oldResource = WikiPediaUriConverter
						.createConformDBpediaURI(oldResource);

				// Old Unique Labels
				if (UNIQUELABELSTRINGS.containsKey(oldResource)) {
					HashSet<String> set = UNIQUELABELSTRINGS.get(oldResource);
					if (oldUniqueLabels != null && oldUniqueLabels.length > 0) {
						for (int k = 0; k < oldUniqueLabels.length; ++k) {
							set.add(oldUniqueLabels[k].toLowerCase());
							addUniqueCandidateWithoutSpecialChars(set,
									oldUniqueLabels[k]);
						}
					}
				} else {
					HashSet<String> set = new HashSet<String>();
					if (oldUniqueLabels != null && oldUniqueLabels.length > 0) {
						for (int k = 0; k < oldUniqueLabels.length; ++k) {
							set.add(oldUniqueLabels[k].toLowerCase());
							addUniqueCandidateWithoutSpecialChars(set,
									oldUniqueLabels[k]);
						}
					}
					UNIQUELABELSTRINGS.put(oldResource, set);
				}

				if (!OCCURRENCES.containsKey(oldResource)) {
					OCCURRENCES
							.put(oldResource, new HashMap<String, Integer>());
				}
				String oldOccurrences = oldDoc.get("Occurrences");
				if ((oldOccurrences != null)
						&& !oldOccurrences.equalsIgnoreCase("")) {
					final String[] splitter = oldOccurrences.split(";;;");
					for (final String element : splitter) {
						final String[] splitter1 = element.split(":::");
						int check = 1;
						try {
							check = Integer.valueOf(splitter1[1]);
						} catch (final NumberFormatException e) {
							Logger.getRootLogger()
									.error("Warning NumberFormatException while Initialization: ");
						}
						addOccurrence(oldResource, splitter1[0], check);
					}
				}

			}
			readerOldIndex.close();
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
		}
	}

	public void workEntities() {
		File f = new File(ENTITIES);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String split[] = line.split("\\t");
				if (split.length > 2) {
					for (int i = 2; i < split.length; ++i) {
						String ent = split[i];
						String[] occ = ent.split(":");
						String uri = WikiPediaUriConverter
								.createConformDBpediaURI(occ[0]);

						// Synonyms
						if (LABELS.containsKey(uri)) {
							HashSet<String> set = LABELS.get(uri);
							// Add Label to UniqueLabel
							addLabelToUniqueLabel(uri, split[0]);
							set.add(split[0].toLowerCase());
						} else {
							HashSet<String> set = new HashSet<String>();
							// set.add(occ[0].toLowerCase());
							set.add(split[0].toLowerCase());
							// Add Label to UniqueLabel
							addLabelToUniqueLabel(uri, split[0]);
							LABELS.put(uri, set);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void addLabelToUniqueLabel(String entity, String label) {
		if (UNIQUELABELSTRINGS.containsKey(entity)) {
			HashSet<String> set = UNIQUELABELSTRINGS.get(entity);
			set.add(label.toLowerCase());
			addUniqueCandidateWithoutSpecialChars(set, label);
		}
	}

	public void workRedirects() {
		File f = new File(REDIRECTS);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String split[] = line.split("\\t");
				// Bug Fix of wrong redirects
				if (split.length < 3) {
					String uri = WikiPediaUriConverter
							.createConformDBpediaURI(split[1]);
					if (LABELS.containsKey(uri)) {
						HashSet<String> set = LABELS.get(uri);
						set.add(split[0].toLowerCase());
						// Add Label to UniqueLabel
						addLabelToUniqueLabel(uri, split[0]);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(split[0].toLowerCase());
						// Add Label to UniqueLabel
						addLabelToUniqueLabel(uri, split[0]);
						LABELS.put(uri, set);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void createNewIndex() {
		File newIndexFile = new File(NEWINDEX);
		try {
			final Directory newDir = FSDirectory.open(newIndexFile);

			Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
			analyzerPerField.put("Label", new DoserIDAnalyzer());
			analyzerPerField.put("PattyRelations", new DoserIDAnalyzer());
			analyzerPerField.put("PattyFreebaseRelations",
					new DoserIDAnalyzer());
			analyzerPerField.put("Relations", new DoserIDAnalyzer());
			analyzerPerField.put("Occurrences", new DoserIDAnalyzer());
			analyzerPerField.put("Type", new DoserIDAnalyzer());
			analyzerPerField.put("StringLabel", new DoserIDAnalyzer());

			PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(
					new StandardAnalyzer(), analyzerPerField);

			final IndexWriterConfig config = new IndexWriterConfig(
					Version.LATEST, aWrapper);
			final IndexWriter newIndexWriter = new IndexWriter(newDir, config);

			for (String uri : entities) {

				Document doc = new Document();
				// Add ID
				doc.add(new StringField("ID", "DBpedia_"
						+ String.valueOf(counter), Store.YES));
				counter++;

				// Add Mainlink
				doc.add(new StringField("Mainlink", uri, Store.YES));

				// Add Labels
				List<String> origLabels = getDbPediaLabel(uri);
				HashSet<String> labelset = LABELS.get(uri);
				if (labelset == null) {
					labelset = new HashSet<String>();
				}
				for (String s : origLabels) {
					labelset.add(s);
				}

				for (String s : labelset) {
					doc.add(new TextField("Label", s.toLowerCase(), Store.YES));
					doc.add(new StringField("StringLabel", s.toLowerCase(),
							Store.YES));
				}

				// Add ShortDescriptions
				String shortDescription = getDbPediaShortDescription(uri);
				doc.add(new TextField("ShortDescription", shortDescription,
						Store.YES));

				// Add longDescriptions
				String longDescription = getDbPediaLongDescription(uri);
				doc.add(new TextField("LongDescription", longDescription,
						Store.YES));

				// Add Type
				String type = filterStandardDomain(getRDFTypesFromEntity(uri));
				doc.add(new StringField("Type", type, Store.YES));

				// Add Occurrences
				HashMap<String, Integer> occs = OCCURRENCES.get(uri);
				StringBuilder builder = new StringBuilder();
				if (occs != null) {
					for (Map.Entry<String, Integer> entry : occs.entrySet()) {
						String key = entry.getKey();
						int value = entry.getValue();
						builder.append(key + ":::" + String.valueOf(value)
								+ ";;;");
					}
				}
				String occurrenceString = builder.toString();
				if (occurrenceString.length() > 0) {
					occurrenceString = occurrenceString.substring(0,
							occurrenceString.length() - 3);
				}
				doc.add(new StringField("Occurrences", occurrenceString,
						Store.YES));

				// UniqueLabelStrings
				HashSet<String> keys = UNIQUELABELSTRINGS.get(uri);
				// Füge noch die Sportsteams hinzu
				if (keys == null) {
					keys = new HashSet<String>();
				}
				if (teams.contains(uri)) {
					keys.addAll(extractSportsTeamNames(labelset));
				}
				// Füge noch weitere Personennamen hinzu
				keys.addAll(addAdditionalPersonNameOccurrences(uri));
				for (String s : origLabels) {
					keys.add(s.toLowerCase());
					addUniqueCandidateWithoutSpecialChars(keys, s);
				}

				for (String s : keys) {
					doc.add(new StringField("UniqueLabel", s, Store.YES));
				}

				// Add DBPedia Facts
				if (relationmap.containsKey(uri)) {
					LinkedList<String> l = relationmap.get(uri);
					builder = new StringBuilder();
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
				} else {
					doc.add(new TextField("Relations", "", Store.YES));
				}

				// Add PattyFacts
				if (pattymap.containsKey(uri)) {
					LinkedList<String> l = pattymap.get(uri);
					builder = new StringBuilder();
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
					doc.add(new TextField("PattyRelations", s, Store.YES));
				} else {
					doc.add(new TextField("PattyRelations", "", Store.YES));
				}

				// Add PattyFreebaseFacts
				if (pattyfreebasemap.containsKey(uri)) {
					LinkedList<String> l = pattyfreebasemap.get(uri);
					builder = new StringBuilder();
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
					doc.add(new TextField("PattyFreebaseRelations", s,
							Store.YES));
				} else {
					doc.add(new TextField("PattyFreebaseRelations", "",
							Store.YES));
				}

				// Add DBpediaPriors
				if (DBPEDIAGRAPHINLINKS.containsKey(uri)) {
					doc.add(new IntField("DbpediaVertexDegree",
							DBPEDIAGRAPHINLINKS.get(uri), Field.Store.YES));
				}

				// Add Evidences
				// if(evidences.containsKey(uri)) {
				// Set<String> ev = extractEvidences(evidences.get(uri));
				// for(String s : ev) {
				// doc.add(new StringField("Evidence", s, Field.Store.YES));
				// }
				// }

				// Write Document To Index
				if (doc.get("Label") != null
						&& !doc.get("Label").equalsIgnoreCase("")) {
					newIndexWriter.addDocument(doc);
				}

			}

			newIndexWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> getDbPediaLabel(final String uri)
			throws QueryException, QueryParseException {
		final List<String> labellist = new LinkedList<String>();
		try {
			final String query = "SELECT ?label WHERE{ <"
					+ uri
					+ "> <http://www.w3.org/2000/01/rdf-schema#label> ?label. }";
			ResultSet results = null;
			QueryExecution qexec = null;

			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			qexec = QueryExecutionFactory.create(cquery, this.labelmodel);
			results = qexec.execSelect();

			if (results != null) {
				while (results.hasNext()) {
					final QuerySolution sol = results.nextSolution();
					final String label = sol.getLiteral("label")
							.getLexicalForm();
					labellist.add(label);
				}
				qexec.close();
			}
		} catch (QueryParseException e) {
			Logger.getRootLogger().info("Query parse Exception");
		}
		return labellist;
	}

	public String getDbPediaShortDescription(final String uri)
			throws QueryException, QueryParseException {
		String labellist = "";
		try {
			final String query = "SELECT ?comment WHERE{ <"
					+ uri
					+ "> <http://www.w3.org/2000/01/rdf-schema#comment> ?comment. }";
			ResultSet results = null;
			QueryExecution qexec = null;

			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			qexec = QueryExecutionFactory.create(cquery, this.shortdescmodel);
			results = qexec.execSelect();

			if (results != null) {
				while (results.hasNext()) {
					final QuerySolution sol = results.nextSolution();
					String desc = sol.getLiteral("comment").getLexicalForm();
					labellist = desc;
				}
				qexec.close();
			}
		} catch (QueryParseException e) {
			Logger.getRootLogger().info("Query parse Exception");
		}
		return labellist;
	}

	public String getDbPediaLongDescription(final String uri)
			throws QueryException, QueryParseException {
		String labellist = "";
		try {
			final String query = "SELECT ?comment WHERE{ <" + uri
					+ "> <http://dbpedia.org/ontology/abstract> ?comment. }";
			ResultSet results = null;
			QueryExecution qexec = null;

			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			qexec = QueryExecutionFactory.create(cquery, this.longdescmodel);
			results = qexec.execSelect();

			if (results != null) {
				while (results.hasNext()) {
					final QuerySolution sol = results.nextSolution();
					final String desc = sol.getLiteral("comment")
							.getLexicalForm();
					labellist = desc;
				}
				qexec.close();
			}
		} catch (QueryParseException e) {
			Logger.getRootLogger().info("Query parse Exception");
		}
		return labellist;
	}

	public void readEntities() {
		File f = new File(ENTITYLIST);
		try {
			String line = null;
			BufferedReader reader = new BufferedReader(new FileReader(f));
			while ((line = reader.readLine()) != null) {
				String uri = URLDecoder.decode(line, "UTF-8").replaceAll(
						"http://dbpedia.org/resource/", "");
				entities.add(WikiPediaUriConverter.createConformDBpediaURI(uri));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void insertWebOccurrences() {
		File dir = new File(WEBOCCURRENCESDIRECTORY);
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(
						files[i]));
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("MENTION")) {
						String[] splitter = line.split("\\t");
						String mention = splitter[1];
						String uri = WikiPediaUriConverter
								.createConformDBpediaURI(splitter[3]
										.replaceAll(
												"http://en.wikipedia.org/wiki/",
												""));
						// System.out.println("Mention: "+mention+"  Uri: "+uri);
						if (UNIQUELABELSTRINGS.containsKey(uri)) {
							HashSet<String> set = UNIQUELABELSTRINGS.get(uri);
							set.add(mention.toLowerCase());
						} else {
							HashSet<String> set = new HashSet<String>();
							set.add(mention.toLowerCase());
							UNIQUELABELSTRINGS.put(uri, set);
						}
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void addOccurrence(String uri, String sf, int amount) {
		HashMap<String, Integer> occ = OCCURRENCES.get(uri);
		if (occ.containsKey(sf)) {
			int i = occ.get(sf);
			i += amount;
			occ.put(sf, i);
		} else {
			occ.put(sf, amount);
		}

		if (UNIQUELABELSTRINGS.containsKey(uri)) {
			HashSet<String> set = UNIQUELABELSTRINGS.get(uri);
			set.add(sf.toLowerCase());
			addUniqueCandidateWithoutSpecialChars(set, sf);
		} else {
			HashSet<String> set = new HashSet<String>();
			set.add(sf.toLowerCase());
			addUniqueCandidateWithoutSpecialChars(set, sf);
			UNIQUELABELSTRINGS.put(uri, set);
		}
	}

	private void addUniqueCandidateWithoutSpecialChars(HashSet<String> set,
			String sf) {
		String newsf = sf.toLowerCase().replaceAll("[^a-zA-Z ]", "");
		if (newsf.length() > 2) {
			set.add(newsf);
		}
	}

	public HashSet<String> addAdditionalPersonNameOccurrences(String res) {
		HashSet<String> names = new HashSet<String>();
		try {
			final String query = "SELECT ?surname WHERE{ <" + res
					+ "> <http://xmlns.com/foaf/0.1/surname> ?surname. }";
			ResultSet results = null;
			QueryExecution qexec = null;

			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			qexec = QueryExecutionFactory.create(cquery, this.persondata);
			results = qexec.execSelect();

			if (results != null) {
				while (results.hasNext()) {
					final QuerySolution sol = results.nextSolution();
					final String surname = sol.getLiteral("surname")
							.getLexicalForm();
					names.add(surname.toLowerCase());
				}
				qexec.close();
			}
		} catch (QueryParseException e) {
			Logger.getRootLogger().info("Query parse Exception");
		}
		// Constraint dass es eine Person ist
		if (names.size() > 0) {
			String rdfslabel = "";
			try {
				final String query = "SELECT ?label WHERE{ <"
						+ res
						+ "> <http://www.w3.org/2000/01/rdf-schema#label> ?label. }";
				ResultSet results = null;
				QueryExecution qexec = null;

				final com.hp.hpl.jena.query.Query cquery = QueryFactory
						.create(query);
				qexec = QueryExecutionFactory.create(cquery, this.labelmodel);
				results = qexec.execSelect();

				if (results != null) {
					while (results.hasNext()) {
						final QuerySolution sol = results.nextSolution();
						final String label = sol.getLiteral("label")
								.getLexicalForm();
						rdfslabel = label;
					}
					qexec.close();
				}
			} catch (QueryParseException e) {
				Logger.getRootLogger().info("Query parse Exception");
			}

			String splitter[] = rdfslabel.split(" ");
			if (splitter.length > 2) {
				// Generiere verschiedene Namensmöglichkeiten
				for (int i = 0; i < splitter.length; i++) {
					for (int j = 0; j < splitter.length; j++) {
						if (!splitter[i].equalsIgnoreCase(splitter[j])) {
							names.add((splitter[i] + " " + splitter[j])
									.toLowerCase());
						}
					}
				}
			}
		}
		return names;
	}

	public void readWikiPageDisambiguation() {
		Model m = ModelFactory.createDefaultModel();

		m.read(DISAMBIGUATIONWIKILINKS);

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
					String label = subject.getURI().replaceAll(
							"http://dbpedia.org/resource/", "");
					label = label.replaceAll("\\_\\(disambiguation\\)", "")
							.toLowerCase();
					if (UNIQUELABELSTRINGS.containsKey(obj.getURI())) {
						HashSet<String> set = UNIQUELABELSTRINGS.get(obj
								.getURI());
						set.add(label);
						addUniqueCandidateWithoutSpecialChars(set, label);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(label);
						addUniqueCandidateWithoutSpecialChars(set, label);
						UNIQUELABELSTRINGS.put(obj.getURI(), set);
					}
				}
			}
		}
	}

	public void sportsTeamsSurfaceForms() {
		Model m = ModelFactory.createDefaultModel();

		m.read(INSTANCEMAPPINGTYPES_NT);

		StmtIterator it = m.listStatements();

		while (it.hasNext()) {
			Statement s = it.next();
			Resource subject = s.getSubject();
			RDFNode object = s.getObject();

			if (object.isResource()) {
				Resource obj = object.asResource();
				if (obj.getURI().equalsIgnoreCase(
						"http://dbpedia.org/ontology/SportsTeam")) {
					teams.add(subject.getURI());
				}
			}
		}
	}

	private HashSet<String> extractSportsTeamNames(HashSet<String> set) {
		HashSet<String> newStringSet = new HashSet<String>();
		for (String s : set) {
			String splitter[] = s.split(" ");
			for (int i = 0; i < splitter.length; i++) {
				if (splitter[i].equalsIgnoreCase(splitter[i].replaceAll(
						"[^a-zA-Z ]", ""))) {
					if (splitter[i].toLowerCase().length() > 3) {
						newStringSet.add(splitter[i].toLowerCase());
					}
				}
			}
		}
		return newStringSet;
	}

	private String filterStandardDomain(Set<String> set) {
		String res = "Misc";
		for (String s : set) {
			if (s.equalsIgnoreCase("http://dbpedia.org/ontology/Person")) {
				res = "Person";
				break;
			} else if (s
					.equalsIgnoreCase("http://dbpedia.org/ontology/Organisation")) {
				res = "Organisation";
				break;
			} else if (s
					.equalsIgnoreCase("http://www.ontologydesignpatterns.org/ont/d0.owl#Location")) {
				res = "Location";
				break;
			}
		}
		return res;
	}

	public Set<String> getRDFTypesFromEntity(final String entityUri) {
		Set<String> set = new HashSet<String>();
		final String query = "SELECT ?types WHERE{ <"
				+ entityUri
				+ "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?types. }";
		ResultSet results = null;
		QueryExecution qexec = null;
		try {
			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			qexec = QueryExecutionFactory.create(cquery, instancemappingtypes);
			results = qexec.execSelect();
		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		} finally {
			if (results != null) {
				while (results.hasNext()) {
					final QuerySolution sol = results.nextSolution();
					final String type = sol.getResource("types").toString();
					set.add(type);
				}
			}
		}
		return set;
	}

	private Set<String> extractEvidences(String evidences) {
		Set<String> set = new HashSet<String>();
		String splitter[] = evidences.split(";");
		for (int i = 0; i < splitter.length; i++) {
			String evidence = splitter[i].replaceAll("(", "").replaceAll(")",
					"");
			String split2[] = evidence.split(",");
			set.add(split2[0]);
		}
		return set;
	}

	public void addSomeAbbreviations() {
		for (Map.Entry<String, HashSet<String>> entry : this.UNIQUELABELSTRINGS
				.entrySet()) {
			String url = entry.getKey();
			HashSet<String> occs = entry.getValue();
			String type = filterStandardDomain(getRDFTypesFromEntity(url));
			if (type.equals("Location")) {
				String tempuri = url.replaceAll("http://dbpedia.org/resource/",
						"").toLowerCase();
				tempuri = tempuri.replaceAll("_", " ");
				StringBuilder builder = new StringBuilder();
				String splitter[] = tempuri.split(" ");
				if (splitter.length > 1) {
					for (int i = 0; i < splitter.length; i++) {
						builder.append(splitter[i].substring(0, 1));
						builder.append(".");
					}
					occs.add(builder.toString());
				}
			}
		}
	}

	public static void main(String[] args) {
		CreateDBpediaIndexV2 index = new CreateDBpediaIndexV2();
		System.out.println("Step-1: Load Evidences");
		// index.loadEvidences();
		System.out.println("Step0: Create DBpediaPriors");
		index.createDBpediaPriors();
		System.out.println("Step1: Read Sportsteams");
		index.sportsTeamsSurfaceForms();
		System.out.println("Step2: Read Wikipedia Disambiguation Links");
		index.readWikiPageDisambiguation();
		System.out.println("Step3: Read Entity List");
		index.readEntities();
		System.out.println("Step4: DBPediaFacts");
		index.fillRelationsIndex();
		System.out.println("Step5: DBPediaProperties");
		index.fillPropertiesIndex();
		System.out.println("Step6: PattyFacts");
		index.fillPattyRelationIndex(PATTYWIKIPATTERN, PATTYWIKIINSTANCE);
		System.out.println("Step7: PattyFreebaseFacts");
		index.fillPattyFreebaseRelationIndex(PATTYFREEBASEPATTERN,
				PATTYFREEBASEINSTANCE);
		System.out.println("Step8: WorkLinkText");
		index.workLinkText();
		System.out.println("Step9: ReadOldIndex");
		index.getUniqueLabelsFromOldIndex();
		System.out.println("Step10: WorkEntities");
		index.workEntities();
		System.out.println("Step11: WorkRedirects");
		index.workRedirects();
		System.out.println("Step12: WebOccurrences");
		index.insertWebOccurrences();
		System.out.println("Step13: CreateSomeAbbreviations");
		index.addSomeAbbreviations();
		System.out.println("Step14: CreateIndex");
		index.createNewIndex();
	}

}
