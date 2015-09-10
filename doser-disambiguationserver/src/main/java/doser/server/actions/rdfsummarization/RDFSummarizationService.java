package doser.server.actions.rdfsummarization;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import doser.summarization.algorithm.SummarizationSOLRIndex;
import doser.summarization.algorithm.SummaryInfos;
import doser.summarization.dpo.RDFSRequest;
import doser.summarization.dpo.RDFSResponse;
import doser.summarization.dpo.Summary;

@Controller
@RequestMapping("/disambiguation/summarize")
public class RDFSummarizationService {

	@RequestMapping(method = RequestMethod.POST)
	public @ResponseBody
	RDFSResponse disambiguate(@RequestBody final RDFSRequest request) {
		final SummarizationSOLRIndex indexing = new SummarizationSOLRIndex();
		final List<String> uris = request.getUris();
		final List<Summary> summaryList = new LinkedList<Summary>();
		final RDFSResponse response = new RDFSResponse();
		for (int i = 0; i < uris.size(); i++) {
			final SummaryInfos info = indexing.summarize(uris.get(i));
			final Summary sum = new Summary();
			sum.setSummary(info.getSummary());
			sum.setUri(info.getUri());
			sum.setLabel(info.getLabel());
			sum.setType(info.getTypes());
			summaryList.add(sum);
		}
		response.setSummaries(summaryList);
		return response;
	}
}
