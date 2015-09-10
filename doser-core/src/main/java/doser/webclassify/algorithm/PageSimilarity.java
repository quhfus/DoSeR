package doser.webclassify.algorithm;

import doser.webclassify.dpo.WebSite;

public interface PageSimilarity {

	public double computeSimilarity(WebSite web1, WebSite web2);
	
}
