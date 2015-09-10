package doser.server.actions.kbenrichment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import doser.entitydisambiguation.modknowledgebase.AddNewDocumentsOperator;
import doser.entitydisambiguation.modknowledgebase.KBModifications;
import doser.entitydisambiguation.modknowledgebase.KnowledgebaseModification;
import doser.entitydisambiguation.modknowledgebase.ModifyKnowledgeBaseException;
import doser.entitydisambiguation.modknowledgebase.NewDocumentOrUpdateOperator;
import doser.entitydisambiguation.modknowledgebase.UpdateKnowledgeBaseEntryOperator;
import doser.entitydisambiguation.modknowledgebase.dpo.DocumentToProcess;
import doser.entitydisambiguation.modknowledgebase.dpo.EntryToProcess;
import doser.entitydisambiguation.modknowledgebase.dpo.KBEnrichmentRequest;
import doser.entitydisambiguation.modknowledgebase.dpo.KBEnrichmentResponse;
import doser.lucene.analysis.DoserIDAnalyzer;
import doser.lucene.analysis.DoserStandardAnalyzer;

@Controller
@RequestMapping("/disambiguation/kbenrichment")
public class KBEnrichmentService {

	private static final String ADDDOCUMENT = "ADDDOCUMENT";

	private static final String ADDORUPDATEDOC = "ADDORUPDATE";

	private static final String UPDATEDOCUMENT = "UPDATEDOCUMENTS";

	private void doAddDocument(final KBEnrichmentRequest request) {
		final List<HashMap<String, String>> list = new LinkedList<HashMap<String, String>>();
		final List<DocumentToProcess> process = request.getDocList();
		for (final DocumentToProcess doc : process) {
			final HashMap<String, String> hash = new HashMap<String, String>();
			final List<EntryToProcess> entrylist = doc.getEntryList();
			for (final EntryToProcess entry : entrylist) {
				hash.put(entry.getFieldName(), entry.getValue());
			}
			list.add(hash);
		}

		Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
		analyzerPerField.put("Mainlink", new DoserIDAnalyzer());
		analyzerPerField.put("ID", new DoserIDAnalyzer());
		PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(
				new DoserStandardAnalyzer(), analyzerPerField);

		final AddNewDocumentsOperator operator = new AddNewDocumentsOperator(
				request.getKburi(), aWrapper, list,
				request.getPrimaryKeyField());

		try {
			KnowledgebaseModification.getInstance()
					.processNewKnowledgeOperation(operator);
		} catch (final ModifyKnowledgeBaseException e) {
			Logger.getRootLogger().error("ModifyKnowledgeBaseException", e);
		}
	}

	private void doAddOrUpdateDocument(final KBEnrichmentRequest request,
			final KBModifications mod) {
		final HashMap<String, String> hash = new HashMap<String, String>();
		final List<DocumentToProcess> docsToProcess = request.getDocList();
		final DocumentToProcess doc = docsToProcess.get(0);
		final List<EntryToProcess> list = doc.getEntryList();
		for (final EntryToProcess pro : list) {
			hash.put(pro.getFieldName(), pro.getValue());
		}

		Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
		analyzerPerField.put("Mainlink", new DoserIDAnalyzer());
		PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(
				new DoserStandardAnalyzer(), analyzerPerField);

		final NewDocumentOrUpdateOperator operator = new NewDocumentOrUpdateOperator(
				request.getKburi(), aWrapper, doc.getKey(), hash,
				request.getPrimaryKeyField(), mod);

		try {
			KnowledgebaseModification.getInstance()
					.processNewKnowledgeOperation(operator);
		} catch (final ModifyKnowledgeBaseException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
	}

	private void doUpdateDocument(final KBEnrichmentRequest request,
			final KBModifications mod) {
		final HashMap<String, HashMap<String, String>> hash = new HashMap<String, HashMap<String, String>>();
		final List<DocumentToProcess> docs = request.getDocList();
		for (final DocumentToProcess doc : docs) {
			final HashMap<String, String> map = new HashMap<String, String>();
			final List<EntryToProcess> entries = doc.getEntryList();
			for (final EntryToProcess entry : entries) {
				map.put(entry.getFieldName(), entry.getValue());
			}
			hash.put(doc.getKey(), map);
		}

		Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
		analyzerPerField.put("Mainlink", new DoserIDAnalyzer());
		PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(
				new DoserStandardAnalyzer(), analyzerPerField);

		final UpdateKnowledgeBaseEntryOperator operator = new UpdateKnowledgeBaseEntryOperator(
				request.getKburi(), aWrapper, hash,
				request.getPrimaryKeyField(), mod);

		try {
			KnowledgebaseModification.getInstance()
					.processNewKnowledgeOperation(operator);
		} catch (final ModifyKnowledgeBaseException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	public @ResponseBody
	KBEnrichmentResponse enrich(@RequestBody final KBEnrichmentRequest request) {
		final KBModifications mod = this.extractFieldAction(request
				.getFieldAction());
		if (request.getCommand().equalsIgnoreCase(ADDORUPDATEDOC)
				&& (mod != null)
				&& CheckRequestsForKBModification
						.checkAddOrUpdateDocumentRequest(request)) {
			this.doAddOrUpdateDocument(request, mod);
		} else if (request.getCommand().equalsIgnoreCase(ADDDOCUMENT)
				&& CheckRequestsForKBModification
						.checkAddDocumentRequest(request)) {
			this.doAddDocument(request);

		} else if (request.getCommand().equalsIgnoreCase(UPDATEDOCUMENT)
				&& (mod != null)
				&& CheckRequestsForKBModification
						.checkUpdateDocumentRequest(request)) {
			this.doUpdateDocument(request, mod);

		}
		return new KBEnrichmentResponse();
	}

	private KBModifications extractFieldAction(final String str) {
		KBModifications res = null;
		if (str != null
				&& str.equalsIgnoreCase(KBModifications.OVERRIDEFIELD
						.toString())) {
			res = KBModifications.OVERRIDEFIELD;
		} else if (str != null
				&& str.equalsIgnoreCase(KBModifications.UPDATEOCCURRENCES
						.toString())) {
			res = KBModifications.UPDATEOCCURRENCES;
		} else if (str != null
				&& str.equalsIgnoreCase(KBModifications.UPDATERELATEDLABELS
						.toString())) {
			res = KBModifications.UPDATERELATEDLABELS;
		}
		return res;
	}
}
