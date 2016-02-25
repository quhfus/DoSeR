package doser.entitydisambiguation.algorithms.collective.general;

import java.util.LinkedList;
import java.util.List;

import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.algorithms.collective.CandidateReduction;
import doser.entitydisambiguation.knowledgebases.EntityCentricKBGeneral;

public class CandidateReductionGeneralW2V extends CandidateReduction {

	private int iterations;
	private boolean disambiguate;
	private EntityCentricKBGeneral eckb;
	private int reduceTo;
	
	public CandidateReductionGeneralW2V(EntityCentricKBGeneral eckb, List<SurfaceForm> rep, int maxsurfaceformsperquery,
			int reduceTo, int iterations, boolean disambiguate, boolean alwaysAction) {
		super(rep, maxsurfaceformsperquery, alwaysAction);
		this.iterations = iterations;
		this.disambiguate = disambiguate;
		this.eckb = eckb;
		this.reduceTo = reduceTo;
	}

	@Override
	public List<SurfaceForm> miniSolve(List<SurfaceForm> rep) {
		List<SurfaceForm> sol = new LinkedList<SurfaceForm>();
		Word2VecDisambiguatorGeneral disambiguator = new Word2VecDisambiguatorGeneral(eckb, rep, disambiguate, reduceTo,
				iterations);
		disambiguator.setup();
		disambiguator.solve();
		sol.addAll(disambiguator.getRepresentation());
		return sol;

	}
}
