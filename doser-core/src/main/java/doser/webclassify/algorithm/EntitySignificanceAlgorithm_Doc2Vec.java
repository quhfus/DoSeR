package doser.webclassify.algorithm;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.webclassify.dpo.Paragraph;
import doser.word2vec.Data;
import doser.word2vec.Doc2VecJsonFormat;
import doser.word2vec.Word2VecJsonFormat;

public class EntitySignificanceAlgorithm_Doc2Vec implements
		EntityRelevanceAlgorithm {

	@Override
	public String process(Map<DisambiguatedEntity, Integer> map, Paragraph p) {
		List<String> entities = new LinkedList<String>();
		for (Map.Entry<DisambiguatedEntity, Integer> entry : map.entrySet()) {
			entities.add(entry.getKey().getEntityUri());
		}
		return computeBestSingleTopic(entities, p.getContent());
	}

	private String computeBestSingleTopic(List<String> entities, String context) {
		Doc2VecJsonFormat format = new Doc2VecJsonFormat();
		context = context.toLowerCase();
		context = context.replaceAll("[\\.\\,\\!\\? ]+", " ");
		String[] candidates = new String[entities.size()];
		candidates = entities.toArray(candidates);

		Data doc = new Data();
		doc.setCandidates(candidates);
		doc.setQryNr("0");
		doc.setContext(context);
		doc.setSurfaceForm("");
		format.addData(doc);

		float max = 0;
		int pos = 0;
		try {
			JSONArray res = Word2VecJsonFormat.performquery(format, "d2vsim");
			JSONObject obj = res.getJSONObject(0);
			JSONArray simArray = obj.getJSONArray("sim");

			System.out.println("Doc2vec : ");

			for (int j = 0; j < simArray.length(); j++) {
				float sim = (float) simArray.getDouble(j);
				if (sim > max) {
					pos = j;
					max = sim;
				}
				System.out.println("Entity: " + entities.get(j) + " sim: "
						+ sim);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (entities.size() == 0) {
			return "";
		} else {
			return entities.get(pos);
		}
	}
}
