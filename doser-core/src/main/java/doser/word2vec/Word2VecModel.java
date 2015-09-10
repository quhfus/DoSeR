package doser.word2vec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

//import org.apache.log4j.Logger;

@Deprecated
public class Word2VecModel {

	private int size;

	private HashMap<String, Integer> vocab;

	private float[][] vec;

	public Word2VecModel(float[][] vec, HashMap<String, Integer> vocab) {
		super();
		this.vec = vec;
		this.vocab = vocab;
		this.size = vec[0].length;

		System.out.println("ICH CHECK AUF INFINITY");
		
		for (Map.Entry<String, Integer> entry : this.vocab.entrySet()) {
			String key = entry.getKey();
			Integer position = entry.getValue();
			for (int i = 0; i < size; i++) {
				if (Float.isInfinite(this.vec[position][i])) {
					System.out.println("ganz oben "+key);
				}
			}
		}
		
		System.out.println(vec.length);
		for (int i = 0; i < vec.length; i++) {
			if(i == 179455) {
				float vectest[] = vec[i];
				for (int j = 0; j < vectest.length; j++) {
					System.out.println("Vec Value"+vectest[j]);
				}
			}
			for (int j = 0; j < vec[i].length; j++) {
				if (Float.isInfinite(this.vec[i][j])) {
					System.out.println("Vektor Position "+i);
				}
			}
		}
		
	}

	public float computeSimilarity(String s1, String s2) {
		s1 = s1.replaceAll("http://dbpedia.org/resource/", "");
		s2 = s2.replaceAll("http://dbpedia.org/resource/", "");
		float similarity = -2;
		if (vocab.containsKey(s1) && vocab.containsKey(s2)) {
			int pos1 = vocab.get(s1);
			int pos2 = vocab.get(s2);
			float[] vec1 = vec[pos1];
			float[] vec2 = vec[pos2];

			float sum = 0;
			for (int i = 0; i < size; i++) {
				sum += vec1[i] * vec2[i];
				
			}
			similarity = sum;
		}
		return similarity;
	}

	public float computeSimilarity(String[] words, String s2) {
		String comp = s2.replaceAll("http://dbpedia.org/resource/", "");
		float similarity = -2;
		if (vocab.containsKey(comp)) {
			Stack<String> l = new Stack<String>();
			for(int i = 0; i < words.length; i++) {
				String w = words[i].replaceAll("http://dbpedia.org/resource/", "");
				if(vocab.containsKey(w)) {
					l.push(w);
				}
			}
					
			if(!l.isEmpty()) {
				float[] vector = vec[vocab.get(l.pop())];
				float[] basis = new float[vector.length];
				for (int i = 0; i < basis.length; i++) {
					basis[i] = vector[i];
				}
				while(!l.isEmpty()) {
					String s = l.pop();
					float[] addVector = vec[vocab.get(s)];
					for(int i = 0; i < basis.length; i++) {
						if(Float.isInfinite(addVector[i])) {
							System.out.println("Hier steht in einem AddVector Unednlich drinnen"+vocab.get(s));
						}
						basis[i] += addVector[i];
					}
				}
				
				float[] vec2 = vec[vocab.get(comp)];
				float sum = 0;
				float length_a = 0;
				for (int i = 0; i < size; i++) {
					sum += basis[i] * vec2[i];
					length_a += (basis[i] * basis[i]);
				}
				length_a = (float) Math.sqrt(length_a);

				similarity = sum / length_a;
			}
		}
		return similarity;
	}

	public float[] computeCentroid(String[] words) {
		float[] cen = new float[size];
		int points = 0;
		for (int i = 0; i < words.length; i++) {
			if (vocab.containsKey(words[i])) {
				points++;
				int pos = vocab.get(words[i]);
				for (int j = 0; j < cen.length; j++) {
					cen[j] += vec[pos][j];
				}
			}
		}
		if (points > 0) {
			for (int i = 0; i < cen.length; i++)
				cen[i] /= (float) points;
			return cen;
		} else
			return null;
	}

	public float computeMSE(String[] words) {
		float[] centroid = computeCentroid(words);
		if (centroid != null) {
			float[] sum = new float[size];
			int points = 0;
			for (int i = 0; i < words.length; i++) {
				if (vocab.containsKey(words[i])) {
					points++;
					int pos = vocab.get(words[i]);
					for (int j = 0; j < sum.length; j++) {
						sum[j] += (float) Math.pow((centroid[j] - vec[pos][j]),
								2);
					}
				}
			}
			float abs = 0;
			for (int i = 0; i < sum.length; i++) {
				abs += Math.pow((sum[i] / (float) points), 2);
			}
			return (float) Math.sqrt(abs);
		} else
			return -2;
	}

	public void evaluateNegativeValues(String str) {
		int pos = vocab.get(str);
		float neg = 1;
		for (Map.Entry<String, Integer> entry : vocab.entrySet()) {
			int value = entry.getValue();
			float[] vec1 = vec[pos];
			float[] vec2 = vec[value];

			float sum = 0;
			float length_a = 0;
			float length_b = 0;
			for (int i = 0; i < size; i++) {
				sum += vec1[i] * vec2[i];
				length_a += (vec1[i] * vec1[i]);
				length_b += (vec2[i] * vec2[i]);
			}
			length_a = (float) Math.sqrt(length_a);

			length_b = (float) Math.sqrt(length_b);
			float similarity = sum / length_a * length_b;
			if (similarity < neg) {
				neg = similarity;
			}
		}
	}

	public static Word2VecModel createWord2VecModel(String path) {
		File file = new File(path);
		if (!file.isFile()) {
//			Logger.getRootLogger().error(
//					"Please enter a valid Word2Vec model path and retry!");
			return null;
		}
		Scanner sc = null;
		try {
			sc = new Scanner(new BufferedReader(new FileReader(file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		HashMap<String, Integer> vocab = new HashMap<String, Integer>();
		float[][] vec = null;
		int words = -1;
		int size = -1;
		if (sc != null) {
			words = sc.nextInt();
			size = sc.nextInt();

			vec = new float[words][size];

			for (int b = 0; b < words; b++) {
				String token = sc.next();
				vocab.put(token, new Integer(b));

				int a = 0;
				while (a < size) {
					if (!sc.hasNext()) {
						break;
					}
					float val = Float.valueOf(sc.next());
					vec[b][a] = val;
					a++;
				}
				float len = 0;
				for (a = 0; a < size; a++)
					len += vec[b][a] * vec[b][a];
				len = (float) Math.sqrt(len);
				for (a = 0; a < size; a++) {
					vec[b][a] /= len;
					if (Float.isInfinite(vec[b][a])) {
						System.out.println(len);
					}
				}
			}
		}
		if (sc != null) {
			sc.close();
		}
		return new Word2VecModel(vec, vocab);
	}

	public static void main(String args[]) {
		Word2VecModel model = Word2VecModel
				.createWord2VecModel("/home/quh/Arbeitsfläche/Word2vec/model.seq");
		String[] vector = { "belgium", "spain", "france" };
		String s2 = "black";
		System.out.println(model.computeSimilarity(vector, s2));
	}
}
