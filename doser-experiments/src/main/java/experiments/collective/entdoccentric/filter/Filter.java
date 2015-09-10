package experiments.collective.entdoccentric.filter;

import experiments.collective.entdoccentric.StandardQueryDataObject;


public abstract class Filter {

	public Filter() {
	}
	
	public abstract StandardQueryDataObject filter(StandardQueryDataObject object);

}
