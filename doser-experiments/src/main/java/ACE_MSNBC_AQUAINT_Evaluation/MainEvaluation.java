package ACE_MSNBC_AQUAINT_Evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import org.apache.log4j.Logger;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.gson.Gson;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import DisambiguationApproachDPO.DisambiguatedEntity;
import DisambiguationApproachDPO.DisambiguationRequest;
import DisambiguationApproachDPO.DisambiguationResponse;
import DisambiguationApproachDPO.EntityDisambiguationDPO;
import DisambiguationApproachDPO.Response;
import doser.tools.indexcreation.WikiPediaUriConverter;

public class MainEvaluation {

	public static final String DISAMBIGUATIONSERVICE = "http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/disambiguation/disambiguationWithoutCategories-collective";

	private Model redirects;
	
	public MainEvaluation(String maindir) {
		File maind = new File(maindir);
		HDT redirectsHDT;
		try {
			redirectsHDT = HDTManager.mapIndexedHDT("/home/quh/dbpedia_redirects.hdt", null);
			final HDTGraph redirectsHDTgraph = new HDTGraph(redirectsHDT);
			this.redirects = ModelFactory.createModelForGraph(redirectsHDTgraph);
		} catch (IOException e) {
			e.printStackTrace();
		}
		evaluate(maind);
	}

	private void evaluate(File mainDirectory) {
		String dirStr = mainDirectory.getAbsolutePath();
		File solutions = new File(dirStr + "/Problems");
		String[] sols = solutions.list();
		int overall = 0;
		int correct = 0;
		int possibleCorrect = 0;
		for (int i = 0; i < sols.length; i++) {
			String text = extractMainText(dirStr + "/RawTexts/" + sols[i]);
			List<Problem> problem = createProblems(new File(dirStr
					+ "/Problems/" + sols[i]), text);
			DisambiguationRequest req = new DisambiguationRequest();
			List<EntityDisambiguationDPO> dpoList = new ArrayList<EntityDisambiguationDPO>();
			List<String> groundtruth = new ArrayList<String>();
			for (Problem p : problem) {
				dpoList.add(p.getDpo());
				groundtruth.add(p.getGroundtruth());
			}
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
//			System.out.println(json);
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
//			System.out.println(buffer.toString());
			DisambiguationResponse disResponse = gson.fromJson(
					buffer.toString(), DisambiguationResponse.class);
			List<Response> responses = disResponse.getTasks();
			for (int j = 0; j < responses.size(); j++) {
				if (responses.get(j) != null) {
					DisambiguatedEntity disEntity = responses.get(j)
							.getDisEntities().get(0);
					if (disEntity != null) {
						String uri = disEntity.getEntityUri();
						String gt = groundtruth.get(j).trim();
						gt = gt.replaceAll("http://en.wikipedia.org/wiki/", "");
						StringBuffer b = new StringBuffer();
						gt = WikiPediaUriConverter.createConformDBpediaUrifromEncodedString(gt);
						String newUri = getRedirect(gt);
						if(newUri != null) {
							gt = newUri;
						}
						b.append("URI: "+uri+" GT: "+gt);
						if (gt.equalsIgnoreCase(uri)) {
							correct++;
							b.append(" true");
						} else {
							b.append(" false");
							System.out.println(b.toString());
						}
						possibleCorrect++;
//						System.out.println(b.toString());
					}
				}
				overall++;
			}
		}
		System.out.println("Zwischenstand: " + correct + " von " + possibleCorrect);
	}

	private List<Problem> createProblems(File problem, String text) {
		List<Problem> list = null;
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();

			FileReader reader = new FileReader(problem);
			InputSource inputSource = new InputSource(reader);

			ProblemHandler handler = new ProblemHandler();
			xmlReader.setContentHandler(handler);
			xmlReader.parse(inputSource);
			list = handler.getList();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}

	private String extractMainText(String filename) {
		StringBuffer buffer = new StringBuffer();
		File textfile = new File(filename);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(textfile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
		return buffer.toString();
	}
	
	public String getRedirect(final String entityUri) {
		final String query = "SELECT ?types WHERE{ <" + entityUri
				+ "> <http://dbpedia.org/ontology/wikiPageRedirects> ?types. }";
		ResultSet results = null;
		QueryExecution qexec = null;
		try {
			final com.hp.hpl.jena.query.Query cquery = QueryFactory
					.create(query);
			qexec = QueryExecutionFactory.create(cquery, redirects);
			results = qexec.execSelect();
		} catch (final QueryException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		} finally {
			if (results != null) {
				while (results.hasNext()) {
					final QuerySolution sol = results.nextSolution();
					final String type = sol.getResource("types").toString();
					return type;
				}
			}
		}
		return null;
	}

	class ProblemHandler implements ContentHandler {

		private List<Problem> dpoList;

		private EntityDisambiguationDPO dpo;

		private Problem p;

		private String currentValue;

		ProblemHandler() {
			super();
			this.dpoList = new ArrayList<Problem>();
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			// TODO Auto-generated method stub

		}

		@Override
		public void startDocument() throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void endDocument() throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
			if (localName.equals("ReferenceInstance")) {
				this.dpo = new EntityDisambiguationDPO();
				this.p = new Problem();
			}

			if (localName.equals("SurfaceForm")) {
				this.currentValue = "";
			}

			if (localName.equals("ChosenAnnotation")) {
				this.currentValue = "";
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (localName.equals("SurfaceForm")) {
				dpo.setSelectedText(currentValue.trim());
			}

			if (localName.equals("ChosenAnnotation")) {
				p.setGroundtruth(currentValue);
			}

			if (localName.equals("ReferenceInstance")) {
				dpo.setContext("");
				p.setDpo(dpo);
				dpoList.add(p);
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			currentValue += new String(ch, start, length);
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length)
				throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void processingInstruction(String target, String data)
				throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void skippedEntity(String name) throws SAXException {
			// TODO Auto-generated method stub

		}

		public List<Problem> getList() {
			return dpoList;
		}
	}

	class Problem {

		private EntityDisambiguationDPO dpo;

		private String groundtruth;

		public EntityDisambiguationDPO getDpo() {
			return dpo;
		}

		public void setDpo(EntityDisambiguationDPO dpo) {
			this.dpo = dpo;
		}

		public String getGroundtruth() {
			return groundtruth;
		}

		public void setGroundtruth(String groundtruth) {
			this.groundtruth = groundtruth;
		}
	}

	public static void main(String[] args) {
		new MainEvaluation(
				"/home/quh/Arbeitsfläche/WikificationACL2011Data/AQUAINT/");
	}

}
