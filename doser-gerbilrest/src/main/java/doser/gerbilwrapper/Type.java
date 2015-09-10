package doser.gerbilwrapper;

import org.apache.log4j.Logger;

public class Type implements Comparable<Type>, Cloneable {

	private double accumulatedWeight;

	private boolean correct;

	private boolean relevant;

	private final boolean btype;

	private final int layer;

	private String name;

	private String uri;

	private double weightedScore;

	public Type(final String name, final String uri, final boolean type,
			final int layer) {
		super();
		this.correct = false;
		if (name == null) {
			this.name = "";
		} else {
			this.name = name;
		}

		if (uri == null) {
			this.uri = "";
		} else {
			this.uri = uri;
		}
		this.btype = type;
		this.accumulatedWeight = 0;
		this.relevant = true;
		this.layer = layer;
	}

	@Override
	public Type clone() throws CloneNotSupportedException {
		Type type = null;
		try {
			type = (Type) super.clone();
		} catch (final CloneNotSupportedException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return type;
	}

	@Override
	public int compareTo(final Type type) {
		if (this.uri.equals(type.getUri())
				&& this.name.equalsIgnoreCase(type.getName())) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public boolean equals(final Object obj) {
		final Type type = (Type) obj;
		if (type.getUri().equalsIgnoreCase(this.getUri())
				&& type.getName().equalsIgnoreCase(this.getName())) {
			return true;
		}
		return false;
	}

	public double getAccumulatedWeight() {
		return this.accumulatedWeight;
	}

	public int getLayer() {
		return this.layer;
	}

	public String getName() {
		return this.name;
	}

	public String getUri() {
		return this.uri;
	}

	public double getWeightedScore() {
		return this.weightedScore;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode() + this.uri.hashCode();
	}

	public boolean isCorrect() {
		return this.correct;
	}

	public boolean isRelevant() {
		return this.relevant;
	}

	public boolean isType() {
		return this.btype;
	}

	public void setAccumulatedWeight(final double accumulatedWeight) {
		this.accumulatedWeight = accumulatedWeight;
	}

	public void setCorrect() {
		this.correct = true;
	}

	public void setRelevant(final boolean isRelevant) {
		this.relevant = isRelevant;
	}

	public void setWeightedScore(final double weightedScore) {
		this.weightedScore = weightedScore;
	}
}
