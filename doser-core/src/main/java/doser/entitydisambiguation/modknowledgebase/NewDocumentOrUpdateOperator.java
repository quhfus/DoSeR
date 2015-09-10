package doser.entitydisambiguation.modknowledgebase;

import java.io.IOException;
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

public class NewDocumentOrUpdateOperator extends AbstractKnowledgebaseOperator {

	private final KBModifications action;

	private final String docPrimKeyField;

	private final Map<String, String> hashMap;

	private final String uri;

	public NewDocumentOrUpdateOperator(final String path, final Analyzer analyzer,
			final String uri, final Map<String, String> hash,
			final String docPrimKeyField, final KBModifications action) {
		super(path, analyzer);
		this.uri = uri;
		this.hashMap = hash;
		this.docPrimKeyField = docPrimKeyField;
		this.action = action;
	}

	@Override
	public void modifyIndex(final IndexWriter writer,
			final IndexSearcher searcher) throws ModifyKnowledgeBaseException {
		final QueryParser qp = new QueryParser(this.docPrimKeyField, new DoserIDAnalyzer());
		try {
			final TopDocs top = searcher.search(
					qp.parse(QueryParserBase.escape(this.uri)), 1);
			final ScoreDoc[] scores = top.scoreDocs;

			final Document doc = new Document();
			if (scores.length == 0) {
				// Create Document first
				final Map<String, String> hash = KnowledgeBaseEntryCreation
						.createKnowledgeBaseEntryOutOfDbPediaURI(this.uri);

				for (final Map.Entry<String, String> entry : hash.entrySet()) {
					String key = entry.getKey();
					final String value = entry.getValue();
					if (key.contains("_")) {
						key = key.replaceAll("_[\\d]", "");
					}
					if (key.equalsIgnoreCase(docPrimKeyField)) {
						doc.add(new StringField(key, value, Field.Store.YES));
					} else {
						doc.add(new TextField(key, value, Field.Store.YES));
					}
				}
				writer.addDocument(doc);
				writer.commit();
			} else {
				final int docNr = scores[0].doc;
				final Document currentDoc = searcher.getIndexReader().document(
						docNr);
				// BugFix create new Document and copy Fields.
				final List<IndexableField> fields = currentDoc.getFields();
				for (final IndexableField field : fields) {
					if (field.name().equalsIgnoreCase(docPrimKeyField)) {
						doc.add(new StringField(field.name(), field
								.stringValue(), Field.Store.YES));
					} else {
						doc.add(new TextField(field.name(),
								field.stringValue(), Field.Store.YES));
					}
				}
			}

			// Update Document
			for (final Map.Entry<String, String> subentry : this.hashMap
					.entrySet()) {
				final IndexableField field = doc.getField(subentry.getKey());
				if (field == null) {
					throw new ModifyKnowledgeBaseException(
							"UpdateField no found", null);
				}
				final List<Document> docListToAdd = new LinkedList<Document>();
				docListToAdd.add(doc);
				if (this.action.equals(KBModifications.OVERRIDEFIELD)) {
					doc.removeFields(subentry.getKey());
					String[] newentries = generateSeperatedFieldStrings(subentry
							.getValue());
					for (int i = 0; i < newentries.length; i++) {
						doc.add(new TextField(subentry.getKey(), newentries[i],
								Field.Store.YES));
					}
				} else if (this.action
						.equals(KBModifications.UPDATERELATEDLABELS)) {
					doc.removeFields(subentry.getKey());
					doc.add(UpdateKnowledgeBaseEntryOperator.updateOccurrences(
							subentry.getValue(), field, "surroundinglabels"));
					writer.updateDocuments(new Term(this.docPrimKeyField),
							docListToAdd);
				} else if (this.action
						.equals(KBModifications.UPDATEOCCURRENCES)) {
					doc.removeFields(subentry.getKey());
					IndexableField f = UpdateKnowledgeBaseEntryOperator
							.updateOccurrences(subentry.getValue(), field,
									"occurrences");
					doc.add(f);
					writer.updateDocuments(new Term(this.docPrimKeyField,
							this.uri), docListToAdd);
				}
			}
		} catch (final IOException e) {
			throw new ModifyKnowledgeBaseException(
					"IOException in IndexSearcher", e);
		} catch (ParseException e) {
			throw new ModifyKnowledgeBaseException("QueryParser Exception", e);
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
