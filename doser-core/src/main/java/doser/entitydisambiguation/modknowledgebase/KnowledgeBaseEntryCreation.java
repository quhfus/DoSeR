package doser.entitydisambiguation.modknowledgebase;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.hp.hpl.jena.query.QueryException;

import doser.tools.RDFGraphOperations;

public final class KnowledgeBaseEntryCreation {

	private static KnowledgeBaseEntryCreation instance = null;

	public static Map<String, String> createKnowledgeBaseEntryOutOfDbPediaURI(
			final String uri) throws ModifyKnowledgeBaseException {
		final Map<String, String> res = new HashMap<String, String>();
		try {
			final List<String> labelList = RDFGraphOperations
					.getDbPediaLabel(uri);
			final String description = RDFGraphOperations
					.getDbPediaDescription(uri);
			if (checkResource(uri, labelList)) {
				// Set ID
				final String split[] = uri.split("/");
				res.put("ID", "DbPedia_" + split[split.length - 1]);

				// Set Labels
				for (int i = 0; i < labelList.size(); ++i) {
					if (i == 0) {
						res.put("label", labelList.get(i));
					} else {
						res.put("label_" + i, labelList.get(i));
					}
				}

				// Set Description
				if (description == null) {
					res.put("description", "");
				} else {
					res.put("description", description);
				}
				// Set Mainlink
				res.put("mainlink", uri.toLowerCase(Locale.US));

				// Set Occurrences
				res.put("occurrences", "");

				// Set Surrounding labels
				res.put("surroundinglabels", "");
			}
		} catch (final QueryException e) {
			throw new ModifyKnowledgeBaseException("DbPedia Uri Invalid", e);
		}
		return res;
	}

	public static KnowledgeBaseEntryCreation getInstance() {
		synchronized (instance) {
			if (instance == null) {
				instance = new KnowledgeBaseEntryCreation();
			}
		}
		return instance;
	}

	private static boolean checkResource(final String url,
			final List<String> labels) {
		boolean res = false;
		if (url.startsWith("http://dbpedia.org/resource/") && !labels.isEmpty()) {
			res = true;
		}
		return res;
	}

	private KnowledgeBaseEntryCreation() {
		super();
	}

}
