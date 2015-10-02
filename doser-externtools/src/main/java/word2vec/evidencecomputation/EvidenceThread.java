package word2vec.evidencecomputation;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import doser.word2vec.Word2VecModel;

@SuppressWarnings("deprecation")
public class EvidenceThread implements Runnable {

	private static final int TOPN = 100;

	private List<String> entities;
	private Word2VecModel model;
	private PrintWriter writer;

	public EvidenceThread(Word2VecModel model, List<String> entities,
			PrintWriter writer) {
		this.entities = entities;
		this.model = model;
		this.writer = writer;
	}

	@Override
	public void run() {
		float[][] vec = model.getVec();
		HashMap<String, Integer> map = model.getVocab();
		for (String entity : entities) {
			PriorityQueue<Holder> topN = new PriorityQueue<Holder>(TOPN,
					new Comparator<Holder>() {
						@Override
						public int compare(Holder h1, Holder h2) {
							return Float.compare(h1.getScore(), h2.getScore());
						}
					});

			float[] entityVec = vec[map.get(entity)];
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				String word = entry.getKey();
				if (!word.contains("http://dbpedia.org/resource/")) {
					float[] vector = vec[entry.getValue()];
					float similarity = computeSimilarity(entityVec, vector);
					if (topN.size() < TOPN) {
						topN.add(new Holder(word, similarity));
					} else if (topN.peek().score < similarity) {
						topN.poll();
						topN.add(new Holder(word, similarity));
					}
				}
			}
			String line = "";
			while(!topN.isEmpty()) {
				Holder h = topN.poll();
				String addition = "(" + h.word + "," + h.score + ")";
				if (topN.size() > 0) {
					addition = "," + addition;
				}
				line = addition + line;
			}
			line = entity + "\t" + line;
			writer.println(line);
		}
	}

	private float computeSimilarity(float[] vec1, float[] vec2) {
		float sum = 0;
		for (int i = 0; i < vec1.length; i++) {
			sum += vec1[i] * vec2[i];
		}
		return sum;
	}

	class Holder {
		private String word;
		private float score;

		public Holder(String entity, float score) {
			super();
			this.word = entity;
			this.score = score;
		}

		public String getEntity() {
			return word;
		}

		public void setEntity(String entity) {
			this.word = entity;
		}

		public float getScore() {
			return score;
		}

		public void setScore(float score) {
			this.score = score;
		}
	}
}
