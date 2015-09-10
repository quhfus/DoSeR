package doser.entitydisambiguation.algorithms.collective;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Vertex implements Comparable<Vertex> {
	private List<String> uris;
	private int entityQuery;
	private double score;
	private boolean isCandidate;
	private String description;
	private String text;
	private double occurrences;

	private Set<Edge> outgoingEdges;

	private double sumOutGoing;

	Vertex() {
		super();
		this.uris = new ArrayList<String>();
		this.outgoingEdges = new HashSet<Edge>();
		this.entityQuery = -1;
		this.isCandidate = false;
		this.sumOutGoing = 0;
	}

	void addOutGoingEdge(Edge e) {
		outgoingEdges.add(e);
		this.sumOutGoing += e.getTransition();
		for(Edge out : outgoingEdges) {
			out.setProbability(out.getTransition() / sumOutGoing);
		}
	}

	void removeAllOutgoingEdges() {
		this.outgoingEdges.clear();
	}

	Edge removeOutgoingEdge(Vertex v, Map<Edge, Number> edgeWeight) {
		Edge toRemove = null;
		for (Edge e : outgoingEdges) {
			if (e.getTarget().equals(v)) {
				toRemove = e;
				break;
			}
		}
		if (toRemove != null) {
			outgoingEdges.remove(toRemove);
			sumOutGoing -= toRemove.getTransition();
		}
		
		// Update Transition Probability
		for(Edge out : outgoingEdges) {
			out.setProbability(out.getTransition() / sumOutGoing);
			edgeWeight.put(out, out.getProbability());
		}
		
		return toRemove;
	}

	double getSumOutGoingEdges() {
		return sumOutGoing;
	}

	Set<Edge> getOutgoingEdges() {
		return this.outgoingEdges;
	}

	List<String> getUris() {
		return uris;
	}

	void addUri(String uri) {
		this.uris.add(uri);
	}

	boolean isCandidate() {
		return isCandidate;
	}

	void setCandidate(boolean isCandidate) {
		this.isCandidate = isCandidate;
	}

	int getEntityQuery() {
		return entityQuery;
	}

	void setEntityQuery(int entityQuery) {
		this.entityQuery = entityQuery;
	}

	void setGraphValue(double val) {
		this.score = val;
	}

	double getScore() {
		return this.score;
	}

	void setScore(double score) {
		this.score = score;
	}

	String getDescription() {
		return description;
	}

	void setDescription(String description) {
		this.description = description;
	}

	String getText() {
		return text;
	}

	void setText(String text) {
		this.text = text;
	}

	double getOccurrences() {
		return occurrences;
	}

	void setOccurrences(int occurrences) {
		this.occurrences = Math.log(occurrences + 1);
	}

	@Override
	public boolean equals(Object obj) {
		Vertex comp = (Vertex) obj;
		boolean isEqual = true;
		if (this.uris.size() != comp.getUris().size()
				|| this.entityQuery != comp.getEntityQuery()) {
			return false;
		}
		for (int i = 0; i < uris.size(); ++i) {
			if (!uris.get(i).equalsIgnoreCase(comp.getUris().get(i))) {
				isEqual = false;
				break;
			}
		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return (generateUriHash(this.uris) + ((Integer) this.getEntityQuery())
				.hashCode());
	}

	private int generateUriHash(List<String> uris) {
		int hash = 0;
		for (String uri : uris) {
			hash += uri.hashCode();
		}
		return hash;
	}

	/**
	 * The return values are switched to provide a descending order when using
	 * Collections.sort(), which generally provides an ascending sort order.
	 * 
	 */
	@Override
	public int compareTo(Vertex o) {
		if (this.getOccurrences() < o.getOccurrences()) {
			return 1;
		} else if (this.getOccurrences() > o.getOccurrences()) {
			return 1;
		} else {
			return 0;
		}
	}
}