package experiments.table.limaye;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import experiments.table.limaye.Table.Column;
import experiments.table.limaye.Table.Column.Cell;

public class StartEvaluationTableEntities {

	public static int sum = 0;

	public static int correct = 0;

	public static int haveoneresult = 0;

	public static void main(String[] args) {
		File file = new File("/home/quh/Arbeitsfläche/Entpackung/Arbeitsfläche/To/webtables/");
		File[] f = file.listFiles();
		int cellsOverall = 0;
		int cellsAnnotated = 0;
		
		for (int u = 0; u < f.length; u++) {
			System.out.println(f[u].getAbsolutePath());
			StartEvaluationTableEntities eval = new StartEvaluationTableEntities();
			String sourcePath = f[u].getAbsolutePath();
			String[] splitter = sourcePath.split("/");
			Table t = eval.readTable(f[u].getAbsolutePath());
			// t.setName(f[u].getAbsolutePath());
			File gtf = new File("/home/quh/Arbeitsfläche/Entpackung/Arbeitsfläche/gt/webtables/"
					+ splitter[splitter.length - 1]);
			eval.addGT(t, gtf.getAbsolutePath());

			
			int cols = t.getNumberofColumns();
			for (int i = 0; i < cols; i++) {
				Column col = t.getColumn(i);
				List<Cell> cellL = col.getCellList();
				List<String> types = col.getMajorTypes();
				cellsOverall++;
//				if(types != null && types.size() > 0) {
//					cellsAnnotated++;
//				}
				for(Cell c : cellL) {
					cellsOverall++;
					if(c.getGt() != null && !c.getGt().equalsIgnoreCase("")) {
						cellsAnnotated++;
					}
				}
			}
			
			System.out.println("Zellen insgesamt: "+cellsOverall+" Zellen annotiert: "+cellsAnnotated);
			
			// Transform into request format
//			TableDisambiguationRequest request = eval
//					.transformInRequestFormat(t);
//			// Call disambiguation service
//			TableDisambiguationResponse response = eval
//					.queryTableDisambiguationService(request);
//			// Transform back into table format
//			t = eval.transformFromRequestFormat(t, response);
//
//			StartEvaluationTableEntities.evaluateResults(t);
		}
//		System.out.println("Insgesamt: " + sum + " davon richtig: " + correct);
	}

//	private TableDisambiguationResponse queryTableDisambiguationService(
//			TableDisambiguationRequest request) {
//		String jsonResult = "";
//		ObjectMapper mapper = new ObjectMapper();
//		String json = null;
//		byte[] jsonByteString = null;
//		try {
//			json = mapper.writeValueAsString(request);
//			System.out.println(json);
//			jsonByteString = json.getBytes("UTF-8");
//		} catch (JsonParseException e) {
//			e.printStackTrace();
//		} catch (JsonMappingException e1) {
//			e1.printStackTrace();
//		} catch (IOException e2) {
//			e2.printStackTrace();
//		}
//
//		HttpClient httpclient = new DefaultHttpClient();
//		URIBuilder builder;
//		try {
//			builder = new URIBuilder(CELLDISURI);
//			HttpPost post = new HttpPost(builder.toString());
//			post.setHeader("accept", "application/json");
//			post.setHeader("content-type", "application/json");
//			post.setEntity(new ByteArrayEntity(jsonByteString, ContentType
//					.create("application/json")));
//			HttpResponse response = httpclient.execute(post);
//			HttpEntity entity = response.getEntity();
//			if (entity != null) {
//				InputStream d = entity.getContent();
//				BufferedReader reader = new BufferedReader(
//						new InputStreamReader(d));
//				String line = null;
//				while ((line = reader.readLine()) != null) {
//					jsonResult += line;
//				}
//			}
//
//			httpclient.getConnectionManager().shutdown();
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		try {
//			return mapper.readValue(jsonResult,
//					TableDisambiguationResponse.class);
//		} catch (JsonParseException e) {
//			e.printStackTrace();
//		} catch (JsonMappingException e1) {
//			e1.printStackTrace();
//		} catch (IOException e2) {
//			e2.printStackTrace();
//		}
//		return new TableDisambiguationResponse();
//
//	}
//
//	private TableDisambiguationRequest transformInRequestFormat(Table t) {
//		TableDisambiguationRequest request = new TableDisambiguationRequest();
//		request.setDocumentId("table");
//		request.setTableName(t.getName());
//		List<TableColumn> tc = new LinkedList<TableColumn>();
//		for (int i = 0; i < t.getNumberofColumns(); i++) {
//			Table.Column co = t.getColumn(i);
//			List<TableCell> cellList = new LinkedList<TableCell>();
//			for (int j = 0; j < co.getCellList().size(); j++) {
//				Table.Column.Cell c = co.getCellList().get(j);
//				TableCell nc = new TableCell();
//				nc.setCellContent(c.getCellContent());
//				cellList.add(nc);
//			}
//			TableColumn tcol = new TableColumn();
//			tcol.setCellheader("");
//			tcol.setCellList(cellList);
//			tc.add(tcol);
//		}
//		request.setColumnList(tc);
//		return request;
//	}
//
//	private Table transformFromRequestFormat(Table t,
//			TableDisambiguationResponse response) {
//		List<ColumnResponseItem> resi = response.getColumns();
//		if (resi != null) {
//			for (int i = 0; i < resi.size(); i++) {
//				ColumnResponseItem it = resi.get(i);
//				List<CellResponse> cr = it.getCells();
//				if (cr != null) {
//					for (int j = 0; j < cr.size(); j++) {
//						CellResponse res = cr.get(j);
//						t.getColumn(i).getCellList().get(j)
//								.setDisambigutedContentString(res.getText());
//						t.getColumn(i).getCellList().get(j)
//								.setDisambiguatedContent(res.getUri());
//					}
//				}
//			}
//		}
//		return t;
//	}

	public Table readTable(String uri) {
		Table t = null;
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			FileReader reader = new FileReader(uri);
			InputSource inputSource = new InputSource(reader);
			LimayeAnnotationParserWebTables p = new LimayeAnnotationParserWebTables();
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
		}
		return t;
	}

	public void addGT(Table table, String uri) {
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			FileReader reader = new FileReader(uri);
			InputSource inputSource = new InputSource(reader);
			LimayeGroundtruthAnnotationParser p = new LimayeGroundtruthAnnotationParser(
					table);
			xmlReader.setContentHandler(p);
			xmlReader.parse(inputSource);
			p = null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

//	public static void evaluateResults(Table t) {
//		// System.out.println(t.getName());
//		int nrC = t.getNumberofColumns();
//		for (int i = 0; i < nrC; i++) {
//			Table.Column c = t.getColumn(i);
//			List<Cell> cList = c.getCellList();
//			for (int j = 0; j < cList.size(); j++) {
//				Cell cell = cList.get(j);
//				String gt = cell.getGt();
//				String val = cell.getDisambiguatedContent();
////				System.out.println(val);
//				if (gt != null
//						&& !gt.equals("")
//						&& !gt.equalsIgnoreCase("http://dbpedia.org/resource/NULL")) {
//					if (val.equalsIgnoreCase(gt)) {
//						correct++;
//					} else {
//						 System.out.println("Input: " + cell.getCellContent()
//						 + " Groundtruth: " + gt + "   Value: " + val);
//					}
//					sum++;
//
//				}
//			}
//		}
//		System.out.println("Insgesamt: " + sum + " davon richtig: " + correct);
//	}

}
