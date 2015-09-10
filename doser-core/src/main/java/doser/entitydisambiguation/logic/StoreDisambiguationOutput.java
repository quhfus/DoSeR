package doser.entitydisambiguation.logic;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import doser.entitydisambiguation.dpo.Position;
import doser.entitydisambiguation.feedback.dpo.RequestFeedbackProxy;
import doser.entitydisambiguation.properties.Properties;
import doser.tools.ServiceQueries;

/**
 * TODO
 * 
 * Funktioniert noch nicht! Muss nochmal überarbeitet werden!
 * 
 * 
 * @author quh
 *
 */
public class StoreDisambiguationOutput {

	class LTRJob implements Runnable {

		private final RequestFeedbackProxy request;

		LTRJob(final RequestFeedbackProxy request) {
			this.request = request;
		}

		// private DisambiguationResponse generateErrorResponse(final Exception
		// exc) {
		// final DisambiguationResponse res = new DisambiguationResponse();
		// res.setDocumentUri(exc.getMessage());
		// return res;
		// }

		@Override
		public void run() {
			final ObjectMapper mapper = new ObjectMapper();
			String json = null;
			try {
				json = mapper.writeValueAsString(this.request);
				Header[] headers = {
						new BasicHeader("Accept", "application/json"),
						new BasicHeader("content-type", "application/json") };
				ByteArrayEntity ent = new ByteArrayEntity(json.getBytes(),
						ContentType.create("application/json"));
				ServiceQueries.httpPostRequest(Properties.getInstance()
						.getLearnToRankOutputService(), ent, headers);
			} catch (IOException e) {
				Logger.getRootLogger().error("Error:", e);
			}
		}
	}

	private int createQueryHash(final String docId, final String text,
			final List<Position> posList) {

		final StringBuffer buffer = new StringBuffer();
		if (posList != null) {
			for (final Position position2 : posList) {
				final Position position = position2;
				buffer.append(position.toString());
			}
		}

		final String uniqueQuery = new StringBuffer().append(docId)
				.append(text.toLowerCase(Locale.US)).append(buffer.toString())
				.toString();
		return uniqueQuery.hashCode();
	}

	public void storeQuery(final String documentId,
			final String surfaceForms, final List<Position> posList,
			final String[] uniqueEntityUris, final String context) {

		StringBuffer buffer = new StringBuffer();
			buffer.append(surfaceForms + "|");
		String sf = buffer.toString();
		sf = sf.substring(0, sf.length() - 1);
		final RequestFeedbackProxy request = new RequestFeedbackProxy();
		request.setOperation("setQueryResult");
		request.setTableName("DisLTR_Cache");
		request.setCurrentFamily("data");
		request.setUniqueEntityUri(uniqueEntityUris);
		request.setRowKey(String.valueOf(createQueryHash(documentId, sf,
				posList)));
		request.setContext(context);
		request.setSurfaceForms(sf);

		final Thread thread = new Thread(new LTRJob(request));
		thread.start();
	}
}
