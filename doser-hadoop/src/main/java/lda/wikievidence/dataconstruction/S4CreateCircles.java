package lda.wikievidence.dataconstruction;

import hbase.operations.HBaseOperations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class S4CreateCircles {

	public static final int MAXDEPTH = 1;
	public static final int MAXSFTOENTS = 50;

	public void processMain(String f, String outputFile) {
		File file = new File(f);
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(new File(
					outputFile), true));

			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			int processed = 0;
			while ((line = reader.readLine()) != null) {
				String splitter[] = line.split("\\t");
				String entityName = splitter[0];
				HashSet<String> circleEntities = new HashSet<String>();
				circleEntities.add(entityName);
				discoverNewEntities(entityName, circleEntities, 0);
				List<String> list = new ArrayList<String>(circleEntities);
				sortList(list);
				StringBuffer buffer = new StringBuffer();
				for (String s : list) {
					buffer.append(s + "|");
				}
				String out = buffer.toString();
				out = out.substring(0, out.length() - 1);
				writer.println(out);
				// if (list.size() > 1) {
				// System.out.println(out);
				// }

				processed++;
				if (processed % 150 == 0) {
					System.out.println("processed: " + processed);
				}
			}
			reader.close();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void sortList(List<String> aItems) {
		Collections.sort(aItems);
	}

	private void discoverNewEntities(String entity,
			HashSet<String> discoveredEntities, int depth) throws IOException {
		if (depth >= MAXDEPTH) {
			return;
		}
		boolean discovery = false;
		Set<String> sfs = new HashSet<String>();
		HBaseOperations.getInstance().getRow("LDADC_EntToSf", entity, "data", sfs, -1);
		// ArrayList<String> sfs = HBaseOperations.getRow("input", entity,
		// "data");
		for (String s : sfs) {
			Set<String> ents = new HashSet<String>();
			HBaseOperations.getInstance().getRow("LDADC_SFToEnt", s, "data", ents, -1);
			// ArrayList<String> ents = HBaseOperations
			// .getRow("output", s, "data");
			// for (String sf : ents) {
			// System.out.println("HBASE LIEFERT MIR DANACH: " + sf);
			// }
			if (ents.size() < MAXSFTOENTS) {
				for (String str : ents) {
					if (!discoveredEntities.contains(str)) {
						discoveredEntities.add(str);
						discovery = true;
					}
					if (discovery) {
						int i = depth + 1;
						discoverNewEntities(str, discoveredEntities, i);
						discovery = false;
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		S4CreateCircles s4 = new S4CreateCircles();
		s4.processMain(args[0], args[1]);
	}

}
