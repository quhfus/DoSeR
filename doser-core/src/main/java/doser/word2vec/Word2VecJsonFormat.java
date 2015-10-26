package doser.word2vec;

import java.io.IOException;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import doser.entitydisambiguation.properties.Properties;
import doser.tools.ServiceQueries;

public class Word2VecJsonFormat {

	private Set<String> data;

	public Set<String> getData() {
		return data;
	}

	public void setData(Set<String> data) {
		this.data = data;
	}
	
	public static JSONArray performquery(Object json, String serviceEndpoint) {
		final ObjectMapper mapper = new ObjectMapper();
		String jsonString = null;
		JSONArray result = null;
		try {
			jsonString = mapper.writeValueAsString(json);
			Header[] headers = { new BasicHeader("Accept", "application/json"),
					new BasicHeader("content-type", "application/json") };
			ByteArrayEntity ent = new ByteArrayEntity(jsonString.getBytes(),
					ContentType.create("application/json"));
			String resStr = ServiceQueries.httpPostRequest(
					(Properties.getInstance().getWord2VecService() + serviceEndpoint), ent, headers);
			JSONObject resultJSON = null;
			try {
				resultJSON = new JSONObject(resStr);
				result = resultJSON.getJSONArray("data");
			} catch (JSONException e) {
				System.out.println(e);
//				Logger.getRootLogger().error("Error: ", e);
			}
		} catch (IOException e) {
			System.out.println(e);
//			Logger.getRootLogger().error("Error:", e);
		}
		return result;
	}
}
