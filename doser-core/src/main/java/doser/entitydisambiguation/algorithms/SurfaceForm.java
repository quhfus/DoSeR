package doser.entitydisambiguation.algorithms;

import java.util.ArrayList;
import java.util.List;

public class SurfaceForm implements Comparable<SurfaceForm>, Cloneable {

	private int queryNr;
	private String surfaceForm;
	private String context;
	private List<String> candidates;
	private Integer ambiguity;
	private boolean isACandidate;
	private double difference;
	private int position;
	private boolean matchesInitial;
	private boolean initial;
	private boolean isRelevant;

	public SurfaceForm(String surfaceForm, String context, List<String> candidates, int qryNr, int position) {
		super();
		this.ambiguity = candidates.size();
		this.surfaceForm = surfaceForm;
		this.context = context;
		this.candidates = candidates;
		this.queryNr = qryNr;
		this.isACandidate = true;
		this.difference = 0;
		this.position = position;
		this.initial = false;
		this.isRelevant = true;
	}

	public boolean isRelevant() {
		return isRelevant;
	}

	public void setRelevant(boolean isRelevant) {
		this.isRelevant = isRelevant;
	}

	public boolean isMatchesInitial() {
		return matchesInitial;
	}

	public void setMatchesInitial(boolean matchesInitial) {
		this.matchesInitial = matchesInitial;
	}

	public boolean isInitial() {
		return initial;
	}

	public void setInitial(boolean initial) {
		this.initial = initial;
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

	public String getContext() {
		return context;
	}

	public int getQueryNr() {
		return queryNr;
	}

	public int getAmbiguity() {
		return this.ambiguity;
	}

	public void setDisambiguatedEntity(String url) {
		candidates.clear();
		candidates.add(url);
	}

	public void clearList() {
		candidates.clear();
	}

	public void addCandidate(String can) {
		candidates.add(can);
	}

	public double getDifference() {
		return difference;
	}

	public void setDifference(double difference) {
		this.difference = difference;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public int compareTo(SurfaceForm o) {
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

		SurfaceForm n = new SurfaceForm(new String(this.surfaceForm), new String(this.context), newCandidates,
				this.queryNr, this.position);
		n.setACandidate(this.isACandidate);
		n.setInitial(this.initial);
		n.setMatchesInitial(this.matchesInitial);
		n.setRelevant(this.isRelevant);
		return n;
	}
}