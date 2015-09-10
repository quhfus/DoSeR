package experiments.collective.entdoccentric;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Disambiguation Optimization class. Method callup when framework starts. After
 * a short initialization phase disambiguation requests can be processed much
 * faster.
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public final class StartupInformationLoader {

	private static HashMap<Integer, Integer> priorHashMap;

	private static HashMap<Integer, HashMap<Integer, Integer>> sensePriorHashMap;

	private static HashMap<Integer, Integer> idToDocIdHashmap;

	public static void initializeDisambiguationFramework() {
		createPriorHashMap();
	}

	private static void createPriorHashMap() {
		idToDocIdHashmap = new HashMap<Integer, Integer>();
		priorHashMap = new HashMap<Integer, Integer>();
		sensePriorHashMap = new HashMap<Integer, HashMap<Integer, Integer>>();
		File file = new File(CollectiveTestApproach.entIndexDirectory);
		try {
			Directory dir = FSDirectory.open(file);
			IndexReader iReader = DirectoryReader.open(dir);
			int maxDoc = iReader.numDocs();
			for (int i = 0; i < maxDoc; i++) {
				String val = iReader.document(i).get("occurences");
				if (val != null && !val.equalsIgnoreCase("")) {
					String[] splitter = val.split(";;;");
					int priorVal = 0;
					for (int j = 0; j < splitter.length; j++) {
						String[] splitter1 = splitter[j].split(":::");
						priorVal += Integer.valueOf(splitter1[1]);
					}
					priorHashMap.put(i, priorVal);
					idToDocIdHashmap.put(iReader.document(i).get("ID")
							.hashCode(), i);

					// SensePriorHashMap
					HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
					for (int j = 0; j < splitter.length; j++) {
						String[] value = splitter[j].split(":::");
						hash.put(value[0].hashCode(), Integer.valueOf(value[1]));
					}
					sensePriorHashMap.put(i, hash);
				}
			}
			iReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static float getPriorOfDocument(int docId) {
		if (priorHashMap.get(docId) == null) {
			return 0;
		} else {
			float prior = (float) Math.log(priorHashMap.get(docId));
			return prior;
		}
	}

	public static float getSensePriorOfDocument(int docId, String keyword) {
		if (!sensePriorHashMap.
				containsKey(docId)) {
			return 0;
		} else {
			HashMap<Integer, Integer> hash = sensePriorHashMap.get(docId);
			if (!hash.containsKey(keyword.hashCode())) {
				return 0;
			}
			int value = hash.get(keyword.hashCode());
			float prior = (float) Math.log(value);
			return prior;
		}
	}

	public static float getPriorOfDocument(String id) {
		if(!idToDocIdHashmap.containsKey(id.hashCode())) {
			return 0;
		}
		return getPriorOfDocument(idToDocIdHashmap.get(id.hashCode()));
	}

	public static float getSensePriorOfDocument(String id, String keyword) {
		if(!idToDocIdHashmap.containsKey(id.hashCode())) {
			return 0;
		}
		return getSensePriorOfDocument(idToDocIdHashmap.get(id.hashCode()),
				keyword);
	}

}
