package doser.server.actions.categorysuggestion;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import doser.categorysuggestion.algorithm.StandardDbPediaCategorySuggestion;
import doser.categorysuggestion.dpo.CatSugRequest;
import doser.categorysuggestion.dpo.CatSugResponse;
import doser.entitydisambiguation.table.logic.Type;

@Controller
@RequestMapping("/disambiguation/categorysuggestion-proxy")
public class CategorySuggestionService {

	@RequestMapping(method = RequestMethod.POST)
	public @ResponseBody
	CatSugResponse suggestCategory(@RequestBody final CatSugRequest request) {
		final String input = request.getInput();
		final String language = request.getLanguage();
		final CatSugResponse response = new CatSugResponse();
		final List<Type> cats = StandardDbPediaCategorySuggestion
				.getInstance().suggest(input, language);
		response.setCategories(cats);
		return response;
	}
}
