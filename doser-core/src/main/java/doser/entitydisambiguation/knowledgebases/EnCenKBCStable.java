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

public class EnCenKBCStable extends EntityCentricKnowledgeBaseDefault {

	/**
	 * This hashmap stores the Sense Prior values of the table computer science
	 * index
	 * <p>
	 * <li>
	 * - Key: Lucene intern document id <br>
	 * - Value: HashMap storing the label appearances <br>
	 * 
	 * HashMap2:<br>
	 * - Key: Hash value of the appearing label <br>
	 * - Value: Number of occurrences of this label</li>
	 */
	private static Map<Integer, HashMap<Integer, Integer>> cstableindexsensePriorHashMap;

	public EnCenKBCStable(String uri, boolean dynamic, Similarity sim) {
		super(uri, dynamic, sim);
		this.externFeatureDef = new ECCSTableExternFeatures();
	}

	@Override
	public void initialize() {
		cstableindexsensePriorHashMap = new HashMap<Integer, HashMap<Integer, Integer>>();
		// cstableindexrelationContextMap = new HashMap<Integer,
		// HashMap<Integer, Integer>>();
		final File file = new File(Properties.getInstance().getCSTableIndex());
		try {
			final Directory dir = FSDirectory.open(file);
			final IndexReader iReader = DirectoryReader.open(dir);
			final int maxDoc = iReader.numDocs();
			for (int i = 0; i < maxDoc; i++) {
				String val = iReader.document(i).get("occurrences");
				if ((i % 50000) == 0) {
					Logger.getRootLogger().info("Loaded Entities: " + i);
				}
				if ((val != null) && !val.equalsIgnoreCase("")) {
					final String[] splitter = val.split(TRIMLABELAMOUNT);
					final HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
					for (final String element : splitter) {
						final String[] value = element.split(TRIMOCCOCC);
						int check = 1;
						try {
							check = Integer.valueOf(value[1]);
						} catch (final NumberFormatException e) {
							Logger.getRootLogger().error(e.getStackTrace());
						}
						hash.put(value[0].hashCode(), check);
					}
					cstableindexsensePriorHashMap.put(i, hash);
				}
				val = iReader.document(i).get("surroundinglabels");
				if ((val != null) && !val.equalsIgnoreCase("")) {
					final String[] splitter = val.split(TRIMLABELAMOUNT);
					final HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
					for (final String element : splitter) {
						final String[] value = element.split(TRIMOCCOCC);
						int check = 1;
						try {
							check = Integer.valueOf(value[1]);
						} catch (final NumberFormatException e) {
							Logger.getRootLogger().error(e.getStackTrace());
						}
						hash.put(value[0].hashCode(), check);
					}
					// cstableindexrelationContextMap.put(i, hash);
				}
			}
		} catch (final IOException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
	}

	private class ECCSTableExternFeatures implements EnCenExtFeatures {

		@Override
		public float getPriorOfDocument(int docId) {
			return 0;
		}

		@Override
		public float getSensePriorOfDocument(String keyword, int docId) {
			float res = 0.0f;
			if (cstableindexsensePriorHashMap.containsKey(docId)) {
				final HashMap<Integer, Integer> hash = cstableindexsensePriorHashMap
						.get(docId);
				if (hash.containsKey(keyword.toLowerCase(Locale.US).hashCode())) {
					final int value = hash.get(keyword.toLowerCase(Locale.US)
							.hashCode());
					final float prior = (float) Math.log(value + 1);
					res = prior;
				}
			}
			return res;
		}

		@Override
		public Set<String> getRelations(String url) {
			return new HashSet<String>();
		}

		@Override
		public int getOccurrences(String sf, String uri) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
}
