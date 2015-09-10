package doser.sequencedetection.algorithm;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Sequence<K extends NCutObject<K>> {

	private List<K> content;

	private int seqKey;

	public Sequence(int key) {
		super();
		this.seqKey = key;
		this.content = new LinkedList<K>();
	}

	public void addElement(K k) {
		content.add(k);
	}
	
	public void addElements(List<K> lst) {
		content.addAll(lst);
	}

	public void setSettings() {
		Collections.sort(content);
	}
}
