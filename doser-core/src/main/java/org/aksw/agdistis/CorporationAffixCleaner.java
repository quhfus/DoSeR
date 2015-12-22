package org.aksw.agdistis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;

public class CorporationAffixCleaner {

	HashSet<String> corporationAffixes = new HashSet<String>();

	public CorporationAffixCleaner() throws IOException {
//		Properties prop = new Properties();
//		InputStream input = CorporationAffixCleaner.class.getResourceAsStream("/config/agdistis.properties");
//		prop.load(input);
//		String file = "/home/quh/Arbeitsfläche/Git/AGDISTIS/src/main/resources/config/corporationAffixes.txt";

		loadCorporationAffixes(null);
	}

	private void loadCorporationAffixes(String file) throws IOException {
//		BufferedReader br = new BufferedReader(new InputStreamReader(CorporationAffixCleaner.class.getResourceAsStream(file)));
//		while (br.ready()) {
//			String line = br.readLine();
			corporationAffixes.add("corp");
			corporationAffixes.add("Corp");
			corporationAffixes.add("ltd");
			corporationAffixes.add("Ltd");
			corporationAffixes.add("inc");
			corporationAffixes.add("Inc");
			corporationAffixes.add("co");
			corporationAffixes.add("Co");
//		}
//		br.close();
	}

	String cleanLabelsfromCorporationIdentifier(String label) {
		for (String corporationAffix : corporationAffixes) {
			if (label.endsWith(corporationAffix)) {
				label = label.substring(0, label.lastIndexOf(corporationAffix));
			}
		}
		return label.trim();
	}

}
