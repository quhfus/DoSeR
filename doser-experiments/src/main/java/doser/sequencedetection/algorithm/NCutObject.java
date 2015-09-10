package doser.sequencedetection.algorithm;

public interface NCutObject<K extends NCutObject<K>> extends Comparable<K> {

	public double computeSimilarity(K k);

	public int getObjectId();
}
