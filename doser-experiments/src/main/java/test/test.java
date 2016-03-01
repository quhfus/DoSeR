package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import DisambiguationApproachDPO.DisambiguationRequest;
import DisambiguationApproachDPO.DisambiguationResponse;
import DisambiguationApproachDPO.EntityDisambiguationDPO;
import DisambiguationApproachDPO.Response;

public class test {

	public static final String DISAMBIGUATIONSERVICE = "http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/disambiguation/disambiguationWithoutCategoriesBiomed-collective";
	
	test() {
		super();
		DisambiguationRequest req = new DisambiguationRequest();
		req.setDocsToReturn(10);
		req.setDocumentUri("Test");
		List<EntityDisambiguationDPO> dpos = new ArrayList<EntityDisambiguationDPO>();
		EntityDisambiguationDPO dpo = new EntityDisambiguationDPO();
		dpo.setContext("");
		dpo.setSelectedText("Influenza");
		dpo.setStartPosition(0);
		dpo.setDocumentId("Test0");
		dpos.add(dpo);
		req.setSurfaceFormsToDisambiguate(dpos);
		sendAction(req);
		
	}
	
	private void sendAction(DisambiguationRequest req) {
		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams, 3000);
		HttpConnectionParams.setSoTimeout(my_httpParams, 0);
		DefaultHttpClient httpclient = new DefaultHttpClient(my_httpParams);
		HttpPost httppost = new HttpPost(DISAMBIGUATIONSERVICE);
		Header[] headers = { new BasicHeader("Accept", "application/json"),
				new BasicHeader("content-type", "application/json") };

		httppost.setHeaders(headers);
		Gson gson = new Gson();
		String json = null;
		json = gson.toJson(req);
		ByteArrayEntity ent = new ByteArrayEntity(json.getBytes(),
				ContentType.create("application/json"));
		httppost.setEntity(ent);

		HttpResponse response;
		StringBuffer buffer = new StringBuffer();
		try {
			response = httpclient.execute(httppost);
			HttpEntity httpent = response.getEntity();
			buffer.append(EntityUtils.toString(httpent));
		} catch (ClientProtocolException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		System.out.println(buffer.toString());
		DisambiguationResponse disResponse = gson.fromJson(
				buffer.toString(), DisambiguationResponse.class);
		List<Response> responses = disResponse.getTasks();
	}
	
	
	public static void main(String[] args) {
		test t = new test();
	}
	
}
