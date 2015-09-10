package doser.summarization.dpo;

import java.util.List;

/**
 * Example:
 * 
 * { "summaries":[ "uri":"uri of a rdf ressource"
 * "summary":"summary of the respective rdf ressource" ] }
 * 
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class RDFSResponse {

	private List<Summary> summaries;

	public List<Summary> getSummaries() {
		return this.summaries;
	}

	public void setSummaries(final List<Summary> summaries) {
		this.summaries = summaries;
	}
}
