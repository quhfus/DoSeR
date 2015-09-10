package doser.word2vec.semanticCategories;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import doser.word2vec.Word2VecModel;

public class ComputeSimilarities {

	public ComputeSimilarities() {
		super();
	}

	public void analzye(Word2VecModel m) {
		File file = new File("/home/zwicklbauer/samplingoutput.dat");
		BufferedReader reader = null;
		
		HashMap<Integer, LinkedList<EntityPair>> vals = new HashMap<Integer, LinkedList<EntityPair>>();
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] splitter = line.split("\\t");
				Integer val = Integer.valueOf(splitter[0]);
				if (vals.containsKey(val)) {
					LinkedList<EntityPair> sims = vals.get(val);
					double sim = m.computeSimilarity(splitter[1], splitter[2]);
					EntityPair pair = new EntityPair(splitter[1], splitter[2],
							splitter[3], splitter[4]);
					if (sim > -2) {
						pair.setSim(sim);
						sims.add(pair);
					}
				} else {
					LinkedList<EntityPair> sims = new LinkedList<EntityPair>();
					double sim = m.computeSimilarity(splitter[1], splitter[2]);
					EntityPair pair = new EntityPair(splitter[1], splitter[2],
							splitter[3], splitter[4]);
					if (sim > -2) {
						pair.setSim(sim);
						sims.add(pair);
						vals.put(val, sims);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		File f = new File("/home/zwicklbauer/sampling/distances");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int maxLength = 0;
		for (Map.Entry<Integer, LinkedList<EntityPair>> entry : vals.entrySet()) {
			LinkedList<EntityPair> value = entry.getValue();
			if(maxLength < value.size()) {
				maxLength = value.size();
			}
		}
		
		for (int i = 0; i < maxLength; i++) {
			StringBuilder builder = new StringBuilder();
			for (int j = 0; j < vals.size(); j++) {
				LinkedList<EntityPair> list = vals.get(j);
				if(list.size() > i) {
					builder.append(list.get(i).getSim()+"\t");
				} else {
					builder.append(" \t");
				}
			}
			writer.println(builder.toString());
		}
		
		
//		for (Map.Entry<Integer, HashSet<EntityPair>> entry : vals.entrySet()) {
//			SummaryStatistics stats = new SummaryStatistics();
//			Integer key = entry.getKey();
//			File f = new File("/home/zwicklbauer/sampling/distance" + key);
//			PrintWriter writer = null;
//			try {
//				writer = new PrintWriter(f);
//				HashSet<EntityPair> value = entry.getValue();
//				for (EntityPair pair : value) {
//					writer.println(key + "\t" + pair.getSim() + "\t"
//							+ pair.getCategory1() + "\t" + pair.getCategory2());
//					stats.addValue(pair.getSim());
//				}
//				System.out.println("DISTANCE: " + key + " AVG: "
//						+ stats.getMean() + " StandardDeviation: "
//						+ stats.getStandardDeviation() + " Variance: "
//						+ stats.getVariance() + " Min: " + stats.getMin()
//						+ " Max: " + stats.getMax());
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} finally {
//				if (writer != null) {
//					writer.close();
//				}
//			}
//		}
	}

	public static void main(String[] args) {
		ComputeSimilarities sims = new ComputeSimilarities();
		Word2VecModel model = Word2VecModel
				.createWord2VecModel("/mnt/ssd1/disambiguation/word2vec/wikientitymodel_min5.seq");
		sims.analzye(model);
	}
}
