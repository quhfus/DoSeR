package doser.server.actions.kbenrichment;

import java.util.List;

import doser.entitydisambiguation.modknowledgebase.dpo.DocumentToProcess;
import doser.entitydisambiguation.modknowledgebase.dpo.EntryToProcess;
import doser.entitydisambiguation.modknowledgebase.dpo.KBEnrichmentRequest;

public final class CheckRequestsForKBModification {

	private CheckRequestsForKBModification() {
		super();
	}
	
	static boolean checkAddDocumentRequest(final KBEnrichmentRequest request) {
		boolean res = true;
		final List<DocumentToProcess> list = request.getDocList();
		if (list == null) {
			res = false;
		} else {
			for (final DocumentToProcess doc : list) {
				final List<EntryToProcess> entryList = doc.getEntryList();
				if (entryList == null) {
					res = false;
				}
			}
		}
		if (request.getKburi() == null
				|| request.getKburi().equalsIgnoreCase("") || list.isEmpty()
				|| request.getPrimaryKeyField() == null
				|| request.getPrimaryKeyField().equalsIgnoreCase("")) {
			res = false;
		}
		return res;
	}

	static boolean checkAddOrUpdateDocumentRequest(
			final KBEnrichmentRequest request) {
		boolean res = true;
		final List<DocumentToProcess> list = request.getDocList();
		final String key = list.get(0).getKey();
		final List<EntryToProcess> entryList = list.get(0).getEntryList();
		if (request.getKburi() == null
				|| request.getKburi().equalsIgnoreCase("") || list == null
				|| list.isEmpty() || entryList == null || entryList.isEmpty()
				|| request.getPrimaryKeyField() == null
				|| request.getPrimaryKeyField().equalsIgnoreCase("")
				|| key == null || key.equalsIgnoreCase("")) {
			res = false;
		}
		return res;
	}

	static boolean checkUpdateDocumentRequest(final KBEnrichmentRequest request) {
		boolean res = true;
		final List<DocumentToProcess> list = request.getDocList();
		if (list == null) {
			res = false;
		} else {
			for (final DocumentToProcess doc : list) {
				final List<EntryToProcess> entryList = doc.getEntryList();
				if (entryList == null) {
					res = false;
				}
			}
		}
		if (request.getKburi() == null
				|| request.getKburi().equalsIgnoreCase("") || list.isEmpty() || request.getPrimaryKeyField() == null
				|| request.getPrimaryKeyField().equalsIgnoreCase("")) {
			res = false;
		}
		return res;
	}

}
