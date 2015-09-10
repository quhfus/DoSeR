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

import org.apache.log4j.Logger;
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
import doser.lucene.analysis.DoserStandardAnalyzer;

public class CreateDBPediaIndex {

	public static final String LABELHDT = "/home/zwicklbauer/WikipediaIndexGeneration/rdffiles/labels_en.hdt";
	public static final String SHORTDESCHDT = "/home/zwicklbauer/WikipediaIndexGeneration/rdffiles/short_abstracts_en.hdt";
	public static final String LONGDESCHDT = "/home/zwicklbauer/WikipediaIndexGeneration/rdffiles/long_abstracts_en.hdt";

	public static final String MAPPINGPROPERTIES = "/home/zwicklbauer/HDTGeneration/mappingbased_properties_cleaned_en.nt";
	public static final String PATTYWIKIPATTERN = "/home/zwicklbauer/Patty/patty-dataset-WikiTypes/wikipedia-patterns.txt";
	public static final String PATTYWIKIINSTANCE = "/home/zwicklbauer/Patty/patty-dataset-WikiTypes/wikipedia-instances.txt";
	public static final String PATTYFREEBASEPATTERN = "/home/zwicklbauer/Patty/patty-dataset-freebase/wikipedia-patterns.txt";
	public static final String PATTYFREEBASEINSTANCE = "/home/zwicklbauer/Patty/patty-dataset-freebase/wikipedia-instances.txt";

	public static final String LINKTEXT = "/home/zwicklbauer/WikipediaEntities/enwiki-latest/linktext";
	public static final String ENTITIES = "/home/zwicklbauer/WikipediaEntities/entities_StandardParse_threshold12";
	public static final String REDIRECTS = "/home/zwicklbauer/WikipediaEntities/enwiki-latest/redirects";

	public static final String EVIDENCEDIRECTORY = "/mnt/ssd1/evidence/*/";
	public static final String WEBOCCURRENCESDIRECTORY = "/home/zwicklbauer/WikipediaEntities/EntitiesWebContext/";

	public static final String OLDINDEX = "/mnt/ssd1/disambiguation/MMapLuceneIndexStandard/";
	public static final String NEWINDEX = "/home/zwicklbauer/NewIndexTryout";

	private HashMap<String, HashSet<String>> LABELS;
	private HashMap<String, HashSet<String>> UNIQUELABELSTRINGS;
	private HashMap<String, HashMap<String, Integer>> OCCURRENCES;
	// private HashMap<String, String> OCCURRENCES;

	private HashMap<String, LinkedList<String>> relationmap;
	private HashMap<String, LinkedList<String>> pattymap;
	private HashMap<String, LinkedList<String>> pattyfreebasemap;

	private HashMap<String, HashSet<String>> evidencemap;

	private static int evidencecounter = 0;

	private Model labelmodel;
	private Model shortdescmodel;
	private Model longdescmodel;
	private int counter;

	CreateDBPediaIndex() {
		super();
		this.LABELS = new HashMap<String, HashSet<String>>();
		this.UNIQUELABELSTRINGS = new HashMap<String, HashSet<String>>();
		this.OCCURRENCES = new HashMap<String, HashMap<String, Integer>>();
		this.relationmap = new HashMap<String, LinkedList<String>>();
		this.pattymap = new HashMap<String, LinkedList<String>>();
		this.pattyfreebasemap = new HashMap<String, LinkedList<String>>();
		this.evidencemap = new HashMap<String, HashSet<String>>();
		HDT labelhdt;
		HDT shortdeschdt;
		HDT longdeschdt;
		try {
			labelhdt = HDTManager.mapIndexedHDT(LABELHDT, null);
			shortdeschdt = HDTManager.mapIndexedHDT(SHORTDESCHDT, null);
			longdeschdt = HDTManager.mapIndexedHDT(LONGDESCHDT, null);
			final HDTGraph labelhdtgraph = new HDTGraph(labelhdt);
			final HDTGraph shortdeschdtgraph = new HDTGraph(shortdeschdt);
			final HDTGraph longdeschdtgraph = new HDTGraph(longdeschdt);
			this.labelmodel = ModelFactory.createModelForGraph(labelhdtgraph);
			this.shortdescmodel = ModelFactory
					.createModelForGraph(shortdeschdtgraph);
			this.longdescmodel = ModelFactory
					.createModelForGraph(longdeschdtgraph);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.counter = 0;
	}

	/**
	 * Fill Occurrences and UniqueLabelStrings
	 * 
	 * Speichere alle Entity Occurrences und UniqueLabelStrings
	 */
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
							// System.out.println(uri);
							// UniqueLabelStrings
							if (UNIQUELABELSTRINGS.containsKey(uri)) {
								HashSet<String> set = UNIQUELABELSTRINGS
										.get(uri);
								set.add(split[0].toLowerCase());
							} else {
								HashSet<String> set = new HashSet<String>();
								set.add(split[0].toLowerCase());
								UNIQUELABELSTRINGS.put(uri, set);
							}

							// Occurrences
							if(!OCCURRENCES.containsKey(uri)) {
								HashMap<String, Integer> map = new HashMap<String, Integer>();
								OCCURRENCES.put(uri, map);
							}
							addOccurrence(uri, split[0].toLowerCase(), Integer.valueOf(nr));
						}
					}
				}
			}

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

					if (!UNIQUELABELSTRINGS.containsKey(oldResource)) {
						UNIQUELABELSTRINGS.put(oldResource, new HashSet<String>());
					}
					HashSet<String> set = UNIQUELABELSTRINGS.get(oldResource); 
					if (oldUniqueLabels != null
							&& oldUniqueLabels.length > 0) {
						for (int k = 0; k < oldUniqueLabels.length; ++k) {
							set.add(oldUniqueLabels[k].toLowerCase());
						}
					}
					
					if (!OCCURRENCES.containsKey(oldResource)) {
						OCCURRENCES.put(oldResource, new HashMap<String, Integer>());
					}
					String oldOccurrences = oldDoc.get("Occurrences");
					if ((oldOccurrences != null)
							&& !oldOccurrences.equalsIgnoreCase("")) {
						final String[] splitter = oldOccurrences
								.split(";;;");
						for (final String element : splitter) {
							final String[] splitter1 = element
									.split(":::");
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
		// System.out.println(UNIQUELABELSTRINGS.size());
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
			analyzerPerField.put("Label", new DoserStandardAnalyzer());
			analyzerPerField.put("PattyRelations", new DoserIDAnalyzer());
			analyzerPerField.put("PattyFreebaseRelations",
					new DoserIDAnalyzer());
			analyzerPerField.put("Relations", new DoserIDAnalyzer());
			analyzerPerField.put("Occurrences", new DoserIDAnalyzer());

			PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(
					new StandardAnalyzer(), analyzerPerField);

			final IndexWriterConfig config = new IndexWriterConfig(
					Version.LATEST, aWrapper);
			final IndexWriter newIndexWriter = new IndexWriter(newDir, config);

			for (Map.Entry<String, HashSet<String>> entry : UNIQUELABELSTRINGS
					.entrySet()) {
				String uri = entry.getKey();
				HashSet<String> set = entry.getValue();
				Document doc = new Document();

				// Add ID
				doc.add(new StringField("ID", "DBpedia_"
						+ String.valueOf(counter), Store.YES));
				counter++;
				// Add Mainlink
				doc.add(new StringField("Mainlink", uri, Store.YES));
				// Add labels
				List<String> origLabels = getDbPediaLabel(uri);
				HashSet<String> labelset = LABELS.get(uri);
				if (labelset != null) {
					for (String s : origLabels) {
						labelset.add(s);
					}
				}
				if (LABELS.containsKey(uri)) {
					labelset = LABELS.get(uri);
					for (String s : labelset) {
						doc.add(new TextField("Label", s, Store.YES));
					}
				}
				// Add ShortDescriptions
				String shortDescription = getDbPediaShortDescription(uri);
				doc.add(new TextField("ShortDescription", shortDescription,
						Store.YES));

				// Add longDescriptions
				String longDescription = getDbPediaLongDescription(uri);
				doc.add(new TextField("LongDescription", longDescription,
						Store.YES));

				// UniqueLabelStrings
				for (String s : set) {
					doc.add(new StringField("UniqueLabel", s, Store.YES));
				}

				// Add Occurrences
				if (OCCURRENCES.containsKey(uri)) {
					// First Build Occurrences String
					HashMap<String, Integer> map = OCCURRENCES.get(uri);
					StringBuilder builder = new StringBuilder();
					for (Map.Entry<String, Integer> ent : map.entrySet()) {
						String key = ent.getKey();
						Integer value = ent.getValue();
						builder.append(key + ":::" + String.valueOf(value)
								+ ";;;");
					}
					String occs = builder.toString();
					if (occs.length() > 0) {
						occs = occs.substring(0, occs.length() - 3);
					}
					doc.add(new TextField("Occurrences", occs, Store.YES));
				}

				// Add DBPedia Facts
				if (relationmap.containsKey(uri)) {
					LinkedList<String> l = relationmap.get(uri);
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
				} else {
					doc.add(new TextField("Relations", "", Store.YES));
				}

				// Add PattyFacts
				if (pattymap.containsKey(uri)) {
					LinkedList<String> l = pattymap.get(uri);
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
					doc.add(new TextField("PattyRelations", s, Store.YES));
				} else {
					doc.add(new TextField("PattyRelations", "", Store.YES));
				}

				// Add PattyFreebaseFacts
				if (pattyfreebasemap.containsKey(uri)) {
					LinkedList<String> l = pattyfreebasemap.get(uri);
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
					doc.add(new TextField("PattyFreebaseRelations", s,
							Store.YES));
				} else {
					doc.add(new TextField("PattyFreebaseRelations", "",
							Store.YES));
				}

				// Add Entity Evidence
				// if (evidencemap.containsKey(uri)) {
				// HashSet<String> evidenceset = evidencemap.get(uri);
				// for (String evidence : evidenceset) {
				// doc.add(new StringField("Evidence", evidence,
				// Field.Store.YES));
				// evidencecounter++;
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
			ResultSet results = null; // NOPMD by quh on 14.02.14 10:04
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

	public void extractEvidence() {
		File dir = new File(EVIDENCEDIRECTORY);
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; ++i) {
			File currentFile = files[i];
			String name = WikiPediaUriConverter
					.createConformDBpediaURI(currentFile.getName());
			String line = null;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(
						currentFile));
				while ((line = reader.readLine()) != null) {
					String[] splitter = line.split(",");
					if (evidencemap.containsKey(name)) {
						HashSet<String> set = evidencemap.get(name);
						set.add(splitter[0]);
						// System.out.println(name+ "   "+ splitter[0]);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(splitter[0]);
						// System.out.println(name+ "   "+ splitter[0]);
						evidencemap.put(name, set);
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

						if (OCCURRENCES.containsKey(uri)) {
							HashMap<String, Integer> map = OCCURRENCES.get(uri);
							if (map.containsKey(mention)) {
								Integer j = map.get(mention);
								j++;
								map.put(mention, j);
							} else {
								map.put(mention, new Integer(1));
							}
						} else {
							HashMap<String, Integer> map = new HashMap<String, Integer>();
							map.put(mention, new Integer(1));
							OCCURRENCES.put(uri, map);
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
	}

	public static void main(String[] args) {
		CreateDBPediaIndex index = new CreateDBPediaIndex();
		System.out.println("Preprocessing:");
		System.out.println("Step Evidence: ");
		// index.extractEvidence();
		System.out.println("Fill Index Relations:");
		index.fillRelationsIndex();
		index.fillPattyRelationIndex(PATTYWIKIPATTERN, PATTYWIKIINSTANCE);
		index.fillPattyFreebaseRelationIndex(PATTYFREEBASEPATTERN,
				PATTYFREEBASEINSTANCE);
		System.out.println("Step1:");
		index.workLinkText();
		System.out.println("Step2:");
		index.workEntities();
		System.out.println("Step3:");
		index.workRedirects();
		System.out.println("Step4:");
		// index.insertWebOccurrences();
		System.out.println("Step5:");
		index.createNewIndex();
		System.out.println("EvidenceCounter: " + evidencecounter);
	}
}