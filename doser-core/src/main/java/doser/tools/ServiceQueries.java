package doser.tools;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 * Class providing queries for different services. Integrated so far: DbPedia
 * Spotlight
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class ServiceQueries {

	public static String httpPostRequest(String uri, AbstractHttpEntity entity,
			Header[] header) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeaders(header);
		httppost.setEntity(entity);

		HttpResponse response;
		StringBuffer buffer = new StringBuffer();
		try {
			response = httpclient.execute(httppost);
			HttpEntity ent = response.getEntity();

			buffer.append(EntityUtils.toString(ent));
			httpclient.getConnectionManager().shutdown();

		} catch (ClientProtocolException e) {
			Logger.getRootLogger().error("HTTPClient error", e);
		} catch (IOException e) {
			Logger.getRootLogger().error("HTTPClient error", e);
		}
		return buffer.toString();
	}
}
