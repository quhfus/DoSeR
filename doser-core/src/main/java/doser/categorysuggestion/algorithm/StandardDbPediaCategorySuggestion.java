package doser.categorysuggestion.algorithm;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import doser.entitydisambiguation.properties.Properties;
import doser.entitydisambiguation.table.logic.Type;

public class StandardDbPediaCategorySuggestion {

	private final static StandardDbPediaCategorySuggestion INSTANCE = null;

	public static StandardDbPediaCategorySuggestion getInstance() {
		StandardDbPediaCategorySuggestion ret;
		if (INSTANCE == null) {
			ret = new StandardDbPediaCategorySuggestion();
		} else {
			ret = INSTANCE;
		}
		return ret;
	}

	private transient IndexReader iReader;

	private transient IndexSearcher iSearcher;

	public StandardDbPediaCategorySuggestion() {
		super();
		try {
			// Directory dir = FSDirectory.open(new
			// File("/home/quh/Arbeitsfläche/Wissensbasen/DbPediaCategories(EExcess)"));
			final Directory dir = FSDirectory.open(new File(Properties
					.getInstance().getCategorySuggestionIndex()));
			this.iReader = DirectoryReader.open(dir);
			this.iSearcher = new IndexSearcher(DirectoryReader.open(dir));
		} catch (final IOException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
	}

	public List<Type> suggest(final String input, final String lang) {
		// by
		// quh
		// on
		// 12.02.14
		// 10:37
		final List<Type> list = new LinkedList<Type>();
		String languageField;
		if (lang.equalsIgnoreCase("en")) {
			languageField = "label_en";
		} else if (lang.equalsIgnoreCase("de")) {
			languageField = "label_de";
		} else if (lang.equalsIgnoreCase("fr")) {
			languageField = "label_fr";
		} else {
			languageField = "label_un";
		}
		final WildcardQuery query = new WildcardQuery(new Term(languageField,
				input.toLowerCase(Locale.US) + "*"));
		try {
			final TopDocs docs = this.iSearcher.search(query, 3000);
			final ScoreDoc[] scoredoc = docs.scoreDocs;
			for (final ScoreDoc scoreDoc : scoredoc) {
				final Document doc = this.iReader.document(scoreDoc.doc);
				final Type cat = new Type(doc.get(languageField + "_original"), doc.get("url"), true, 0);
				list.add(cat);
			}
		} catch (final IOException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		Collections.sort(list);
		List<Type> res;
		if (list.size() > 25) {
			res = list.subList(0, 25);
		} else {
			res = list;
		}
		return res;
	}

	// public static void main(String[] args) {
	// List<Category> cat =
	// StandardDbPediaCategorySuggestion.getInstance().suggest("Science", "en");
	// for (Iterator<Category> iterator = cat.iterator(); iterator.hasNext();) {
	// Category category = (Category) iterator.next();
	// System.out.println(category.getUrl());
	// System.out.println(category.getLabel());
	// }
	// }
}
