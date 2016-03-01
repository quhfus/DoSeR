package doser.entitydisambiguation.knowledgebases;

import java.util.List;

import org.apache.lucene.search.similarities.Similarity;

public class EntityCentricKBBiomed extends AbstractEntityCentricKBGeneral {

	public EntityCentricKBBiomed(String uri, boolean dynamic, Similarity sim) {
		super(uri, dynamic, sim);
	}

	public EntityCentricKBBiomed(String uri, boolean dynamic) {
		super(uri, dynamic);
	}

	/**
	 * Takes a set of entities as well as a target entity and generates one
	 * string that fits into the word2vec query format used in this class. The
	 * source entities are concatenated and should be compared with the target
	 * entity.
	 *
	 * @param source
	 *            a set of source entities
	 * @param target
	 *            the target entity.
	 * @return String in appropriate word2vec query format
	 */
	public String generateWord2VecFormatString(String source, String target) {
		source = convertUrlToBiomedEntityIdentifier(source);
		target = convertUrlToBiomedEntityIdentifier(target);
		int c = source.compareToIgnoreCase(target);
		String res = "";
		if (c < 0) {
			res = source + "|" + target;
		} else if (c == 0) {
			res = source + "|" + target;
		} else {
			res = target + "|" + source;
		}
		return res;
	}

	/**
	 * Takes a set of entities as well as a target entity and generates one
	 * string that fits into the word2vec query format used in this class. The
	 * source entities are concatenated and should be compared wit the target
	 * entity.
	 *
	 * @param source
	 *            a set of source entities
	 * @param target
	 *            the target entity.
	 * @return String in appropriate word2vec query format
	 */
	public String generateWord2VecFormatString(List<String> source, String target) {
		StringBuilder builder = new StringBuilder();
		for (String s : source) {
			s = convertUrlToBiomedEntityIdentifier(s);
			builder.append(s);
			builder.append("|");
		}
		String src = builder.toString();
		src = src.substring(0, src.length() - 1);
		target = convertUrlToBiomedEntityIdentifier(target);
		return src + "|" + target;
	}

	private String convertUrlToBiomedEntityIdentifier(String url) {
		String res = "";
		if (url.startsWith("http://www.uniprot.org/uniprot/")) {
			res = "UNIPROT_" + url.replaceAll("http://www.uniprot.org/uniprot/", "");
		} else if (url.startsWith("http://www.ncbi.nlm.nih.gov/gene/")) {
			res = "NCBI_" + url.replaceAll("http://www.ncbi.nlm.nih.gov/gene/", "");
		} else if (url.startsWith("http://linkedlifedata.com/resource/umls-concept/")) {
			res = "UMLS_" + url.replaceAll("http://linkedlifedata.com/resource/umls-concept/", "");
		}
		return res;
	}

	@Override
	protected String generateDomainName() {
		return "Biomed";
	}
	
	@Override
	protected String kbName() {
		return "CalbC Biomedical KB";
	}
}
