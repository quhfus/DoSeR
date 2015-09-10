package doser.entitydisambiguation.algorithms.collective;

public class Edge {
	
	private Integer edgeNr;
	
	private Vertex target;
	
	private double transition;
	private Double edgeProbability;
	
	public Edge(Integer edgeNr, Vertex target, double transition) {
		super();
		this.transition = transition;
		this.edgeNr = edgeNr;
		this.target = target;
	}
	
	double getTransition() {
		return transition;
	}
	void setTransition(double transition) {
		this.transition = transition;
	}
	
	void setProbability(double p) {
		this.edgeProbability = new Double(p);
	}
	
	Double getProbability() {
		return this.edgeProbability;
	}
	
	Vertex getTarget() {
		return this.target;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this.edgeNr == ((Edge) obj).edgeNr) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return edgeNr.hashCode();
	}

}
