package Agdistis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.aksw.agdistis.util.Triple;
import org.aksw.agdistis.util.TripleIndex;

public class DomainWhiteLister {
	private TripleIndex index;
	HashSet<String> whiteList = new HashSet<String>();

	public DomainWhiteLister(TripleIndex index) throws IOException {
//		Properties prop = new Properties();
//		InputStream input =DomainWhiteLister.class.getResourceAsStream("/config/agdistis.properties");
//		prop.load(input);
		String file = "test";

		loadWhiteDomains(file);

		this.index = index;
	}

	private void loadWhiteDomains(String file) throws IOException {
//		BufferedReader br = new BufferedReader(new InputStreamReader(DomainWhiteLister.class.getResourceAsStream(file)));
//		while (br.ready()) {
//			String line = br.readLine();
			whiteList.add("http://dbpedia.org/ontology/Place");
			whiteList.add("http://dbpedia.org/ontology/Person");
			whiteList.add("http://dbpedia.org/ontology/Organisation");
			whiteList.add("http://dbpedia.org/class/yago/YagoGeoEntity");
			whiteList.add("http://xmlns.com/foaf/0.1/Person");
			whiteList.add("http://dbpedia.org/ontology/WrittenWork");
//		}
//		br.close();
	}

	public boolean fitsIntoDomain(String candidateURL) {
		List<Triple> tmp = index.search(candidateURL, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", null);
		if (tmp.isEmpty())
			return true;
		for (Triple triple : tmp) {
			if (!triple.getObject().contains("wordnet") && !triple.getObject().contains("wikicategory"))
				if (whiteList.contains(triple.getObject())) {
					return true;
				}
		}
		return false;
	}
}
