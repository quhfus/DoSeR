package doser.entitydisambiguation.algorithms;

class Candidate implements Comparable<Candidate> {

	private String candidate;
	private double score;

	Candidate(String candidate, double score) {
		super();
		this.candidate = candidate;
		this.score = score;
	}

	@Override
	public int compareTo(Candidate o) {
		if (this.score < o.score) {
			return -1;
		} else if (this.score > o.score) {
			return 1;
		} else {
			return 0;
		}
	}

	String getCandidate() {
		return candidate;
	}

	double getScore() {
		return score;
	}
	
	@Override
	public String toString() {
		return candidate;
	}
}