package doser.webclassify.dpo;

import java.util.List;

public class WebClassificationRequest {

	private List<WebSite> websitestream;
	
	public WebClassificationRequest() {
		super();
	}

	public List<WebSite> getWebsitestream() {
		return websitestream;
	}

	public void setWebsitestream(List<WebSite> websitestream) {
		this.websitestream = websitestream;
	}
}
