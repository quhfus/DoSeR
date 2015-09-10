package doser.server.actions.webclassify;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import doser.webclassify.annotation.AnnotateCategories;
import doser.webclassify.dpo.WebClassificationRequest;
import doser.webclassify.dpo.WebClassificationResponse;
import doser.webclassify.dpo.WebSite;

@Controller
@RequestMapping("/webclassify/classify")
public class WebSessionClassification {

	@RequestMapping(method = RequestMethod.POST)
	public @ResponseBody
	WebClassificationResponse disambiguate(
			@RequestBody final WebClassificationRequest request) {
		WebClassificationResponse response = new WebClassificationResponse();

		List<WebSite> webStream = request.getWebsitestream();
		AnnotateCategories algorithm = new AnnotateCategories();
		for (WebSite site : webStream) {
			algorithm.annotateCategory(site);
		}
		return null;
	}

	public static void main(String[] args) {
		WebClassificationRequest req = new WebClassificationRequest();
		WebSite site = new WebSite();
		site.setName("Test");
		site.setText("President Obama called Wednesday on Congress to extend a tax break for students included in last year's economic stimulus package, arguing that the policy provides more generous assistance.");
		List<WebSite> lst = new LinkedList<WebSite>();
		lst.add(site);
		req.setWebsitestream(lst);
		WebSessionClassification test = new WebSessionClassification();
		test.disambiguate(req);
	}
}
