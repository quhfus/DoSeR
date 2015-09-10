package doser.tools.indexcreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import doser.lucene.analysis.DoserIDAnalyzer;

/**
 * Enriches the doser Lucene Index with Patty Facts
 * 
 * @author stefan
 *
 */

public class AddPattyFactsToIndex {
	
	public static final String FACTFIELD = "PattyFreebaseTypesFacts";
	
	public static final String OLDINDEX = "/mnt/ssd1/disambiguation/MMapLuceneIndexStandard/";
	public static final String NEWINDEX = "/home/zwicklbauer/NewIndexTryout";
	
	public static void main(String[] args) {
		int annotatedEntities = 0;
		HashMap<Integer, String> patternMap = new HashMap<Integer, String>();
		HashMap<String, LinkedList<String>> map = new HashMap<String, LinkedList<String>>();

		// Read Pattern File - either standard WikiTypes or Freebase 
		File patternFile = new File(args[1]);

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

		//Read Instancefile - either WikiTypes or Freebase Types
		File instanceFile = new File(args[0]);
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

				String subject = splitter[1].replaceAll(" ", "_");
				String object = splitter[2].replaceAll(" ", "_");

				if (!map.containsKey(subject)) {
					LinkedList<String> list = new LinkedList<String>();
					map.put(subject, list);
				}
				LinkedList<String> l = map.get(subject);
				l.add("pattyWikiTypes/" + patternMap.get(j) + ":::"
						+ "dbpediaRes/" + object);
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

		// Write Facts to Lucene Index
		File oldIndexFile = new File(OLDINDEX);
		File newIndexFile = new File(NEWINDEX);
		try {
			final Directory oldDir = FSDirectory.open(oldIndexFile);
			final Directory newDir = FSDirectory.open(newIndexFile);
			final IndexWriterConfig config = new IndexWriterConfig(
					Version.LATEST, new DoserIDAnalyzer());
			final IndexReader readerOldIndex = DirectoryReader.open(oldDir);
			final IndexWriter newIndexWriter = new IndexWriter(newDir, config);
			int numDocs = readerOldIndex.maxDoc();
			for (int i = 0; i < numDocs; i++) {
				Document doc = readerOldIndex.document(i);
				
				///////////////////////////////////////////////////////////////////////////////////////////////
//				doc.removeField("Relations");
//				String facts = doc.get("Facts");
//				doc.add(new TextField("DBpediaFacts", facts, Store.YES));
//				doc.removeField("Facts");
				///////////////////////////////////////////////////////////////////////////////////////////////
				
				String docurl = doc.get("Mainlink");
				LinkedList<String> l = map.get(docurl.replaceAll(
						"http://dbpedia.org/resource/", ""));
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
				if(s != null && s.length() > 1) {
					annotatedEntities++;
				}
				doc.add(new TextField(FACTFIELD, s, Store.YES));
				newIndexWriter.addDocument(doc);
			}
			readerOldIndex.close();
			newIndexWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Summary: ");
		System.out.println("HashSize: "+map.size());
		System.out.println("Annotated Entities: "+annotatedEntities);
	}

}
