package doser.entitydisambiguation.knowledgebases;

import java.util.List;

import org.apache.lucene.search.similarities.Similarity;

public class EntityCentricKBDBpedia extends AbstractEntityCentricKBGeneral {

	public EntityCentricKBDBpedia(String uri, boolean dynamic) {
		super(uri, dynamic);
	}

	public EntityCentricKBDBpedia(String uri, boolean dynamic, Similarity sim) {
		super(uri, dynamic, sim);
	}

	/**
	 * Takes a set of dbpedia entities as well as a target entity and generates
	 * one string that fits into the word2vec query format used in this class.
	 * The source entities are concatenated and should be compared with the
	 * target entity.
	 *
	 * @param source
	 *            a set of source entities
	 * @param target
	 *            the target entity.
	 * @return String in appropriate word2vec query format
	 */
	@Override
	public String generateWord2VecFormatString(String source, String target) {
		String s = source.replaceAll("http://dbpedia.org/resource/", "");
		String t = target.replaceAll("http://dbpedia.org/resource/", "");
		int c = s.compareToIgnoreCase(target);
		String res = "";
		if (c < 0) {
			res = s + "|" + t;
		} else if (c == 0) {
			res = s + "|" + t;
		} else {
			res = t + "|" + s;
		}
		return res;
	}

	/**
	 * Takes a set of dbpedia entities as well as a target entity and generates
	 * one string that fits into the word2vec query format used in this class.
	 * The source entities are concatenated and should be compared with the
	 * target entity.
	 *
	 * @param source
	 *            a set of source entities
	 * @param target
	 *            the target entity.
	 * @return String in appropriate word2vec query format
	 */
	@Override
	public String generateWord2VecFormatString(List<String> source, String target) {
		StringBuilder builder = new StringBuilder();
		for (String s : source) {
			s = s.replaceAll("http://dbpedia.org/resource/", "");
			builder.append(s);
			builder.append("|");
		}
		String src = builder.toString();
		src = src.substring(0, src.length() - 1);
		String t = target.replaceAll("http://dbpedia.org/resource/", "");
		return src + "|" + t;
	}
	
	@Override
	protected String generateDomainName() {
		return "DBpedia";
	}
	
	@Override
	protected String kbName() {
		return "DBpedia KB";
	}
}
