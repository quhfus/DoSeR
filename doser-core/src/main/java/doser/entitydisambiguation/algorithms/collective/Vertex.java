package doser.entitydisambiguation.algorithms.collective;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Vertex implements Comparable<Vertex> {
	private List<String> uris;
	private int entityQuery;
	private double score;
	private boolean isCandidate;
	private String description;
	private String text;
	private String context;
	private double occurrences;

	private Set<Edge> outgoingEdges;

	private double sumOutGoing;

	public Vertex() {
		super();
		this.uris = new ArrayList<String>();
		this.outgoingEdges = new HashSet<Edge>();
		this.entityQuery = -1;
		this.isCandidate = false;
		this.sumOutGoing = 0;
		this.text = "";
		this.context = "";
	}

	public void addOutGoingEdge(Edge e) {
		outgoingEdges.add(e);
		this.sumOutGoing += e.getTransition();
		for(Edge out : outgoingEdges) {
			out.setProbability(out.getTransition() / sumOutGoing);
		}
	}

	public void removeAllOutgoingEdges() {
		this.outgoingEdges.clear();
	}

	public Edge removeOutgoingEdge(Vertex v, Map<Edge, Number> edgeWeight) {
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

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public double getSumOutGoingEdges() {
		return sumOutGoing;
	}

	public Set<Edge> getOutgoingEdges() {
		return this.outgoingEdges;
	}

	public List<String> getUris() {
		return uris;
	}

	public void addUri(String uri) {
		this.uris.add(uri);
	}

	public boolean isCandidate() {
		return isCandidate;
	}

	public void setCandidate(boolean isCandidate) {
		this.isCandidate = isCandidate;
	}

	public int getEntityQuery() {
		return entityQuery;
	}

	public void setEntityQuery(int entityQuery) {
		this.entityQuery = entityQuery;
	}

	public void setGraphValue(double val) {
		this.score = val;
	}

	public double getScore() {
		return this.score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getDescription() {
		return description;
	}

	void setDescription(String description) {
		this.description = description;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public double getOccurrences() {
		return occurrences;
	}

	public void setOccurrences(int occurrences) {
		this.occurrences = Math.log10(occurrences + 1);
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