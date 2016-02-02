package doser.entitydisambiguation.dpo;

import java.util.List;

/**
 * { "documentUri":"unique document id", "surfaceFormsToDisambiguate": [ {
 * "selectedText":"influenza", "context":
 * "Typically, influenza is transmitted through the air by coughs or sneezes, creating aerosols containing the virus."
 * , "position": { "pageId":0, "offsets":[1,2,3,5,6,7],
 * "boundingBox":{"minx":0.1, "miny":0.3, "maxx":0.01, "maxy":0.03} } } ],
 * "alreadyDisambiguatedEntities":[ { "text":"Illness",
 * "entityUri":"http://en.dbpedia.org/page/Illness", "confidence": 0.90,
 * "distance": 300 }, { "text":"Desease",
 * "entityUri":"http://en.dbpedia.org/page/Desease", "confidence": 0.65,
 * "distance": 500 } ] }
 * 
 * Version 2.0 is used for additional testing. Current version
 * offers the usage of a position array in surfaceFormsToDisambiguate
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class DisambiguationRequest {
	private String documentUri;
	private List<EntityDisambiguationDPO> surfaceFormsToDisambiguate;
	private Integer docsToReturn;
	private String mainTopic;

	public String getDocumentUri() {
		return this.documentUri;
	}

	public List<EntityDisambiguationDPO> getSurfaceFormsToDisambiguate() {
		return this.surfaceFormsToDisambiguate;
	}

	public void setDocumentUri(final String documentUri) {
		this.documentUri = documentUri;
	}

	public void setSurfaceFormsToDisambiguate(
			final List<EntityDisambiguationDPO> surfaceFormsToDisambiguate) {
		this.surfaceFormsToDisambiguate = surfaceFormsToDisambiguate;
	}

	public Integer getDocsToReturn() {
		return docsToReturn;
	}

	public void setDocsToReturn(Integer docsToReturn) {
		this.docsToReturn = docsToReturn;
	}

	public String getMainTopic() {
		return mainTopic;
	}

	public void setMainTopic(String mainTopic) {
		this.mainTopic = mainTopic;
	}
}
