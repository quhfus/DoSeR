package experiments.collective.entdoccentric;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class TestClass {

	public static final String indexDirectory = "/home/quh/Arbeitsfläche/Code_Data/LuceneCorpora/Lucene 4.1/PorterStemmerKnowledgeBaseCalbCSmall+UMLS+Uniprot/";

	public static void main(String[] args) throws IOException {
		File indexDir = new File(indexDirectory);

		Directory dir = FSDirectory.open(indexDir);
		IndexSearcher iSearcher = new IndexSearcher(DirectoryReader.open(dir));
		
		long time = System.currentTimeMillis();
		String name = "The proteasome is a multicatalytic proteinase complex which is characterized by its ability to cleave";
//		String name = "On the isolation of a prolactin inhibiting factor";
		BooleanQuery query = new BooleanQuery();
	//	query.add(new FuzzyQuery(new Term("titleandabs", "lipopolysaccharide")), Occur.MUST);
		String[] words = name.split(" ");
		DefaultSimilarity sim = new DefaultSimilarity();
		for (int i = 0; i < words.length; i++) {
			query.add(new TermQuery(new Term("title", words[i])),
					Occur.MUST);
//			query.add(new FuzzyQuery(new Term("abstract", words[i])),
//					Occur.SHOULD);
		}

		TopDocs top = iSearcher.search(query, 101);
		ScoreDoc[] score = top.scoreDocs;
		for (int i = 0; i < score.length; i++) {
			System.out.println(score[i].doc);
		}
		System.out.println(System.currentTimeMillis() - time);
	}
}
