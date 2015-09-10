package doser.entitydisambiguation.modknowledgebase.dpo;

import java.util.LinkedList;
import java.util.List;

public class DocumentToProcess {

	private List<EntryToProcess> entryList;

	private String key;

	public List<EntryToProcess> getEntryList() {
		List<EntryToProcess> list = entryList;
		if(entryList == null) {
			list = new LinkedList<EntryToProcess>();
		}
		return list;
	}

	public String getKey() {
		return this.key;
	}

	public void setEntryList(final List<EntryToProcess> entryList) {
		this.entryList = entryList;
	}

	public void setKey(final String key) {
		this.key = key;
	}
}
