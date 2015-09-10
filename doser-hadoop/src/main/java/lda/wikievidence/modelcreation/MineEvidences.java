package lda.wikievidence.modelcreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lda.wikievidence.modelcreation.WikipediaLDAThreadExtractEvidenceTerms.Output;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.google.gson.Gson;

public class MineEvidences extends LDAExecutor {

	public static final String CIRCLES = "/home/zwicklbauer/circles.dat";
	public static final String EVIDENCEDIR = "/home/hduser/EvidenceMining/evidence";
	public static final String HADOOPINPUT = "/home/zwicklbauer/ldadata.dat";
	public static final String LUCENEINDEX = "/home/zwicklbauer/MMapLuceneIndexStandard/";
	public static final int RANDOMDOCUMENTS = 1000;
	public static final int MODDOCUMENTS = 6;

	public static final int POOLSIZE = 26;

	private HashSet<String> indexStrings;

	private Map<Integer, Output> hashmap;
	
	private Map<String, String> fileHashMap;
	
	private Set<String> fileSet;

	public MineEvidences() {
		super();
		this.indexStrings = new HashSet<String>();
		this.hashmap = new HashMap<Integer, Output>();
		this.fileSet = new HashSet<String>();
		this.fileHashMap = new HashMap<String, String>();
	}
	
	public void initializeAvailableFiles() {
		File dir = new File(EVIDENCEDIR);
		String files[] = dir.list();
		for(String file : files) {
			fileSet.add(file);
		}
	}

	public void fillHashMap() {
		File indexDirectory = new File(LUCENEINDEX);
		IndexReader reader = null;
		try {
			Directory dir = FSDirectory.open(indexDirectory);
			reader = DirectoryReader.open(dir);
			int docs = reader.maxDoc();
			for (int i = 0; i < docs; i++) {
				Document doc = reader.document(i);
				String s = doc.get("Mainlink");
				s = s.replaceAll("http://dbpedia.org/resource/", "");
				s = URLDecoder.decode(s, "UTF-8");
				s = URLEncoder.encode(s, "UTF-8");
				indexStrings.add(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		File file = new File(CIRCLES);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			List<LDAClient> lst = new LinkedList<LDAClient>();
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] split = line.split("\\|");
				for (int i = 0; i < split.length; i++) {
					split[i] = split[i].replaceAll(".html", "");
				}

				if (lst.size() == POOLSIZE) {
					executeThreadPool(lst);
					lst.clear();
				}
				if (indexStrings.contains(split[0]) && !fileSet.contains(split[0])) {
					lst.add(new WikipediaLDAThreadExtractEvidenceTerms(lst.size(), 50, hashmap, split, fileHashMap));
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initializeData() {
		File file = new File(HADOOPINPUT);
		Gson gson = new Gson();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			int lineNr = 0;
			int entries = 0;
			while ((line = reader.readLine()) != null && lineNr < 1000000) {
				if ((lineNr % MODDOCUMENTS) == 0) {
					Output out = gson.fromJson(line, Output.class);
					hashmap.put(entries, out);
					entries++;
				}
				lineNr++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void initializeEntityFiles() {
		File dir = new File(WikipediaLDAThreadExtractEvidenceTerms.WIKIPEDIAPAGESDIR);
		String s[] = dir.list();
		for (int i = 0; i < s.length; i++) {
			String str = s[i].replaceAll(".html", "");
			str = str.replaceAll("'", "%");
			try {
				str = URLDecoder.decode(str, "UTF-8");
				str = URLEncoder.encode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
//			System.out.println(str+"    "+s[i]);
			fileHashMap.put(str, s[i]);
		}
	}


	public static void main(String[] args) {
//		MineEvidences evidence = new MineEvidences();
//		evidence.initializeEntityFiles();
//		System.out.println("First");
//		evidence.initializeAvailableFiles();
//		System.out.println("Second");
//		evidence.fillHashMap();
//		System.out.println("Third");
//		evidence.initializeData();
//		System.out.println("Fourth");
//		evidence.start();
		
		String test = "Test_(test)";
		try {
			System.out.println(URLEncoder.encode(test, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}