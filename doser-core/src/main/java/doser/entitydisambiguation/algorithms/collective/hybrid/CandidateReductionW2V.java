package doser.entitydisambiguation.algorithms.collective.hybrid;

import java.util.LinkedList;
import java.util.List;

import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;

public class CandidateReductionW2V {

	public static final int MAXSURFACEFORMSPERQUERY = 20;
	public static final int REDUCETO = 5;
	private List<SurfaceForm> rep;
	private Word2Vec w2v;
	private EntityCentricKnowledgeBaseDefault eckb;

	CandidateReductionW2V(EntityCentricKnowledgeBaseDefault eckb, List<SurfaceForm> rep, Word2Vec w2v) {
		super();
		this.rep = rep;
		this.w2v = w2v;
		this.eckb = eckb;
	}

	public void solve() {
		List<SurfaceForm> finalList = new LinkedList<SurfaceForm>();
		if (this.rep.size() > MAXSURFACEFORMSPERQUERY) {
			int counter = 0;
			while (true) {
				if ((counter + MAXSURFACEFORMSPERQUERY) < this.rep.size()) {
					List<SurfaceForm> subList = this.rep.subList(counter, (counter + MAXSURFACEFORMSPERQUERY));
					finalList.addAll(miniSolve(subList));
					counter += MAXSURFACEFORMSPERQUERY;
				} else {
					List<SurfaceForm> subList = this.rep.subList(counter, this.rep.size());
					List<SurfaceForm> cloneList = new LinkedList<SurfaceForm>();
					for(SurfaceForm sf : subList) {
						SurfaceForm clone = (SurfaceForm) sf.clone();
						cloneList.add(clone);
					}
					
					int prevcounter = 0;
					List<SurfaceForm> prevList = this.rep.subList(counter - MAXSURFACEFORMSPERQUERY, counter);
					while (cloneList.size() < MAXSURFACEFORMSPERQUERY) {
						SurfaceForm clone = (SurfaceForm) prevList.get(prevcounter).clone();
						clone.setRelevant(false);
						cloneList.add(clone);
						prevcounter++;
					}
					List<SurfaceForm> workedList = miniSolve(cloneList);
					List<SurfaceForm> sfs = new LinkedList<SurfaceForm>();
					for(SurfaceForm sf :  workedList) {
						if(sf.isRelevant()) {
							sfs.add(sf);
						}
					}
					finalList.addAll(sfs);
					break;
				}
			}
			this.rep = finalList;
		}
	}

	public List<SurfaceForm> getRep() {
		return rep;
	}

	private List<SurfaceForm> miniSolve(List<SurfaceForm> rep) {
		List<SurfaceForm> sol = new LinkedList<SurfaceForm>();
		Word2VecDisambiguator disambiguator = new Word2VecDisambiguator(eckb.getFeatureDefinition(), rep, w2v, false,
				REDUCETO, 125);
		disambiguator.setup();
		disambiguator.solve();
		sol.addAll(disambiguator.getRepresentation());
		return sol;
	}

}
