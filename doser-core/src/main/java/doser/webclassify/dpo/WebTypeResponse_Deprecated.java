package doser.webclassify.dpo;

import java.util.Map;
import java.util.Set;

@Deprecated
public class WebTypeResponse_Deprecated {

	private Map<String, Set<String>> types;

	public Map<String, Set<String>> getTypes() {
		return types;
	}

	public void setTypes(Map<String, Set<String>> types) {
		this.types = types;
	}
}
