package doser.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import doser.general.HelpfulMethods;


/**
 * Majority vote methods for arbitrary types
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public final class MajorityVoteAlgorithm<K extends Comparable<? super K>> {

	public MajorityVoteAlgorithm() {
		super();
	}

	public Map.Entry<K, Integer> getMajorityType(final List<K> typeList) {
		final List<Map.Entry<K, Integer>> list = this
				.getMajorityTypes(typeList);
		Map.Entry<K, Integer> res = null;
		if (!list.isEmpty()) {
			res = list.get(0);
		}
		return res;
	}

	public List<Map.Entry<K, Integer>> getMajorityTypes(final List<K> list) {
		final Map<K, Integer> hash = new HashMap<K, Integer>();
		for (final K k : list) {
			if (hash.containsKey(k)) {
				Integer number = hash.get(k);
				hash.put(k, ++number);
			} else {
				hash.put(k, 1);
			}
		}
		return HelpfulMethods.sortByValue(hash);
	}

}
