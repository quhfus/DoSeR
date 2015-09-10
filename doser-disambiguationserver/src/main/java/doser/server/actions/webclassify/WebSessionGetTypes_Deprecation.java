package doser.server.actions.webclassify;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import doser.entitydisambiguation.table.logic.Type;
import doser.tools.RDFGraphOperations;
import doser.webclassify.dpo.WebTypeRequest_Deprecated;
import doser.webclassify.dpo.WebTypeResponse_Deprecated;

@Controller
@RequestMapping("/webclassify/types")
@Deprecated
public class WebSessionGetTypes_Deprecation {

	@RequestMapping(method = RequestMethod.POST)
	public @ResponseBody
	WebTypeResponse_Deprecated disambiguate(
			@RequestBody final WebTypeRequest_Deprecated request) {
		WebTypeResponse_Deprecated response = new WebTypeResponse_Deprecated();
		Set<String> entities = request.getEntities();
		Map<String, Set<String>> res = new TreeMap<String, Set<String>>();
		for (String ent : entities) {
			Set<Type> types = RDFGraphOperations.getDbpediaCategoriesFromEntity(ent);
			Set<String> t = new HashSet<String>();
			for (Type type : types) {
				t.add(type.getUri());
			}
			res.put(ent, t);
		}
		response.setTypes(res);
		return response;
	}
	
}
