package doser.entitydisambiguation.knowledgebases;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.similarities.Similarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import doser.lucene.features.IEntityCentricExtFeatures;

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
public class EntityCentricKnowledgeBase extends AbstractKnowledgeBase {

	private final static Logger logger = LoggerFactory.getLogger(EntityCentricKnowledgeBase.class);
	
	protected static final String TRIMLABELAMOUNT = ";;;";
	protected static final String TRIMOCCOCC = ":::";
	protected static final String KBOCCURRENCESFIELD = "Occurrences";
	protected static final String KBMAINLINK = "Mainlink";

	/**
	 * This hashmap stores the Prior values of the standard DbPedia, CalbC and
	 * eHealth index. Key: Lucene intern document id Value: Amount of
	 * occurrences of this entity
	 */
	protected Map<String, Integer> indexpriorHashMap;

	/**
	 * This hashmap stores the Sense Prior values of the standard DbPedia, CalbC
	 * and eHealth index.
	 * <p>
	 * <li>- Key: Lucene intern document id <br>
	 * - Value: HashMap storing the label appearances <br>
	 * 
	 * HashMap2:<br>
	 * - Key: Hash value of the appearing label <br>
	 * - Value: Number of occurrences of this label</li>
	 */
	protected Map<Integer, HashMap<Integer, Integer>> indexsensePriorHashMap;
	protected Map<String, HashMap<Integer, Integer>> indexsensePriorHashMapBlanc;

	/**
	 * This map stores the relations the entities can be associated with. Key:
	 * The source entity uri Value: HashSet of other entities that form a binary
	 * relation with the source entity.
	 */
	protected Map<Integer, HashSet<String>> indexRelation;

	protected IEntityCentricExtFeatures externFeatureDef;
	
	public EntityCentricKnowledgeBase(String uri, boolean dynamic, Similarity sim) {
		super(uri, dynamic, sim);
		this.externFeatureDef = new ECExternFeatures();
	}

	public EntityCentricKnowledgeBase(String uri, boolean dynamic) {
		super(uri, dynamic);
		this.externFeatureDef = new ECExternFeatures();
	}

	/**
	 * Returns the feature definition class.
	 * 
	 * @return
	 */
	public IEntityCentricExtFeatures getFeatureDefinition() {
		return this.externFeatureDef;
	}

	@Override
	public void initialize() {
		indexpriorHashMap = new HashMap<String, Integer>();
		indexsensePriorHashMap = new HashMap<Integer, HashMap<Integer, Integer>>();
		indexRelation = new HashMap<Integer, HashSet<String>>();
		indexsensePriorHashMapBlanc = new HashMap<String, HashMap<Integer, Integer>>();
		try {
			final IndexReader iReader = super.getSearcher().getIndexReader();
			final int maxDoc = iReader.numDocs();
			for (int i = 0; i < maxDoc; i++) {
				if ((i % 50000) == 0) {
					logger.info(this.kbName()+ " Loaded Entities: " + i);
				}

				final String val = iReader.document(i).get(KBOCCURRENCESFIELD);
				String entity = iReader.document(i).get("Mainlink").replaceAll("http://dbpedia.org/resource/", "");
				if ((val != null) && !val.equalsIgnoreCase("")) {
					final String[] splitter = val.split(TRIMLABELAMOUNT);
					final HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
					for (final String element : splitter) {
						final String[] value = element.split(TRIMOCCOCC);
						int check = 1;
						try {
							check = Integer.valueOf(value[1]);
						} catch (final NumberFormatException e) {
							logger.warn(this.kbName()+ " Warning NumberFormatException while Initialization:" + val);
						}
						hash.put(value[0].toLowerCase(Locale.US).hashCode(), check);
					}
					indexsensePriorHashMapBlanc.put(entity, hash);
				}
			}
			iReader.close();
		} catch (final IOException e) {
			logger.error("IOException in "+EntityCentricKnowledgeBase.class.getName(), e);
		}
	}
	
	protected String kbName() {
		return "General KB";
	}

	class ECExternFeatures implements IEntityCentricExtFeatures {

		private ECExternFeatures() {
			super();
		}

		@Override
		public float getPriorOfDocument(int docId) {
			float res = 0.0f;
			if (indexpriorHashMap.containsKey(docId)) {
				final float prior = (float) Math.log(indexpriorHashMap.get(docId));
				res = prior;
			}
			return res;
		}

		@Override
		public float getSensePriorOfDocument(String keyword, int docId) {
			float res = 0.0f;
			if (indexsensePriorHashMap.containsKey(docId)) {
				final HashMap<Integer, Integer> hash = indexsensePriorHashMap.get(docId);
				if (hash.containsKey(keyword.toLowerCase(Locale.US).hashCode())) {
					final int value = hash.get(keyword.toLowerCase(Locale.US).hashCode());
					res = (float) Math.log(value + 1);
				}
			}
			return res;
		}

		@Override
		public int getOccurrences(String sf, String uri) {
			String entity = uri.replaceAll("http://dbpedia.org/resource/", "");
			int res = 0;
			if (indexsensePriorHashMapBlanc.containsKey(entity)) {
				final HashMap<Integer, Integer> hash = indexsensePriorHashMapBlanc.get(entity);
				if (hash.containsKey(sf.toLowerCase().hashCode())) {
					res = hash.get(sf.toLowerCase().hashCode());
				}
			}
			return (res + 1);
		}
		
		@Override
		public Set<String> getRelations(String url) {
			if (indexRelation.containsKey(url.hashCode())) {
				return indexRelation.get(url.hashCode());
			}
			return new HashSet<String>();
		}
	}
}
