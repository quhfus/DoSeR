package doser.entitydisambiguation.dpo;

import java.util.List;

/**
 * {
 * 
 * "documentUri":"unique document id", "disambiguatedSurfaceforms": [ {
 * "selectedText":"influenza", "position": { "pageId":0,
 * "offsets":[1,2,3,5,6,7], "boundingBox":{"minx":0.1, "miny":0.3, "maxx":0.01,
 * "maxy":0.03} }, "disEntities": [ { "text":"Influenza (Illness)"
 * "entityUri":"http://en.dbpedia.org/pages/..." "confidence":"0.80"
 * "description":"some additional description"
 * 
 * ---a list of synonyms (for a later stage)--- "synonyms": [ { "term":"..." } ]
 * } // more Items ] }
 * 
 * Version 2.0 is used for additional testing. Current version offers the usage
 * of a position array in surfaceFormsToDisambiguate
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class DisambiguationResponse {
	
	private List<Response> tasks; // NOPMD by quh on 18.02.14 09:34

	private String documentUri;
	
	public List<Response> getTasks() {
		return tasks;
	}

	public void setTasks(List<Response> tasks) {
		this.tasks = tasks;
	}

	public String getDocumentUri() {
		return this.documentUri;
	}

	public void setDocumentUri(final String documentUri) {
		this.documentUri = documentUri;
	}
}
