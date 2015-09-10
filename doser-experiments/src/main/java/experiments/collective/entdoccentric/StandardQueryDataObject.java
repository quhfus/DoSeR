package experiments.collective.entdoccentric;

import java.util.LinkedList;
import java.util.List;

public class StandardQueryDataObject {

	private int docId;
	
	private List<EntityObject> ents; 
	
	public StandardQueryDataObject(int docId) {
		super();
		this.ents = new LinkedList<EntityObject>();
		this.docId = docId;
	}
	
	public void addEntity(EntityObject obj) {
		ents.add(obj);
	}
	

	public int getDocId() {
		return docId;
	}

	public List<EntityObject> getEnts() {
		return ents;
	}

	public static class EntityObject {
		private String text;
		private LinkedList<String> resultLinks;
		private String context;
		private TrecEvalResultObject result;
		private int queryId;
		
		
		public EntityObject() {
			super();
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public LinkedList<String> getResultLinks() {
			return resultLinks;
		}
		public void setResultLinks(LinkedList<String> resultLinks) {
			this.resultLinks = resultLinks;
		}
		public String getContext() {
			return context;
		}
		public void setContext(String context) {
			this.context = context;
		}
		public TrecEvalResultObject getResult() {
			return result;
		}
		public void setResult(TrecEvalResultObject result) {
			this.result = result;
		}
		public int getQueryId() {
			return queryId;
		}
		public void setQueryId(int queryId) {
			this.queryId = queryId;
		}
	}
}
