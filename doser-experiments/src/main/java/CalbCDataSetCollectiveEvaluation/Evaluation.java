package CalbCDataSetCollectiveEvaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

import DisambiguationApproachDPO.DisambiguatedEntity;
import DisambiguationApproachDPO.DisambiguationRequest;
import DisambiguationApproachDPO.DisambiguationResponse;
import DisambiguationApproachDPO.EntityDisambiguationDPO;
import DisambiguationApproachDPO.Response;
import experiments.collective.entdoccentric.calbc.CalbCPubMedID;
import experiments.collective.entdoccentric.calbc.Concept;
import experiments.collective.entdoccentric.calbc.Entity;
import experiments.evaluation.UnicodeBOMInputStream;

public class Evaluation {
	private static final String DISAMBIGUATIONSERVICE = "http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/disambiguation/disambiguationWithoutCategories-collective";
	private static final String CALBCFILE = "";

	private Gson gson;
	private BufferedReader reader;

	Evaluation() {
		File jsonFile = new File(CALBCFILE);
		this.gson = new Gson();
		try {
			FileInputStream fis = new FileInputStream(jsonFile);
			UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(fis);
			InputStreamReader isr = new InputStreamReader(ubis);
			reader = new BufferedReader(isr);
			ubis.skipBOM();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void evaluate() {
		String line = null;
		String jsonString = "";
		CalbCPubMedID entry = null;
		try {
			while ((line = reader.readLine()) != null) {
				if (!line.equalsIgnoreCase("")) {
					jsonString += line;
				} else {
					break;
				}
			}
			entry = gson.fromJson(jsonString, CalbCPubMedID.class);
			if (entry == null) {
				return;
			}
			if (entry.getAbs() != null && !entry.getAbs().equalsIgnoreCase("")) {
				// Transform entry into possible Disambiguation task
				DisambiguationRequest req = new DisambiguationRequest();
				req.setDocsToReturn(10);
				req.setDocumentUri("CalbCSmall");
				List<EntityDisambiguationDPO> l = new ArrayList<EntityDisambiguationDPO>();
				List<Entity> entityList = entry.getEntityList();
				for (Entity e : entityList) {
					if (!e.isTitle()) {
						EntityDisambiguationDPO dpo = new EntityDisambiguationDPO();
						dpo.setContext(entry.getAbs());
						dpo.setDocumentId("CalbCSmall");
						dpo.setSelectedText(e.getKeyword());
						dpo.setStartPosition(e.getPosition());
						l.add(dpo);
					}
				}
				req.setSurfaceFormsToDisambiguate(l);
				List<Response> responses = queryService(req);
				int counter = 0;
				for (Entity e : entityList) {
					if (!e.isTitle()) {
						Response res = responses.get(counter);
						List<DisambiguatedEntity> disEntities = res.getDisEntities();
						isInGroundtruth(e.getConceptList(), disEntities.get(0).getEntityUri());
						counter++;
					}
				}
			}
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
	}

	private boolean isInGroundtruth(List<Concept> conceptList, String dis) {
		for (Concept c : conceptList) {
			System.out.println(c.getUrl());
		}
		System.out.println("DisambiguatedEntity: " + dis);
		return true;
	}

	private List<Response> queryService(DisambiguationRequest req) {
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
		ByteArrayEntity ent = new ByteArrayEntity(json.getBytes(), ContentType.create("application/json"));
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
		DisambiguationResponse disResponse = gson.fromJson(buffer.toString(), DisambiguationResponse.class);
		return disResponse.getTasks();
	}

	public static void main(String[] args) {
		Evaluation eval = new Evaluation();
		eval.evaluate();
	}

}
