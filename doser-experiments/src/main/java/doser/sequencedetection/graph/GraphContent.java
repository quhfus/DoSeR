package doser.sequencedetection.graph;

public class GraphContent<T> {

	private boolean ambiguous;

	private T content;

	GraphContent(T content, boolean ambiguous) {
		this.content = content;
		this.ambiguous = ambiguous;
	}

	public T getContent() {
		return content;
	}

	public boolean isAmbiguous() {
		return ambiguous;
	}
}