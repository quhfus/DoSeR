package doser.entitydisambiguation.algorithms.collective;

import java.util.List;

public interface SolverInterface {

	public boolean disambiguate(List<CollectiveSFRepresentation> rep);
	}