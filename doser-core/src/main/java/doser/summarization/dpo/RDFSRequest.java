package doser.summarization.dpo;

import java.util.List;

/**
 * Example:
 * 
 * { "uris":[ { "uri":"uri of a rdf ressource" } ] }
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class RDFSRequest {

	private List<String> uris;

	public List<String> getUris() {
		return this.uris;
	}

	public void setUris(final List<String> uris) {
		this.uris = uris;
	}
}
