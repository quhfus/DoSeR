package doser.entitydisambiguation.algorithms.collective;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import doser.entitydisambiguation.algorithms.SurfaceForm;

public abstract class CandidateReduction {

	// public static final int MAXSURFACEFORMSPERQUERY = 20;
	// public static final int REDUCETO = 5;
	private List<SurfaceForm> rep;
	private boolean alwaysAction;
	private int maxsurfaceformsperquery;

	public CandidateReduction(List<SurfaceForm> rep,
			int maxsurfaceformsperquery, boolean alwaysAction) {
		super();
		this.rep = rep;
		this.maxsurfaceformsperquery = maxsurfaceformsperquery;
		this.alwaysAction = alwaysAction;
	}

	public void solve() {
		List<SurfaceForm> finalList = new LinkedList<SurfaceForm>();
		if (this.rep.size() > maxsurfaceformsperquery) {
			int counter = 0;
			while (true) {
				long time = System.currentTimeMillis();
				if ((counter + maxsurfaceformsperquery) < this.rep.size()) {
					List<SurfaceForm> subList = this.rep.subList(counter, (counter + maxsurfaceformsperquery));
					finalList.addAll(miniSolve(subList));
					counter += maxsurfaceformsperquery;
				} else {
					List<SurfaceForm> subList = this.rep.subList(counter, this.rep.size());
					List<SurfaceForm> cloneList = new LinkedList<SurfaceForm>();
					for (SurfaceForm sf : subList) {
						SurfaceForm clone = (SurfaceForm) sf.clone();
						cloneList.add(clone);
					}

					int prevcounter = 0;
					List<SurfaceForm> prevList = this.rep.subList(counter - maxsurfaceformsperquery, counter);
					while (cloneList.size() < maxsurfaceformsperquery) {
						SurfaceForm clone = (SurfaceForm) prevList.get(prevcounter).clone();
						clone.setRelevant(false);
						cloneList.add(clone);
						prevcounter++;
					}
					List<SurfaceForm> workedList = miniSolve(cloneList);
					List<SurfaceForm> sfs = new LinkedList<SurfaceForm>();
					for (SurfaceForm sf : workedList) {
						if (sf.isRelevant()) {
							sfs.add(sf);
						}
					}
					finalList.addAll(sfs);
					break;
				}
				long millis = System.currentTimeMillis() - time;
				String formatedTime = String.format("%d min, %d sec", 
					    TimeUnit.MILLISECONDS.toMinutes(millis),
					    TimeUnit.MILLISECONDS.toSeconds(millis) - 
					    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
					);
				System.out.println(formatedTime);
			}
			this.rep = finalList;
		} else {
			if(alwaysAction) {
				finalList.addAll(miniSolve(rep));
				this.rep = finalList;
			}
		}
	}

	public List<SurfaceForm> getRep() {
		return rep;
	}

	public abstract List<SurfaceForm> miniSolve(List<SurfaceForm> rep);
}
