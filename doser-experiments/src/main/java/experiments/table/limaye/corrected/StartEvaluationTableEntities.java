package experiments.table.limaye.corrected;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.gson.Gson;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import DisambiguationApproachDPO.DisambiguatedEntity;
import DisambiguationApproachDPO.DisambiguationRequest;
import DisambiguationApproachDPO.DisambiguationResponse;
import DisambiguationApproachDPO.EntityDisambiguationDPO;
import DisambiguationApproachDPO.Response;
import experiments.table.limaye.corrected.Table.Column;
import experiments.table.limaye.corrected.Table.Column.Cell;

public class StartEvaluationTableEntities {

	public static final String DISAMBIGUATIONSERVICE = "http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/disambiguation/disambiguationWithoutCategories-collective";

	private final static String REDIRECTS = "/home/quh/HDT/redirects.hdt";

	private final static String LABELS = "/home/quh/HDT/labels.hdt";

	// private final static String TYPES = "/home/quh/HDT/instance-types.hdt";

	public static int sum = 0;

	public static int correct = 0;

	public static int annotated = 0;

	public static int haveoneresult = 0;
	
	public static int disambiguationpages = 0;

	public static int colIns = 0;
	public static int rowIns = 0;
	public static int test = 0;
	
	public static void main(String[] args) {
		StartEvaluationTableEntities evaluate = new StartEvaluationTableEntities();
		evaluate.action();
	}

	public void action() {
		HDT hdt = null;
		HDT hdt_l = null;
		HDT hdt_d = null;
		Model m = null;
		Model m_l = null;
		Model m_d = null;
		try {
			hdt = HDTManager.mapIndexedHDT(REDIRECTS, null);
			hdt_l = HDTManager.mapIndexedHDT(LABELS, null);
			// hdt_d = HDTManager.mapIndexedHDT(TYPES, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		HDTGraph graph = new HDTGraph(hdt);
		m = ModelFactory.createModelForGraph(graph);
		graph = new HDTGraph(hdt_l);
		m_l = ModelFactory.createModelForGraph(graph);
		File file = new File("/home/quh/Arbeitsfläche/Table Disambiguation Data sets/LimayeAll/all_tables_raw(regen)/");
		File[] f = file.listFiles();
		int cellsOverall = 0;
		int cellsAnnotated = 0;

		for (int u = 0; u < f.length; u++) {
			// System.out.println(f[u].getAbsolutePath());
			StartEvaluationTableEntities eval = new StartEvaluationTableEntities();
			String sourcePath = f[u].getAbsolutePath();

			Table t = eval.readTable(f[u].getAbsolutePath(), m, m_l, m_d);
			if (t != null) {
				int cols = t.getNumberofColumns();
				for (int i = 0; i < cols; i++) {
					Column col = t.getColumn(i);
					List<Cell> cellL = col.getCellList();
					List<String> types = col.getMajorTypes();
					cellsOverall++;
					// if(types != null && types.size() > 0) {
					// cellsAnnotated++;
					// }
					for (Cell c : cellL) {
						cellsOverall++;
						if (c.getGt() != null && !c.getGt().equalsIgnoreCase("")) {
							cellsAnnotated++;
						}
					}
				}

				System.out.println("Zellen insgesamt: " + cellsOverall + " Zellen annotiert: " + cellsAnnotated);

				// Query each column separately
				for (int i = 0; i < t.getNumberofColumns(); i++) {

					Column column = t.getColumn(i);
					List<EntityDisambiguationDPO> request_dpo = eval.transformInRequestFormat(column);
					String topic = column.getHeader();
//					List<Response> l = queryService(request_dpo, topic);
//					setDisambiguatedColumn(t, i, l);
					colIns++;
					if(i == 0) {
						rowIns += column.getCellList().size();
					}
				}
				System.out.println("Columns"+colIns);
				System.out.println("Rows"+rowIns);
				test += t.getNumberofColumns() * t.getColumn(0).getCellList().size();
				System.out.println("Ins" +test);
//				StartEvaluationTableEntities.evaluateResults(t);
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

	private List<EntityDisambiguationDPO> transformInRequestFormat(Column c) {
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

	public Table readTable(String uri, Model m, Model m_l, Model m_d) {
		Table t = null;
		boolean readIn = true;
		FileReader reader = null;
		LimayeAnnotationParserWebTables p = null;
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			reader = new FileReader(uri);
			InputSource inputSource = new InputSource(reader);
			p = new LimayeAnnotationParserWebTables(m, m_l, m_d);
			xmlReader.setContentHandler(p);
			xmlReader.parse(inputSource);
			t = p.getTable();
			p = null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			readIn = false;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (!readIn) {
			return null;
		}
		return t;
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
				if (gt != null && !gt.equals("") && !gt.equalsIgnoreCase("http://dbpedia.org/resource/NULL")) {
					if (val.equalsIgnoreCase(gt)) {
						correct++;
					} else {
						System.out.println("Input: " + cell.getCellContent() + "Groundtruth: " + gt + " Value: " + val);
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
