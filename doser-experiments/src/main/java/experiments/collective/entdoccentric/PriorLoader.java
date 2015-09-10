package experiments.collective.entdoccentric;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class PriorLoader {

	private static HashMap<Integer, Integer> priorHashMap;

	private static HashMap<Integer, HashMap<Integer, Integer>> sensePriorHashMap;

	public static void initializeDisambiguationFramework() {
		createPriorHashMap();
	}

	private static void createPriorHashMap() {
		priorHashMap = new HashMap<Integer, Integer>();
		sensePriorHashMap = new HashMap<Integer, HashMap<Integer, Integer>>();
//		File file = new File(Properties.getInstance().getEntityCentricKBLocation());
		File file = new File("/home/quh/Arbeitsfläche/Entpackung/Arbeitsfläche/Code_Data/LuceneCorpora/Lucene 4.1/NoStemmingKnowledgeBaseCalbCSmallBackup/");
		int overallAnnos = 0;
		try {
			Directory dir = FSDirectory.open(file);
			IndexReader iReader = DirectoryReader.open(dir);
			int maxDoc = iReader.numDocs();
			for (int i = 0; i < maxDoc; i++) {
				if (i % 50000 == 0) {
					System.out.println("Loaded Entities: "+i);
				}
				
				String val = iReader.document(i).get("occurences");
				
				if (val != null && !val.equalsIgnoreCase("")) {
					String[] splitter = val.split(";;;");
					int priorVal = 0;
					
					HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
					for (int j = 0; j < splitter.length; j++) {
						String[] splitter1 = splitter[j].split(":::");
						int check = 1;
						try {
							check = Integer.valueOf(splitter1[1]);
						} catch (NumberFormatException e) {
						}
						int newnr = generateNr(check);
						priorVal += newnr;
						overallAnnos += newnr;
						hash.put(splitter1[0].hashCode(), newnr);
					}
					priorHashMap.put(i, priorVal);

					
//					
//					for (int j = 0; j < splitter.length; j++) {
//						String[] value = splitter[j].split(":::");
//						int check = 1;
//						try {
//							check = Integer.valueOf(value[1]);
//						} catch (NumberFormatException e) {
//						}
//						int newnr = generateNr(check);
//						hash.put(value[0].hashCode(), newnr);
//					}
					sensePriorHashMap.put(i, hash);
				}
			}
			iReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("OverallAnnos: "+overallAnnos);
	}
	
	private static int generateNr(int basicNr) {
		Random random = new Random();
		int j = 0;
		for (int i = 1; i <= basicNr; i++) {
			int ran = random.nextInt();
			if(ran % 25 == 0) {
				j++;
			}
		}
		return j;
	}

	public static float getPriorOfDocument(int docId) {
		if(!priorHashMap.containsKey(docId)) {
			return 0;
		} else {
			float prior = (float) Math.log(priorHashMap.get(docId));
			return prior;
		}
	}

	public static float getSensePriorOfDocument(int docId, String keyword) {
		if(!sensePriorHashMap.containsKey(docId)) {
			return 0;
		} else {
			HashMap<Integer, Integer> hash = sensePriorHashMap.get(docId);
			if (!hash.containsKey(keyword.toLowerCase().hashCode())) {
				return 0;
			}
			int value = hash.get(keyword.toLowerCase().hashCode());
			float prior = (float) Math.log(value);
			return prior;
		}
	}
}
