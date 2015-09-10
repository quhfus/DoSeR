package experiments.collective.entdoccentric;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import experiments.collective.entdoccentric.StandardQueryDataObject.EntityObject;
import experiments.collective.entdoccentric.query.QueryGenerator;
import experiments.collective.entdoccentric.query.QuerySettings;

public class CollectiveTestApproach {

	public static final String entIndexDirectory = "/home/quh/Arbeitsfläche/Entpackung/Arbeitsfläche/Code_Data/LuceneCorpora/Lucene 4.1/NoStemmingKnowledgeBaseCalbCSmallBackup/";
	public static final String docIndexDirectory = "/home/quh/Arbeitsfläche/Entpackung/Arbeitsfläche/Code_Data/LuceneCorpora/Lucene 4.1/NoStemmingCalbCSmall/";

	private boolean fuzzy;

	private boolean withDescription;

	private IndexSearcher entISearcher;
	private IndexReader entIReader;
	private Query entQuery;

	private IndexSearcher docISearcher;
	private IndexReader docIReader;
	private Query docQuery;
	private ScoreDoc[] docScore;

	private Set<Bucket> rdyBuckets;

	private int qryId;

	public CollectiveTestApproach(boolean fuzzy, boolean standardSeacher,
			boolean withDescription) {
		File indexDir = new File(entIndexDirectory);
		File indexDir1 = new File(docIndexDirectory);
		this.fuzzy = fuzzy;
		this.withDescription = withDescription;
		try {
			Directory dir = FSDirectory.open(indexDir);
			Directory dir1 = FSDirectory.open(indexDir1);
			entISearcher = new IndexSearcher(DirectoryReader.open(dir));
			entIReader = DirectoryReader.open(dir);
			docISearcher = new IndexSearcher(DirectoryReader.open(dir1));
			docIReader = DirectoryReader.open(dir1);
			if (!standardSeacher) {
				entISearcher.setSimilarity(new BM25Similarity());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void search(StandardQueryDataObject object, int queryNumber) {
		this.qryId = queryNumber;
		List<Document> docDocList = new LinkedList<Document>();
		Set<Bucket> buckets = new HashSet<Bucket>();

		List<EntityObject> objects = object.getEnts();
		for (int k = 0; k < objects.size(); ++k) {

			EntityObject obj = objects.get(k);
			// ENTITY CENTRIC CANDIDATES

			QuerySettings settings = new QuerySettings();
			settings.setDocumentcentric(false);
			settings.setDescriptionFuzzy(fuzzy);
			settings.setQuery("ltr");
			settings.setUseDescription(withDescription);
			entQuery = QueryGenerator.getInstance().createQuery(obj, settings);

			try {
				TopDocs top = entISearcher.search(entQuery, 10);
				ScoreDoc[] docs = top.scoreDocs;
				LinkedList<String> container = new LinkedList<String>();
				for (int i = 0; i < docs.length; i++) {
					container.add(entIReader.document(top.scoreDocs[i].doc)
							.get("ID"));
				}
				Bucket buck = new Bucket();
				buck.setContainer(container);
				buck.setObjectPosition(k);
				buckets.add(buck);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Document Centric Algorithm to get a set of relevant documents!
			settings = new QuerySettings();
			settings.setDocumentcentric(true);
			settings.setDescriptionFuzzy(fuzzy);
			settings.setQuery("std");
			settings.setUseDescription(withDescription);
			docQuery = QueryGenerator.getInstance().createQuery(obj, settings);

			try {
				TopDocs top = docISearcher.search(docQuery, 101);
				docScore = top.scoreDocs;
				/**
				 * Achtung! Damit der Test stimmt muss das Dokument, aus welchem
				 * das Query entity stammt entfernt werden. Im Folgenden eher
				 * als Hack realisiert. Muss noch besser implementiert werden.
				 */

				long objectId = Long.valueOf(object.getDocId());
				for (int i = 0; i < docScore.length; i++) {
					long scoreDoc = Long.valueOf(docIReader.document(
							docScore[i].doc).get("id"));
					// System.out.println(scoreDoc);
					if (objectId != scoreDoc) {
						Document doc = docIReader.document(docScore[i].doc);
						docDocList.add(doc);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.rdyBuckets = new HashSet<Bucket>();

		while (buckets.size() > 0) {
			Document currentBestDoc = checkCurrentBestDoc(docDocList, buckets);
			List<Bucket> rdy = extractRdyBuckets(currentBestDoc, buckets);
			rdyBuckets.addAll(rdy);

			for (Bucket rdyb : rdy) {
				if (buckets.contains(rdyb)) {
					buckets.remove(rdyb);
				}
			}
		}

	}

	private List<Bucket> extractRdyBuckets(Document doc, Set<Bucket> buckets) {
		List<Bucket> rdy = new LinkedList<Bucket>();
		String concepts = doc.get("concept");
		String[] arr = createConceptArray(concepts);
		for (int i = 0; i < arr.length; i++) {
			for (Bucket bucket : buckets) {
				boolean add = false;
				for (String s : bucket.getContainer()) {
					if (s.equalsIgnoreCase(arr[i])) {
						bucket.setEntity(arr[i]);
						rdy.add(bucket);
						add = true;
						break;
					}
				}
				if (add) {
					break;
				}
			}
		}
		return rdy;
	}

	private Document checkCurrentBestDoc(List<Document> docs, Set<Bucket> buck) {
		int max = 0;
		Document maxdoc = null;
		for (Document doc : docs) {
			int tempnr = 0;
			String concepts = doc.get("concept");
			String[] arr = createConceptArray(concepts);
			for (Bucket bucket : buck) {
				for (int i = 0; i < arr.length; i++) {
					boolean add = false;
					for (String s : bucket.getContainer()) {
						if (s.equalsIgnoreCase(arr[i])) {
							tempnr++;
							add = true;
							break;
						}
					}
					if (add) {
						break;
					}
				}
			}
			if (tempnr >= max) {
				max = tempnr;
				maxdoc = doc;
			}

		}
		System.out.println("Max wert: " + max + "Bucketanzahl: " + buck.size());
		return maxdoc;
	}

	public void configureResultObject(List<TrecEvalResultObject> result,
			List<EntityObject> object) {
		// System.out.println(rdyBuckets.size());
		for (int i = 0; i < rdyBuckets.size(); i++) {
			String[] resultStrings = new String[6];
			resultStrings[0] = String.valueOf(qryId + i);
			resultStrings[1] = "Q0";
			resultStrings[2] = String.valueOf(searchEntity(i));
			resultStrings[3] = String.valueOf(i + 1);
			resultStrings[4] = String.valueOf(1);
			resultStrings[5] = "STANDARD";
			result.get(i).setResult(resultStrings);

			LinkedList<String> str = object.get(i).getResultLinks();
			HashMap<Integer, String> hashm = new HashMap<Integer, String>();
			for (int j = 0; j < str.size(); j++) {
				hashm.put(str.get(j).hashCode(), str.get(j));
			}
			String[][] optimalResultStrings = new String[hashm.size()][4];
			int amountIt = 0;
			for (Integer key : hashm.keySet()) {
				optimalResultStrings[amountIt][0] = String.valueOf(qryId + i);
				optimalResultStrings[amountIt][1] = "Q0";
				optimalResultStrings[amountIt][2] = hashm.get(key);
				optimalResultStrings[amountIt][3] = "1";
				System.out.println(hashm.get(key));
				amountIt++;
			}
			System.out.println("----------------------------------------------"
					+ optimalResultStrings.length);
			result.get(i).setOptimalResult(optimalResultStrings);
		}
	}

	private String searchEntity(int i) {
		for (Bucket b : rdyBuckets) {
			if (b.getObjectPosition() == i) {
				return b.getEntity();
			}
		}
		return null;
	}

	private String[] createConceptArray(String str) {
		List<String> lst = new LinkedList<String>();
		str = str.trim();
		String[] arr = str.split(" ");
		for (int i = 0; i < arr.length; i++) {
			if (!arr[i].equalsIgnoreCase("") && analyseConcept(arr[i])) {
				// if(randomNr % 5 == 0) {
				lst.add(generateID(arr[i].toUpperCase()));
				// }
			}
		}
		String[] result = new String[lst.size()];
		lst.toArray(result);
		return result;
	}

	private boolean analyseConcept(String str) {
		String[] arr = str.split(":");
		// System.out.println(arr.length);
		if (arr.length < 3) {
			return false;
		}
		if (arr[2] == null || arr[2].equalsIgnoreCase("")) {
			return false;
		}
		return true;
	}

	private String generateID(String line) {
		String[] splitter = line.split(":");

		String link = "";
		if (splitter[1].equalsIgnoreCase("uniprot")
				&& !splitter[2].equalsIgnoreCase("") && splitter[2] != null) {
			link = "UN_" + splitter[2];
		} else if (splitter[1].equalsIgnoreCase("entrezgene")
				&& !splitter[2].equalsIgnoreCase("") && splitter[2] != null) {
			link = "NC_" + splitter[2];
		} else if (splitter[1].equalsIgnoreCase("umls")
				&& !splitter[2].equalsIgnoreCase("") && splitter[2] != null) {
			link = "LI_" + splitter[2];
		} else if (splitter[1].equalsIgnoreCase("ncbi")
				&& !splitter[2].equalsIgnoreCase("") && splitter[2] != null) {
			link = "NC_" + splitter[2];
		} else if (splitter[1].equalsIgnoreCase("disease")
				&& !splitter[2].equalsIgnoreCase("") && splitter[2] != null) {
			link = "LI_" + splitter[2];
		}
		return link;
	}

	public class Bucket {

		private LinkedList<String> container;
		private int objectPosition;
		private String entity;

		public LinkedList<String> getContainer() {
			return container;
		}

		public void setContainer(LinkedList<String> container) {
			this.container = container;
		}

		public int getObjectPosition() {
			return objectPosition;
		}

		public void setObjectPosition(int objectPosition) {
			this.objectPosition = objectPosition;
		}

		public String getEntity() {
			return entity;
		}

		public void setEntity(String entity) {
			this.entity = entity;
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof Bucket))
				return false;
			return this.objectPosition == ((Bucket) obj).objectPosition;
		}

		public int hashCode() {
			return objectPosition;
		}
	}
}
