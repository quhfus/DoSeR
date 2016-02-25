package doser.entitydisambiguation.knowledgebases;

import org.apache.lucene.search.similarities.Similarity;

public class EntityCentricKBBiomed extends EntityCentricKBGeneral{

	public EntityCentricKBBiomed(String uri, boolean dynamic, Similarity sim) {
		super(uri, dynamic, sim);
	}
	
	public EntityCentricKBBiomed(String uri, boolean dynamic) {
		super(uri, dynamic);
	}

	@Override
	protected String generateDomainName() {
		return "Biomed";
	}

}
