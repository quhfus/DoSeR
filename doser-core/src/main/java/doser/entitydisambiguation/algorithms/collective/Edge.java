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
	
	public double getTransition() {
		return transition;
	}
	public void setTransition(double transition) {
		this.transition = transition;
	}
	
	public void setProbability(double p) {
		this.edgeProbability = new Double(p);
	}
	
	public Double getProbability() {
		return this.edgeProbability;
	}
	
	public Vertex getTarget() {
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
