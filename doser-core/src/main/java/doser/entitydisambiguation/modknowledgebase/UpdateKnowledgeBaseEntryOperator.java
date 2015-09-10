package doser.entitydisambiguation.modknowledgebase;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import doser.lucene.analysis.DoserIDAnalyzer;

public class UpdateKnowledgeBaseEntryOperator extends
		AbstractKnowledgebaseOperator {

	private static String buildStringFromHashMap(final Map<String, Integer> hash) {
		final StringBuffer buffer = new StringBuffer("");
		for (final Map.Entry<String, Integer> entry : hash.entrySet()) {
			buffer.append(entry.getKey() + ":::" + entry.getValue() + ";;;");
		}
		final String str = buffer.toString();
		return str.substring(0, str.length() - 3);
	}

	public static IndexableField updateOccurrences(final String occurrence,
			final IndexableField field, final String fieldname) {
		IndexableField res = null;
		final String fieldString = field.stringValue();
		final HashMap<String, Integer> hash = new HashMap<String, Integer>();
		if ((fieldString != null) && !fieldString.equalsIgnoreCase("")) {
			final String[] split = fieldString.split(";;;");
			for (final String element : split) {
				final String[] splitter = element.split(":::");
				int check = 1;
				try {
					check = Integer.valueOf(splitter[1]);
					hash.put(splitter[0], check);
				} catch (final NumberFormatException e) {
					res = field;
				}
			}
			if (hash.containsKey(occurrence)) {
				Integer amount = hash.get(occurrence);
				hash.put(occurrence, ++amount);
			} else {
				hash.put(occurrence, 1);
			}
			final String value = buildStringFromHashMap(hash);
			res = new TextField(fieldname, value, Field.Store.YES);

		} else if ((fieldString != null) && fieldString.equalsIgnoreCase("")) {
			res = new TextField(fieldname, occurrence + ":::1", Field.Store.YES);
		}
		return res;
	}

	private final KBModifications action;

	private final Map<String, HashMap<String, String>> attributes;

	private final String docPrimaryKey;

	public UpdateKnowledgeBaseEntryOperator(final String path,
			final Analyzer analyzer,
			final Map<String, HashMap<String, String>> hash,
			final String docPrimaryKey, final KBModifications action) {
		super(path, analyzer);
		this.attributes = hash;
		this.docPrimaryKey = docPrimaryKey;
		this.action = action;
	}

	@Override
	public void modifyIndex(final IndexWriter writer,
			final IndexSearcher searcher) throws ModifyKnowledgeBaseException {
		for (final Map.Entry<String, HashMap<String, String>> entry : this.attributes
				.entrySet()) {
			final String key = entry.getKey();
			final HashMap<String, String> hash = entry.getValue();
			final QueryParser qp = new QueryParser(this.docPrimaryKey,
					new DoserIDAnalyzer());
			try {
				final TopDocs top = searcher.search(
						qp.parse(QueryParserBase.escape(key)), 1);
				final ScoreDoc[] scores = top.scoreDocs;
				if (scores.length > 0) {
					final Document doc = new Document();
					final Document currentDoc = searcher.getIndexReader()
							.document(scores[0].doc);
					// BugFix create new Document und copy Fields.
					final List<IndexableField> fields = currentDoc.getFields();
					for (final IndexableField field : fields) {
						if (field.stringValue() != null) {
							if (field.name().equalsIgnoreCase(docPrimaryKey)) {
								doc.add(new StringField(field.name(), field
										.stringValue(), Field.Store.YES));
							} else {
								doc.add(new TextField(field.name(), field
										.stringValue(), Field.Store.YES));
							}
						}
					}
					final List<Document> docListToAdd = new LinkedList<Document>();
					docListToAdd.add(doc);
					for (final Map.Entry<String, String> subentry : hash
							.entrySet()) {
						final IndexableField field = doc.getField(subentry
								.getKey());
						if (field == null) {
							throw new ModifyKnowledgeBaseException(
									"UpdateField no found", null);
						}
						if (this.action.equals(KBModifications.OVERRIDEFIELD)) {
							doc.removeFields(subentry.getKey());
							String[] newentries = generateSeperatedFieldStrings(subentry
									.getValue());
							for (int i = 0; i < newentries.length; i++) {
								doc.add(new TextField(subentry.getKey(),
										newentries[i], Field.Store.YES));
							}
						} else if (this.action
								.equals(KBModifications.UPDATERELATEDLABELS)) {
							doc.removeFields(subentry.getKey());
							doc.add(updateOccurrences(subentry.getValue(),
									field, "surroundinglabels"));
						} else if (this.action
								.equals(KBModifications.UPDATEOCCURRENCES)) {
							doc.removeFields(subentry.getKey());
							IndexableField f = updateOccurrences(
									subentry.getValue(), field, "occurrences");
							doc.add(f);
						}
					}
					writer.updateDocuments(new Term(this.docPrimaryKey, key),
							docListToAdd);
				} else {
					throw new ModifyKnowledgeBaseException(
							"Document not found", null);
				}
			} catch (final IOException e) {
				throw new ModifyKnowledgeBaseException(
						"IOException in IndexSearcher", e);
			} catch (ParseException e) {
				throw new ModifyKnowledgeBaseException("Queryparser Exception",
						e);
			}
		}
	}

	// private void updateCachingOccurrences(int docId,
	// IndexableField f) {
	// HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
	// String str = f.stringValue();
	// final String[] split = str.split(";;;");
	// for (final String element : split) {
	// final String[] splitter = element.split(":::");
	// int check = 1;
	// try {
	// check = Integer.valueOf(splitter[1]);
	// hash.put(splitter[0].hashCode(), check);
	// } catch (final NumberFormatException e) {
	// Logger.getRootLogger().error(e.getStackTrace());
	// }
	// }
	// HashMapUpdateInformation updateInfos = new HashMapUpdateInformation(
	// UpdateTypes.Occurrences, docId, hash);
	// this.setChanged();
	// notifyObservers(updateInfos);
	// }

	/**
	 * Verschiedene Einträge werden in HTML formulare mit &#13;&#10 getrennt
	 * (neue Zeile). Jede Zeile soll ein eigenes Field im Index darstellen
	 * 
	 * @param the
	 *            whole string
	 * @return field content array
	 */
	private String[] generateSeperatedFieldStrings(String str) {
		String[] splitter = str.split("&#13;&#10;");
		return splitter;
	}

}
