package doser.entitydisambiguation.knowledgebases;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import doser.entitydisambiguation.properties.Properties;
import doser.lucene.features.EnCenExtFeatures;

/**
 * Default Entity Centric Knowledge base class. All features are implemented
 * default wise. If an implementation of an individual EnCenExtFeatures is
 * required, this class can be inherited.
 * 
 * If this class is inherited and the subclass integrates a new EnCenExtFeatures
 * class definition, the subclass should also overwrite the
 * getFeatureDefinition() method to return the correct instance of the new
 * EnCenExtFeatures class.
 * 
 * @author quhfus
 * 
 */
public class EntityCentricKnowledgeBaseDefault extends KnowledgeBase {

	protected static final String TRIMLABELAMOUNT = ";;;";
	protected static final String TRIMOCCOCC = ":::";
	protected static final String KBOCCURRENCESFIELD = "Occurrences";
	protected static final String KBFACTFIELD = "Relations";
	protected static final String PATTYRELATIONS = "PattyRelations";
	protected static final String PATTYFREEBASERELATIONS = "PattyFreebaseRelations";
	protected static final String KBMAINLINK = "Mainlink";

	/**
	 * This hashmap stores the Prior values of the standard DbPedia, CalbC and
	 * eHealth index. Key: Lucene intern document id Value: Amount of
	 * occurrences of this entity
	 */
	protected static Map<String, Integer> indexpriorHashMap;

	/**
	 * This hashmap stores the Sense Prior values of the standard DbPedia, CalbC
	 * and eHealth index.
	 * <p>
	 * <li>
	 * - Key: Lucene intern document id <br>
	 * - Value: HashMap storing the label appearances <br>
	 * 
	 * HashMap2:<br>
	 * - Key: Hash value of the appearing label <br>
	 * - Value: Number of occurrences of this label</li>
	 */
	protected static Map<Integer, HashMap<Integer, Integer>> indexsensePriorHashMap;
	protected static Map<String, HashMap<Integer, Integer>> indexsensePriorHashMapBlanc;

	/**
	 * This map stores the relations the entities can be associated with. Key:
	 * The source entity uri Value: HashSet of other entities that form a binary
	 * relation with the source entity.
	 */
	protected static Map<Integer, HashSet<String>> indexRelation;

	protected EnCenExtFeatures externFeatureDef;

	public EntityCentricKnowledgeBaseDefault(String uri, boolean dynamic,
			Similarity sim) {
		super(uri, dynamic, sim);
		this.externFeatureDef = new ECExternFeatures();
	}

	public EntityCentricKnowledgeBaseDefault(String uri, boolean dynamic) {
		super(uri, dynamic);
		this.externFeatureDef = new ECExternFeatures();
	}

	/**
	 * Returns the feature definition class.
	 * 
	 * @return
	 */
	public EnCenExtFeatures getFeatureDefinition() {
		return this.externFeatureDef;
	}

	@Override
	public void initialize() {
		indexpriorHashMap = new HashMap<String, Integer>();
		indexsensePriorHashMap = new HashMap<Integer, HashMap<Integer, Integer>>();
		indexRelation = new HashMap<Integer, HashSet<String>>();
		indexsensePriorHashMapBlanc = new HashMap<String, HashMap<Integer, Integer>>();
		final File file = new File(Properties.getInstance()
				.getEntityCentricKBWikipedia());
		try {
			final Directory dir = FSDirectory.open(file);
			final IndexReader iReader = DirectoryReader.open(dir);
			final int maxDoc = iReader.numDocs();
			for (int i = 0; i < maxDoc; i++) {
				if ((i % 50000) == 0) {
					Logger.getRootLogger().info("Loaded Entities: " + i);
				}

				final String val = iReader.document(i).get(KBOCCURRENCESFIELD);
				String entity = iReader.document(i).get("Mainlink")
						.replaceAll("http://dbpedia.org/resource/", "");
				if ((val != null) && !val.equalsIgnoreCase("")) {
					final String[] splitter = val.split(TRIMLABELAMOUNT);

					
					
					
					
					
					
					
					// int priorVal = 0;
					// for (final String element : splitter) {
					// // System.out.println(element);
					// final String[] splitter1 = element.split(TRIMOCCOCC);
					// int check = 1;
					// try {
					// check = Integer.valueOf(splitter1[1]);
					// } catch (final NumberFormatException e) {
					// Logger.getRootLogger().error(
					// "Warning NumberFormatException while Initialization: "
					// + val);
					// }
					// priorVal += check;
					// }
					// indexpriorHashMap.put(i, priorVal);
					//
					// // SensePriorHashMap
					// final HashMap<Integer, Integer> hash = new
					// HashMap<Integer,
					// Integer>();
					// for (final String element : splitter) {
					// final String[] value = element.split(TRIMOCCOCC);
					// int check = 1;
					// try {
					// check = Integer.valueOf(value[1]);
					// } catch (final NumberFormatException e) {
					// Logger.getRootLogger().error(
					// "Warning NumberFormatException while Initialization: "
					// + val);
					// }
					// hash.put(value[0].hashCode(), check);
					// }
					// indexsensePriorHashMap.put(i, hash);
					// }

				
				
				
				
				
				
//				
//					final HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
//					for (final String element : splitter) {
//						final String[] value = element.split(TRIMOCCOCC);
//						int check = 1;
//						try {
//							check = Integer.valueOf(value[1]);
//						} catch (final NumberFormatException e) {
//							Logger.getRootLogger().error(
//									"Warning NumberFormatException while Initialization: "
//											+ val);
//						}
//						hash.put(value[0].toLowerCase(Locale.US).hashCode(),
//								check);
//					}
//					indexsensePriorHashMapBlanc.put(entity, hash);

					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					// Create Temporary Prior HashMap for DBPedia only
					// Disambiguation
					// String entry =
					// iReader.document(i).get("DbpediaVertexDegree");
					// if(entry != null) {
					// indexpriorHashMap.put(entity, Integer.parseInt(entry));
					// }
				}

				// DBediaFacts
				// final String uri = iReader.document(i).get(KBMAINLINK);
				// final String facts = iReader.document(i).get(KBFACTFIELD);
				// final HashSet<String> set = new HashSet<String>();
				// if (facts != null && !facts.equalsIgnoreCase("")) {
				// final String[] splitter = facts.split(TRIMLABELAMOUNT);
				// for (int j = 0; j < splitter.length; j++) {
				// String s = splitter[j];
				// String[] split = s.split(TRIMOCCOCC);
				// set.add(split[1].replaceAll("dbpediaRes/", ""));
				// }
				// }

				// PattyRelations
				// final String pattyrelations = iReader.document(i).get(
				// PATTYRELATIONS);
				// if (pattyrelations != null
				// && !pattyrelations.equalsIgnoreCase("") &&
				// !pattyrelations.equalsIgnoreCase(" ")) {
				// final String[] splitter =
				// pattyrelations.split(TRIMLABELAMOUNT);
				// for (int j = 0; j < splitter.length; j++) {
				// String s = splitter[j];
				// String[] split = s.split(TRIMOCCOCC);
				// set.add(split[1].replaceAll("dbpediaRes/", ""));
				// }
				// }
				// PattyFreebaseRelations
				// final String pattyfreebaserelations =
				// iReader.document(i).get(
				// PATTYFREEBASERELATIONS);
				// if (pattyfreebaserelations != null
				// && !pattyfreebaserelations.equalsIgnoreCase("")) {
				// final String[] splitter =
				// pattyfreebaserelations.split(TRIMLABELAMOUNT);
				// for (int j = 0; j < splitter.length; j++) {
				// String s = splitter[j];
				// String[] split = s.split(TRIMOCCOCC);
				// set.add(split[1].replaceAll("dbpediaRes/", ""));
				// }
				// }
				// indexRelation.put(uri.hashCode(), set);
			}
			iReader.close();
		} catch (final IOException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
	}

	class ECExternFeatures implements EnCenExtFeatures {

		private ECExternFeatures() {
			super();
		}

		@Override
		public float getPriorOfDocument(int docId) {
			float res = 0.0f;
			if (indexpriorHashMap.containsKey(docId)) {
				final float prior = (float) Math.log(indexpriorHashMap
						.get(docId));
				res = prior;
			}
			return res;
		}

		@Override
		public float getSensePriorOfDocument(String keyword, int docId) {
			float res = 0.0f;
			if (indexsensePriorHashMap.containsKey(docId)) {
				final HashMap<Integer, Integer> hash = indexsensePriorHashMap
						.get(docId);
				if (hash.containsKey(keyword.toLowerCase(Locale.US).hashCode())) {
					final int value = hash.get(keyword.toLowerCase(Locale.US)
							.hashCode());
					res = (float) Math.log(value + 1);
				}
			}
			return res;
		}

		@Override
		public Set<String> getRelations(String url) {
			if (indexRelation.containsKey(url.hashCode())) {
				System.out.println("Wir liefern was");
				return indexRelation.get(url.hashCode());
			}
			return new HashSet<String>();
		}

		@Override
		public int getOccurrences(String sf, String uri) {
			String entity = uri.replaceAll("http://dbpedia.org/resource/", "");
			int res = 0;
			if (indexsensePriorHashMapBlanc.containsKey(entity)) {
				final HashMap<Integer, Integer> hash = indexsensePriorHashMapBlanc
						.get(entity);
				if (hash.containsKey(sf.toLowerCase(Locale.US).hashCode())) {
					res = hash.get(sf.toLowerCase(Locale.US).hashCode());
				}
			}
			return (res + 1);
		}

		// @Override
		// public int getOccurrences(String sf, String uri) {
		// String entity = uri.replaceAll("http://dbpedia.org/resource/", "");
		// int res = 0;
		// if (indexpriorHashMap.containsKey(entity)) {
		// res = indexpriorHashMap.get(entity);
		// }
		// return (res + 1);
		// }
	}
}
