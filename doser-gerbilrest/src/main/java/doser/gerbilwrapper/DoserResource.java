package doser.gerbilwrapper;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.TurtleNIFDocumentCreator;
import org.aksw.gerbil.transfer.nif.TurtleNIFDocumentParser;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.SpanImpl;
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
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class DoserResource extends ServerResource {

	public static final String DISAMBIGUATIONSERVICE = "http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/disambiguation/disambiguationWithoutCategories-collective";
	public static final int CONTEXTAREA = 1200;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DoserResource.class);

	private TurtleNIFDocumentParser parser = new TurtleNIFDocumentParser();
	private TurtleNIFDocumentCreator creator = new TurtleNIFDocumentCreator();

	@Post
	public String accept(Representation request) {
		Reader inputReader;
		try {
			inputReader = request.getReader();
		} catch (IOException e) {
			LOGGER.error("Exception while reading request.", e);
			return "";
		}
		// ... this is only the parsing of an incoming document
		Document document;
		try {
			document = parser.getDocumentFromNIFReader(inputReader);
		} catch (Exception e) {
			LOGGER.error("Exception while reading request.", e);
			return "";
		}
		// If your system is only for entity linking, the document object
		// should already contain a list of markings

		// Now we have the text and a list of markings (this could be
		// empty or contain Span objects which would mark the named
		// entities inside the text) and could call your system for
		// performing the entity linking task...

		List<Marking> markings = document.getMarkings();
		for (Marking mark : markings) {
			SpanImpl span = (SpanImpl) mark;
//			System.out.println(document.getText().substring(
//					span.getStartPosition(),
//					span.getStartPosition() + span.getLength())
//					+ "    "
//					+ extractContext(span.getStartPosition(),
//							document.getText()));
		}

		List<Marking> entities = new ArrayList<Marking>(markings.size());

		if (markings.size() > 0) {
			DisambiguationRequest req = new DisambiguationRequest();
			req.setDocsToReturn(1);
			req.setDocumentUri(document.getDocumentURI());
			List<EntityDisambiguationDPO> dpoList = new ArrayList<EntityDisambiguationDPO>();

			for (int i = 0; i < markings.size(); ++i) {
				SpanImpl span = (SpanImpl) markings.get(i);
				String sf = document.getText().substring(
						span.getStartPosition(),
						span.getStartPosition() + span.getLength());
				System.out.println("Surface Form: "+sf);
				System.out.println("------------------------------------------------------------------------------------");
				EntityDisambiguationDPO dpo = new EntityDisambiguationDPO();
				dpo.setDocumentId(document.getDocumentURI());
				String context = extractContext(span.getStartPosition(),
						document.getText());
				dpo.setContext(context);
				dpo.setSelectedText(sf);
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

			// System.out.println("Responses Size:" +responses.size());
			// for(Response res : responses) {
			// System.out.println("Response: "+res.getSelectedText()+res.getDisEntities());
			// }

			// ... as result a list of NamedEntity or ScoredNamedEntity objects
			// should be created for the A2W or Sa2W tasks respectively. For
			// C2W, Rc2W or Sc2W you should create a list of Annotations or
			// ScoredAnnotations

			for (int i = 0; i < markings.size(); ++i) {
				SpanImpl span = (SpanImpl) markings.get(i);
				Response res = responses.get(i);

				if (res != null) {
					List<DisambiguatedEntity> disEntities = res
							.getDisEntities();
//					System.out.println("Surface form: "+(document.getText().substring(span.getStartPosition(), span.getStartPosition() + span.getLength())) + "context: "+extractContext(span.getStartPosition(),
//							document.getText())); 
//					System.out.println(disEntities.get(0).getEntityUri());
					entities.add(new NamedEntity(span.getStartPosition(), span
							.getLength(), disEntities.get(0).getEntityUri()));
				}
			}
		}

		// ... this new list is added to the document and the document is
		// send back to GERBIL
		document.setMarkings(entities);
		String nifDocument = creator.getDocumentAsNIFString(document);
		return nifDocument;
	}

	private String extractContext(int position, String text) {
		long startArea = position - CONTEXTAREA;
		long endArea = position + CONTEXTAREA;
		if (startArea < 0) {
			startArea = 0;
		}
		if (endArea > text.length() - 1) {
			endArea = text.length() - 1;
		}
		String tempText = text.substring((int) startArea, (int) endArea);
		String[] splitter = tempText.split(" ");
		String result = "";
		for (int i = 1; i < splitter.length - 1; i++) {
			result += splitter[i] + " ";
		}
		return result;
	}

}
