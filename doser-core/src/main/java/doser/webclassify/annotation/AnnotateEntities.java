package doser.webclassify.annotation;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import doser.entitydisambiguation.properties.Properties;
import doser.entitydisambiguation.table.logic.Type;
import doser.general.HelpfulMethods;
import doser.language.Languages;
import doser.tools.RDFGraphOperations;
import doser.tools.ServiceQueries;
import doser.webclassify.algorithm.EntityRelevanceAlgorithm;
import doser.webclassify.algorithm.EntitySignificanceAlgorithmPR_W2V;
import doser.webclassify.algorithm.EntitySignificanceAlgorithm_Doc2Vec;
import doser.webclassify.dpo.Paragraph;

public class AnnotateEntities {

	public List<Map.Entry<DisambiguatedEntity, Integer>> createEntityDistributionParagraph(
			Map<DisambiguatedEntity, Integer> map) {
		return HelpfulMethods.sortByValue(map);
	}

	public List<Map.Entry<DisambiguatedEntity, Integer>> createEntityDistributionDocument(Set<Paragraph> paragraphs,
			Languages lang) {
		Map<DisambiguatedEntity, Integer> map = createEntityMap(paragraphs, lang);
		return HelpfulMethods.sortByValue(map);
	}

	public List<DisambiguatedEntity> extractSignificantEntitiesInParagraph(Paragraph p, Languages lang) {
		Set<Paragraph> set = new HashSet<Paragraph>();
		set.add(p);
		Map<DisambiguatedEntity, Integer> map = createEntityMap(set, lang);
		List<DisambiguatedEntity> l = new ArrayList<DisambiguatedEntity>();
		l.add(extractTopicEntity(map, p, lang));
		return l;
	}

	public DisambiguatedEntity extractTopicEntity(Map<DisambiguatedEntity, Integer> map, Paragraph p, Languages lang) {
		EntityRelevanceAlgorithm sig = new EntitySignificanceAlgorithm_Doc2Vec();
		String topicEntityString = sig.process(map, p);
		DisambiguatedEntity topicEntity = null;
		if (!topicEntityString.equalsIgnoreCase("")) {
			topicEntity = new DisambiguatedEntity();
			topicEntity.setEntityUri(topicEntityString);
			topicEntity.setCategories(RDFGraphOperations.getDbpediaCategoriesFromEntity(topicEntityString));
			topicEntity.setType(filterStandardDomain(RDFGraphOperations.getRDFTypesFromEntity(topicEntityString)));
			List<String> labels = null;
			if(lang.equals(Languages.german)) {
				labels = RDFGraphOperations.getDbPediaLabel_GER(topicEntity.getEntityUri());
			} else {
				labels = RDFGraphOperations.getDbPediaLabel(topicEntity.getEntityUri());
			}
			if (labels.size() > 0) {
				topicEntity.setText(labels.get(0));
			}
		}
		return topicEntity;
	}

	public Map<DisambiguatedEntity, Integer> createEntityMap(Set<Paragraph> p, Languages lang) {
		Map<DisambiguatedEntity, Integer> map = new HashMap<DisambiguatedEntity, Integer>();

		for (Paragraph para : p) {
			JSONArray array = queryEntities(para.getContent(), lang);
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					try {
						JSONObject obj = array.getJSONObject(i);
						String uri = obj.getString("@URI");
						String offset = obj.getString("@offset");
						DisambiguatedEntity e = new DisambiguatedEntity();
						e.setEntityUri(uri);
						List<String> labels = null;
						if (lang.equals(Languages.german)) {
							labels = RDFGraphOperations.getDbPediaLabel_GER(uri);
						} else {
							labels = RDFGraphOperations.getDbPediaLabel(uri);
						}
						if (labels.size() > 0) {
							e.setText(labels.get(0));
						}
						if (map.containsKey(e)) {

							// BugFix Issue: Only offset of the first entity is
							// stored, if an entity occurs multiple times in a
							// paragraph
							Set<DisambiguatedEntity> keySet = map.keySet();
							for (DisambiguatedEntity ent : keySet) {
								if (ent.equals(e)) {
									ent.addOffset(Integer.parseInt(offset));
									break;
								}
							}
							Integer amount = map.get(e);
							map.put(e, ++amount);
						} else {
							map.put(e, 1);
						}
						e.addOffset(Integer.parseInt(offset));
						e.setEntityUri(uri);
						e.setType(filterStandardDomain(RDFGraphOperations.getRDFTypesFromEntity(uri)));
					} catch (JSONException e) {
						Logger.getRootLogger().error("Error: ", e);
					}
				}
			}
		}
		return map;
	}

	private JSONArray queryEntities(String text, Languages lang) {
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		Header[] headers = { new BasicHeader("Accept", "application/json") };
		String serviceUrl = "";
		if (lang.equals(Languages.german)) {
			postParameters.add(new BasicNameValuePair("text", text));
			postParameters.add(new BasicNameValuePair("confidence", "0.45"));
			postParameters.add(new BasicNameValuePair("support", "20"));
			serviceUrl = Properties.getInstance().getDBpediaSpotLight_Ger_Rest();
		} else {
			postParameters.add(new BasicNameValuePair("text", text));
			postParameters.add(new BasicNameValuePair("confidence", "0.2"));
			postParameters.add(new BasicNameValuePair("support", "20"));
			serviceUrl = Properties.getInstance().getDBpediaSpotLight_En_Rest();
		}

		UrlEncodedFormEntity ent = null;
		try {
			ent = new UrlEncodedFormEntity(postParameters);
		} catch (UnsupportedEncodingException e1) {
			Logger.getRootLogger().error("Error:", e1);
		}

		if (ent != null) {
			String resStr = ServiceQueries.httpPostRequest(serviceUrl, ent, headers);

			JSONObject resultJSON = null;
			JSONArray entities = null;
			try {
				resultJSON = new JSONObject(resStr);
				entities = resultJSON.getJSONArray("Resources");
			} catch (JSONException e) {
				Logger.getRootLogger().info("No Ressources found");
			}
			return entities;
		}
		return null;
	}

	private String filterStandardDomain(Set<Type> set) {
		String res = "Misc";
		for (Type t : set) {
			if (t.getUri().equalsIgnoreCase("http://dbpedia.org/ontology/Person")) {
				res = "Person";
				break;
			} else if (t.getUri().equalsIgnoreCase("http://dbpedia.org/ontology/Organisation")) {
				res = "Organization";
				break;
			} else if (t.getUri().equalsIgnoreCase("http://www.ontologydesignpatterns.org/ont/d0.owl#Location")) {
				res = "Location";
				break;
			}
		}
		return res;
	}
}
