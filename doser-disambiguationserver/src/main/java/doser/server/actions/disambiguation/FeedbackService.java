package doser.server.actions.disambiguation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import doser.entity.disambiguation.feedback.Feedback;
import doser.entitydisambiguation.feedback.dpo.RequestFeedbackProxy;

@Controller
@RequestMapping("/hbase/feedback")
public class FeedbackService {

	@RequestMapping(method = RequestMethod.POST)
	public @ResponseBody
	String disambiguate(
			@RequestBody final RequestFeedbackProxy request) {
		String operation = request.getOperation();
		StringBuffer res = new StringBuffer();
		synchronized (this) {
			if (operation.equalsIgnoreCase("setQueryResult")) {
				res.append(Feedback.getInstance().setQueryResult(request));
			} else if (operation.equalsIgnoreCase("setFinalFeedback")) {
				res.append(Feedback.getInstance().setFinalFeedback(request));
			} else {
				res.append("Fail_Unknown_Error");
			}
		}
		return res.toString();
	}
}
