package doser.entitydisambiguation.modknowledgebase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

public class AddNewDocumentsOperator extends AbstractKnowledgebaseOperator {

	private final List<HashMap<String, String>> attributes;

	private final String primaryKeyField;
	
	public AddNewDocumentsOperator(final String path, final Analyzer anal,
			final List<HashMap<String, String>> attributes, final String primaryKeyField) {
		super(path, anal);
		this.attributes = attributes;
		this.primaryKeyField = primaryKeyField;
	}

	@Override
	public void modifyIndex(final IndexWriter writer,
			final IndexSearcher searcher) throws ModifyKnowledgeBaseException {
		for (final HashMap<String, String> hash : this.attributes) {
			final Document doc = new Document();
			for (final Map.Entry<String, String> entry : hash.entrySet()) {
				String key = entry.getKey();
				final String value = entry.getValue();
				if (key.contains("_")) {
					key = key.replaceAll("_[\\d]", "");
				}
				if (key.equalsIgnoreCase(primaryKeyField)) {
					doc.add(new StringField(key, value, Field.Store.YES));
				} else {
					doc.add(new TextField(key, value, Field.Store.YES));
				}
			}
			try {
				writer.addDocument(doc);
			} catch (final IOException e) {
				throw new ModifyKnowledgeBaseException(
						"IndexWriter add document exception", e);
			}
		}
	}
}
