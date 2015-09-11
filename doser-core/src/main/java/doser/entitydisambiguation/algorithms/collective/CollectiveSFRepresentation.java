package doser.entitydisambiguation.algorithms.collective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CollectiveSFRepresentation implements
		Comparable<CollectiveSFRepresentation>, Cloneable {

	private int queryNr;

	private String surfaceForm;

	private String context;

	private List<String> candidates;

	private Integer ambiguity;

	private boolean isACandidate;

	private double difference;

	private HashMap<String, Float> localContextCompatibility;

	CollectiveSFRepresentation(String surfaceForm, String context,
			List<String> candidates, int qryNr) {
		super();
		this.ambiguity = candidates.size();
		this.surfaceForm = surfaceForm;
		this.context = context;
		this.candidates = candidates;
		this.queryNr = qryNr;
		this.isACandidate = true;
		this.difference = 0;
		this.localContextCompatibility = new HashMap<String, Float>();
	}

	public void setCandidates(List<String> candidates) {
		this.candidates = candidates;
	}

	public List<String> getCandidates() {
		return candidates;
	}

	public void setACandidate(boolean can) {
		this.isACandidate = can;
	}

	public String getSurfaceForm() {
		return surfaceForm;
	}

	public boolean isACandidate() {
		return isACandidate;
	}

	String getContext() {
		return context;
	}

	int getQueryNr() {
		return queryNr;
	}

	int getAmbiguity() {
		return this.ambiguity;
	}

	public void setDisambiguatedEntity(String url) {
		candidates.clear();
		candidates.add(url);
	}

	public void clearList() {
		candidates.clear();
	}

	public double getDifference() {
		return difference;
	}

	public void setDifference(double difference) {
		this.difference = difference;
	}
	
	public void setLocalContextCompatibility(
			HashMap<String, Float> localContextCompatibility) {
		this.localContextCompatibility = localContextCompatibility;
	}

	public void setCandidateCompatibility(String entity,
			float compatibilityScore) {
		if (this.candidates.contains(entity)) {
			this.localContextCompatibility.put(entity, compatibilityScore);
		}
	}

	public float getLocalContextCompatibility(String entity) {
		if (localContextCompatibility.containsKey(entity)) {
			return localContextCompatibility.get(entity);
		} else {
			return 0f;
		}
	}

	@Override
	public int compareTo(CollectiveSFRepresentation o) {
		// if (this.ambiguity < o.getAmbiguity()) {
		// return -1;
		// } else if (this.ambiguity == o.getAmbiguity()) {
		// if(this.difference < o.getDifference()) {
		// return -1;
		// } else if(this.difference > o.getDifference()) {
		// return 1;
		// } else {
		// return 0;
		// }
		// } else {
		// return 1;
		// }
		if (this.difference < o.getDifference()) {
			return 1;
		} else if (this.difference > o.getDifference()) {
			return -1;
		} else {
			return 0;
		}
	}

	public Object clone() {
		ArrayList<String> newCandidates = new ArrayList<String>();
		for (String s : candidates) {
			newCandidates.add(s);
		}

		CollectiveSFRepresentation n = new CollectiveSFRepresentation(
				new String(this.surfaceForm), new String(this.context),
				newCandidates, this.queryNr);
		n.setACandidate(this.isACandidate);
		HashMap<String, Float> newContextCompatibility = new HashMap<String, Float>();
		newContextCompatibility.putAll(this.localContextCompatibility);
		n.setLocalContextCompatibility(newContextCompatibility);
		
		return n;
	}
}