package doser.webclassify.annotation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.table.logic.Type;
import doser.general.HelpfulMethods;
import doser.language.Languages;
import doser.tools.RDFGraphOperations;
import doser.tools.ServiceQueries;
import doser.webclassify.dpo.WebSite;
import doser.webclassify.dpo.WebTypeRequest_Deprecated;
import doser.webclassify.dpo.WebTypeResponse_Deprecated;

public class AnnotateCategories {

	public static final String TYPEQUERYURL = "http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/webclassify/types";

	public AnnotateCategories() {
		super();
	}

	public void annotateCategories(DisambiguatedEntity entity, Languages lang) {
		Set<Type> types = null;
		if (lang.equals(Languages.german)) {
			types = RDFGraphOperations.getDbpediaCategoriesFromEntity_GER(entity.getEntityUri());
		} else {
			types = RDFGraphOperations.getDbpediaCategoriesFromEntity(entity.getEntityUri());
		}
		entity.setCategories(types);
	}

	@SuppressWarnings("deprecation")
	public void annotateCategory(WebSite website) {
		Set<Type> types = queryWebsiteTypes(website);
	}

	@SuppressWarnings("deprecation")
	private Set<Type> queryWebsiteTypes(WebSite page) {
		Set<Type> pageTypes = new TreeSet<Type>();
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("text", page.getText()));
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
			String resStr = ServiceQueries.httpPostRequest("http://theseus.dimis.fim.uni-passau.de:8061/rest/annotate",
					ent, headers);
			JSONObject resultJSON = null;
			JSONArray entities = null;
			try {
				resultJSON = new JSONObject(resStr);
				entities = resultJSON.getJSONArray("Resources");
				Set<String> entitySet = new HashSet<String>();
				List<String> entityList = new LinkedList<String>();
				for (int i = 0; i < entities.length(); i++) {
					JSONObject obj = entities.getJSONObject(i);
					String e = obj.getString("@URI");
					entitySet.add(e);
					entityList.add(e);
				}
				List<String> testList = new LinkedList<String>();
				testList.addAll(entitySet);
				WebTypeRequest_Deprecated req = new WebTypeRequest_Deprecated();
				req.setEntities(entitySet);
				final ObjectMapper mapper = new ObjectMapper();
				String json = null;
				byte[] jsonByteString = null;
				try {
					json = mapper.writeValueAsString(req);
					jsonByteString = json.getBytes("UTF-8");
				} catch (final JsonParseException e) {
					Logger.getRootLogger().error("Error:", e);
				} catch (final JsonMappingException e1) {
					Logger.getRootLogger().error(e1.getStackTrace());
				} catch (final IOException e2) {
					Logger.getRootLogger().error(e2.getStackTrace());
				}

				Header[] headersTypeQuery = { new BasicHeader("Accept", "application/json"),
						new BasicHeader("content-type", "application/json") };
				ByteArrayEntity bytes = new ByteArrayEntity(jsonByteString, ContentType.create("application/json"));
				resStr = ServiceQueries.httpPostRequest(TYPEQUERYURL, bytes, headersTypeQuery);
				WebTypeResponse_Deprecated response = null;
				try {
					response = mapper.readValue(resStr, WebTypeResponse_Deprecated.class);
				} catch (final JsonParseException e) {
					Logger.getRootLogger().error(e.getStackTrace());
				} catch (final JsonMappingException e1) {
					Logger.getRootLogger().error(e1.getStackTrace());
				} catch (final IOException e2) {
					Logger.getRootLogger().error(e2.getStackTrace());
				}
				Map<String, Set<String>> map = response.getTypes();
				createDistribution(map, testList);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return pageTypes;
	}

	private void createDistribution(final Map<String, Set<String>> map, List<String> entities) {
		Map<String, Integer> distribution = new HashMap<String, Integer>();
		for (String s : entities) {
			Set<String> set = map.get(s);
			for (String str : set) {
				if (distribution.containsKey(str)) {
					int i = distribution.get(str);
					distribution.put(str, ++i);
				} else {
					distribution.put(str, 1);
				}
			}
		}
		List<Map.Entry<String, Integer>> entries = HelpfulMethods.sortByValue(distribution);
		int topK = 0;
		for (Map.Entry<String, Integer> entry : entries) {
			if (topK < 100) {
				System.out.println(entry.getKey() + "\t" + entry.getValue());
			}
			topK++;
		}
	}

}
