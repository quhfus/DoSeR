package experiments.table.imdbAndMusicBrainz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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

import experiments.table.limaye.corrected.DisambiguatedEntity;
import experiments.table.limaye.corrected.DisambiguationRequest;
import experiments.table.limaye.corrected.DisambiguationResponse;
import experiments.table.limaye.corrected.EntityDisambiguationDPO;
import experiments.table.limaye.corrected.Response;
import experiments.table.limaye.corrected.Table;
import experiments.table.limaye.corrected.Table.Column;
import experiments.table.limaye.corrected.Table.Column.Cell;

public class StartEvaluationTableEntities {

	public static final String DISAMBIGUATIONSERVICE = "http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/disambiguation/disambiguationWithoutCategories-collective";

	public static int sum = 0;

	public static int correct = 0;

	public static int annotated = 0;

	public static int haveoneresult = 0;
	
	public static int disambiguationpages = 0;

	public static void main(String[] args) {
		StartEvaluationTableEntities evaluate = new StartEvaluationTableEntities();
		evaluate.action();
	}

	public void action() {
		int cellsOverall = 0;
		int cellsAnnotated = 0;

		BufferedReader reader = null;
		try {
		reader = new BufferedReader(new FileReader(new File("/home/quh/Arbeitsfläche/Table Disambiguation Data sets/musicbrainz_columns.txt")));
		String line = null;
		Table current = new Table();
		List<String> lines = new ArrayList<String>();
		while((line = reader.readLine()) != null) {
			lines.add(line);			
			// New table
			if(line.equals("")) {
				readTable(current, lines);
				if (current != null) {
					int cols = current.getNumberofColumns();
					for (int i = 0; i < cols; i++) {
						Column col = current.getColumn(i);
						List<Cell> cellL = col.getCellList();
						cellsOverall++;
						for (Cell c : cellL) {
							cellsOverall++;
							if (c.getGt() != null && !c.getGt().equalsIgnoreCase("")) {
								cellsAnnotated++;
							}
						}
					}
					System.out.println("Zellen insgesamt: " + cellsOverall + " Zellen annotiert: " + cellsAnnotated);
					for (int i = 0; i < current.getNumberofColumns(); i++) {
						Column column = current.getColumn(i);
						List<EntityDisambiguationDPO> request_dpo = transformInRequestFormat(column);
						String topic = column.getHeader();
						List<Response> l = queryService(request_dpo, topic);
						setDisambiguatedColumn(current, i, l);
					}

					StartEvaluationTableEntities.evaluateResults(current);
				}
				lines = new ArrayList<String>();
				current = new Table();
			}
		}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Insgesamt: " + sum + " davon richtig: " + correct);
	}

	private static List<Response> queryService(List<EntityDisambiguationDPO> dpos, String topic) {

		DisambiguationRequest req = new DisambiguationRequest();
		req.setDocsToReturn(1);
		req.setDocumentUri("TestUrl");
		req.setSurfaceFormsToDisambiguate(dpos);
		// req.setMainTopic(topic);

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
		// System.out.println(buffer.toString());
		DisambiguationResponse disResponse = gson.fromJson(buffer.toString(), DisambiguationResponse.class);
		List<Response> responses = disResponse.getTasks();
		return responses;
	}

	private static List<EntityDisambiguationDPO> transformInRequestFormat(Column c) {
		List<EntityDisambiguationDPO> list = new LinkedList<EntityDisambiguationDPO>();
		List<Cell> cells = c.getCellList();
		for (Cell cell : cells) {
			EntityDisambiguationDPO dpo = new EntityDisambiguationDPO();
			dpo.setDocumentId("");
			dpo.setContext(cell.getCellContent());
			dpo.setSelectedText(cell.getCellContent());
			// System.out.println(cell.getCellContent());
			dpo.setStartPosition(0);
			list.add(dpo);
		}
		return list;
	}

	private void setDisambiguatedColumn(Table t, int columnNr, List<Response> list) {
		Column col = t.getColumn(columnNr);
		List<Cell> cellList = col.getCellList();
		for (int i = 0; i < cellList.size(); i++) {
			Response res = list.get(i);
			Cell cell = cellList.get(i);
			if (res == null) {
				cell.setDisambigutedContentString("");
			} else {
				List<DisambiguatedEntity> disEntities = res.getDisEntities();
				if (disEntities == null || disEntities.size() == 0) {
					cell.setDisambigutedContentString("");
				} else {
					cell.setDisambigutedContentString(disEntities.get(0).getText());
					cell.setDisambiguatedContent(disEntities.get(0).getEntityUri());
					// System.out.println(cell.getCellContent());
					// System.out.println(disEntities.get(0).getEntityUri());

				}
			}
		}
	}

	public void readTable(Table t, List<String> lines) {
		t.addColumn("");
		Column col = t.getColumn(0);
		for(int i = 1; i < lines.size() - 1; i++) {
			String[] splitter = lines.get(i).split("\\t");
			String sf = splitter[0];
			col.addCell(sf);
			if(splitter.length > 1) {
				col.addLastCellGT(splitter[1]);
			}
		}
	}

	public static void evaluateResults(Table t) {
		// System.out.println(t.getName());
		int nrC = t.getNumberofColumns();
		for (int i = 0; i < nrC; i++) {
			Table.Column c = t.getColumn(i);
			List<Cell> cList = c.getCellList();
			for (int j = 0; j < cList.size(); j++) {
				Cell cell = cList.get(j);
				String gt = cell.getGt();
				String val = cell.getDisambiguatedContent();
				// System.out.println(val);
				if (gt != null && !gt.equals("") && !gt.equalsIgnoreCase("http://dbpedia.org/resource/NULL") && !gt.equalsIgnoreCase("NULL")) {
					if(gt.contains(val)) {
						correct++;
					} else {
						System.out.println("Input: " + cell.getCellContent() + " Groundtruth: " + gt + " Value: " + val);
					}
					if (val != null && !val.equalsIgnoreCase("")) {
						annotated++;
					}
					if(gt.contains("(disambiguation)")) {
						disambiguationpages++;
					}
					sum++;

				}
			}
		}
		float prec = ((float) correct / (float) annotated);
		float recall = ((float) correct / (float) sum);
		float f1 = (2 * prec * recall) / (prec + recall);
		float acc = ((float) correct / (float) sum);
		System.out.println("Precision: " + prec + " Recall: " + recall + " F1: " + f1 + " Accuracy: " + acc + "DisambiguationPages: "+disambiguationpages);
	}
}
