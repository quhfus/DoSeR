package AidaDatasetEvaluation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
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

public class AidaDataSetEvaluation {

	public static final String DISAMBIGUATIONSERVICE = "http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/disambiguation/disambiguationWithoutCategories-collective";
	
	private int overall;
	
	private int correct;
	
	public AidaDataSetEvaluation() {
		super();
		this.overall = 0;
		this.correct = 0;
	}
	
	public static void main(String[] args) throws IOException {
		AidaDataSetEvaluation eval = new AidaDataSetEvaluation();
		eval.action();
	}
	
	public void action() throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(new File("/home/quh/Arbeitsfläche/Disambiguation/Datasets/AIDA-YAGO2-dataset.tsv")));
		String line = null;
		List<String> list = new LinkedList<String>();
		List<String> gt = new LinkedList<String>();
		int count = 0;
		boolean isOpen = false;
		while((line = reader.readLine()) != null) {
			if(line.contains("-DOCSTART-") && line.contains("testa")) {
				if(!list.isEmpty() && !gt.isEmpty() && isOpen) {
					Document doc = new Document(list,gt);
					evaluate(doc);
					count++;
				}
				list.clear();
				gt.clear();
				isOpen = true;
			} else {
				String splitter[] = line.split("\\t");
				if(splitter.length > 4) {
					if(splitter[1].equalsIgnoreCase("B")) {
						String dbpediaUri = "http://dbpedia.org/resource/"+splitter[4].replaceAll("http://en.wikipedia.org/wiki/", "");
						if(isAvaiableInKb(dbpediaUri)) {
							
						}
						list.add(splitter[2]);
						gt.add(dbpediaUri);
					}
				}
			}
		}
		System.out.println(count);
		System.out.println("Overall :"+overall+" Correct: "+correct);
		reader.close();
	}
	
	public void evaluate(Document doc) {
		DisambiguationRequest req = new DisambiguationRequest();
		req.setDocumentUri("Local Disambiguation");
		List<EntityDisambiguationDPO> dpoList = new ArrayList<EntityDisambiguationDPO>();
		
		List<String> sfs = doc.sfList;
		for (int i = 0; i < sfs.size(); ++i) {
			EntityDisambiguationDPO dpo = new EntityDisambiguationDPO();
			dpo.setDocumentId("Local Disambiguation");
			dpo.setContext("");
			dpo.setSelectedText(sfs.get(i));
			dpoList.add(dpo);
		};

		req.setSurfaceFormsToDisambiguate(dpoList);
		
		
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
		List<String> gt = doc.gt;
		for (int i = 0; i < responses.size(); i++) {
			DisambiguatedEntity disEntity = responses.get(i).getDisEntities().get(0);
			System.out.println(disEntity.getEntityUri().toString() + "\t"+ gt.get(i));
			if(disEntity != null) {
				String uri = disEntity.getEntityUri();
				if(gt.get(i).equalsIgnoreCase(uri)) {
					correct++;
				}
			}
			overall++;
		}
		System.out.println("Zwischenstand: "+correct+" von "+overall);
	}
	
	public boolean isAvaiableInKb(String gt) {
		return true;
	}
	
	class Document {
		private List<String> sfList;
		private List<String> gt;
		
		Document(List<String> sfList, List<String> gt) {
			super();
			this.sfList = sfList;
			this.gt = gt;
		}
	}

}
