package doser.entitydisambiguation.algorithms.collective;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import doser.entitydisambiguation.properties.Properties;
import doser.tools.ServiceQueries;

public class Word2Vec {

	protected List<CollectiveSFRepresentation> repList;

	protected Map<String, Float> word2vecsimilarities;

	public Word2Vec(List<CollectiveSFRepresentation> rep) {
		super();
		this.repList = rep;
		this.computeWord2VecSimilarities(rep);
	}

	protected float getWord2VecSimilarity(String source, String target) {
		source = source.replaceAll("http://dbpedia.org/resource/", "");
		target = target.replaceAll("http://dbpedia.org/resource/", "");
		int c = source.compareToIgnoreCase(target);
		String res = "";
		if (c < 0) {
			res = source + "|" + target;
		} else if (c == 0) {
			res = source + "|" + target;
		} else {
			res = target + "|" + source;
		}

		float result = 0;
		if (this.word2vecsimilarities.containsKey(res)) {
			result = this.word2vecsimilarities.get(res) + 1.0f;
		}
		return result;
	}

	protected float getWord2VecSimilarity(List<String> source, String target) {
		target = target.replaceAll("http://dbpedia.org/resource/", "");
		Collections.sort(source);
		String res = "";
		for (String s : source) {
			res += s.replaceAll("http://dbpedia.org/resource/", "") + "|";
		}
		res += target;
		float result = 0;
		if (this.word2vecsimilarities.containsKey(res)) {
			result = this.word2vecsimilarities.get(res) + 1.0f;
		}
//		System.out.println("VALUES: "+res+"  "+result);
		return result;
	}

	/**
	 * Basically, this method works but is not as fast as computing word2vec
	 * similarities within Java. However, we apply this method because we do not
	 * have to load the word2vec model in memory at Tomcat startup. Another
	 * advantage is the decoupling of word2vec queries in our Knowledgebase
	 * interface.
	 * 
	 * @param rep
	 */
	private void computeWord2VecSimilarities(
			List<CollectiveSFRepresentation> rep) {
		this.word2vecsimilarities = new HashMap<String, Float>();
		Set<String> entities = new HashSet<String>();
		List<String> multientities = new LinkedList<String>();
		for (CollectiveSFRepresentation r : rep) {
			List<String> l = r.getCandidates();
			for (String s : l) {
				if (l.size() == 1) {
					multientities.add(s.replaceAll(
							"http://dbpedia.org/resource/", ""));
				}
				entities.add(s.replaceAll("http://dbpedia.org/resource/", ""));
			}
		}
		Collections.sort(multientities);
		StringBuilder builder = new StringBuilder();
		for (String s : multientities) {
			builder.append(s + "|");
		}
		String ent = builder.toString();
		if (ent.length() > 0) {
			ent = ent.substring(0, ent.length() - 1);
			entities.add(ent);
		}

		Set<String> combinations = new HashSet<String>();
		for (String e1 : entities) {
			for (String e2 : entities) {
				int nr1 = e1.split("\\|").length;
				int nr2 = e2.split("\\|").length;
				if (nr1 > 1 || nr2 > 1) {
					if (nr1 > 1 && nr1 == 1) {
						combinations.add(e1 + "|" + e2);
					} else if (nr2 > 1 && nr1 == 1) {
						combinations.add(e2 + "|" + e1);
					}
				} else {
					int c = e1.compareToIgnoreCase(e2);
					if (c < 0) {
						combinations.add(e1 + "|" + e2);
					} else if (c == 0) {
						combinations.add(e1 + "|" + e2);
					} else {
						combinations.add(e2 + "|" + e1);
					}
				}
			}
		}
		Word2VecJsonFormat format = new Word2VecJsonFormat();
		format.setData(combinations);
		JSONArray res = queryword2vec(format);
		for (int i = 0; i < res.length(); i++) {
			try {
				JSONObject obj = res.getJSONObject(i);
				String ents = obj.getString("ents");
				float sim = (float) obj.getDouble("sim");
				if(ents.split("\\|").length > 2) {
//					System.out.println(ents + " "+sim);
				}
				this.word2vecsimilarities.put(ents, sim);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private JSONArray queryword2vec(Object json) {
		final ObjectMapper mapper = new ObjectMapper();
		String jsonString = null;
		JSONArray result = null;
		try {
			jsonString = mapper.writeValueAsString(json);
			Header[] headers = { new BasicHeader("Accept", "application/json"),
					new BasicHeader("content-type", "application/json") };
			ByteArrayEntity ent = new ByteArrayEntity(jsonString.getBytes(),
					ContentType.create("application/json"));
			// String resStr = ServiceQueries.httpPostRequest((Properties
			// .getInstance().getWord2VecService() + "w2vsim"), ent,
			// headers);
			String resStr = ServiceQueries.httpPostRequest(
					("http://localhost:5000/" + "w2vsim"), ent, headers);
			JSONObject resultJSON = null;
			try {
				resultJSON = new JSONObject(resStr);
				result = resultJSON.getJSONArray("data");
			} catch (JSONException e) {
				Logger.getRootLogger().error("Error: ", e);
			}
		} catch (IOException e) {
			Logger.getRootLogger().error("Error:", e);
		}
		return result;
	}

	class Word2VecJsonFormat {
		private Set<String> data;

		public Set<String> getData() {
			return data;
		}

		public void setData(Set<String> data) {
			this.data = data;
		}
	}

	public static void main(String[] args) {
		List<String> l = new ArrayList<String>();
		l.add("paris");
		l.add("germany");
		List<String> l1 = new ArrayList<String>();
		l1.add("madrid");
		l1.add("spain");
		List<String> l2 = new ArrayList<String>();
		l2.add("france");
		List<String> l3 = new ArrayList<String>();
		l3.add("taiko");
		CollectiveSFRepresentation rep1 = new CollectiveSFRepresentation(
				"Paris", "Mein White House steht in Washington", l, 0);
		CollectiveSFRepresentation rep2 = new CollectiveSFRepresentation(
				"Madrid", "Mein White House steht in Washington", l1, 1);
		CollectiveSFRepresentation rep3 = new CollectiveSFRepresentation(
				"Madrid", "Mein White House steht in Washington", l2, 2);
		CollectiveSFRepresentation rep4 = new CollectiveSFRepresentation(
				"Madrid", "Mein White House steht in Washington", l3, 4);
		List<CollectiveSFRepresentation> reps = new LinkedList<CollectiveSFRepresentation>();
		reps.add(rep1);
		reps.add(rep2);
		reps.add(rep3);
		reps.add(rep4);
		Word2Vec w2v = new Word2Vec(reps);
		List<String> testlist = new LinkedList<String>();
		testlist.add("france");
		testlist.add("taiko");
		System.out.println(w2v.getWord2VecSimilarity(testlist, "madrid"));
		// System.out.println(w2v.getWord2VecSimilarity("paris", "m1"));
	}
}
