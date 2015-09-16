package doser.webclassify.annotation;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.table.logic.Type;
import doser.general.HelpfulMethods;
import doser.tools.RDFGraphOperations;
import doser.tools.ServiceQueries;
import doser.webclassify.algorithm.EntitySignificanceAlgorithmPR_W2V;
import doser.webclassify.dpo.Paragraph;

public class AnnotateEntities {

	public static final String RESTDBPEDIASPOTLIGHT = "http://theseus.dimis.fim.uni-passau.de:8061/rest/annotate";
	
	public List<Map.Entry<DisambiguatedEntity, Integer>> createEntityDistributionParagraph(Map<DisambiguatedEntity, Integer> map) {
		return HelpfulMethods.sortByValue(map);
	}

	public List<Map.Entry<DisambiguatedEntity, Integer>> createEntityDistributionDocument(
			Set<Paragraph> paragraphs) {
		Map<DisambiguatedEntity, Integer> map = createEntityMap(paragraphs);
		return HelpfulMethods.sortByValue(map);
	}

	public List<DisambiguatedEntity> extractSignificantEntitiesInParagraph(
			Paragraph p) {
		Set<Paragraph> set = new HashSet<Paragraph>();
		set.add(p);
		Map<DisambiguatedEntity, Integer> map = createEntityMap(set);
		List<DisambiguatedEntity> l = new ArrayList<DisambiguatedEntity>();
		l.add(extractTopicEntity(map));
		return l;
	}
	
	public DisambiguatedEntity extractTopicEntity(Map<DisambiguatedEntity, Integer> map) {
		EntitySignificanceAlgorithmPR_W2V sig = new EntitySignificanceAlgorithmPR_W2V();
		DisambiguatedEntity topicEntity = new DisambiguatedEntity();
		topicEntity.setEntityUri(sig.process(map));
		return topicEntity;
	}

	public Map<DisambiguatedEntity, Integer> createEntityMap(Set<Paragraph> p) {
		Map<DisambiguatedEntity, Integer> map = new HashMap<DisambiguatedEntity, Integer>();

		for (Paragraph para : p) {
			JSONArray array = queryEntities(para.getContent());
			if (array != null) {
				List<DisambiguatedEntity> entityList = new LinkedList<DisambiguatedEntity>();
				for (int i = 0; i < array.length(); i++) {
					try {
						JSONObject obj = array.getJSONObject(i);
						String uri = obj.getString("@URI");
						DisambiguatedEntity e = new DisambiguatedEntity();
						e.setEntityUri(uri);
						List<String> labels = RDFGraphOperations
								.getDbPediaLabel(uri);
						if (labels.size() > 0) {
							e.setText(labels.get(0));
						}
						if (map.containsKey(e)) {
							Integer amount = map.get(e);
							map.put(e, ++amount);
						} else {
							map.put(e, 1);
						}
						e.setEntityUri(uri);
						e.setType(filterStandardDomain(RDFGraphOperations
								.getRDFTypesFromEntity(uri)));
						entityList.add(e);
						// Add Entity Class Type
					} catch (JSONException e) {
						Logger.getRootLogger().error("Error: ", e);
					}
				}
			}
		}
		return map;
	}

	private JSONArray queryEntities(String text) {
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("text", text));
		postParameters.add(new BasicNameValuePair("confidence", "0.2"));
		postParameters.add(new BasicNameValuePair("support", "20"));
		UrlEncodedFormEntity ent = null;
		try {
			ent = new UrlEncodedFormEntity(postParameters);
		} catch (UnsupportedEncodingException e1) {
			Logger.getRootLogger().error("Error:", e1);
		}
		Header[] headers = { new BasicHeader("Accept", "application/json") };
		if (ent != null) {
			String resStr = ServiceQueries.httpPostRequest(
					RESTDBPEDIASPOTLIGHT, ent, headers);
			JSONObject resultJSON = null;
			JSONArray entities = null;
			try {
				resultJSON = new JSONObject(resStr);
				entities = resultJSON.getJSONArray("Resources");
			} catch (JSONException e) {
				Logger.getRootLogger().error("Error: ", e);
			}
			return entities;
		}
		return null;
	}

	private String filterStandardDomain(Set<Type> set) {
		String res = new String();
		for (Type t : set) {
			if (t.getUri().equalsIgnoreCase(
					"http://dbpedia.org/ontology/Person")) {
				res = "Person";
				break;
			} else if (t.getUri().equalsIgnoreCase(
					"http://dbpedia.org/ontology/Organisation")) {
				res = "Organisation";
				break;
			} else if (t
					.getUri()
					.equalsIgnoreCase(
							"http://www.ontologydesignpatterns.org/ont/d0.owl#Location")) {
				res = "Location";
				break;
			} else {
				res = "Misc";
			}
		}
		return res;
	}
}
