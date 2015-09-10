package doser.entitydisambiguation.table.columndisambiguation;

import java.util.Set;

import aima.core.agent.Action;
import aima.core.search.framework.ActionsFunction;
import aima.core.search.framework.ResultFunction;

public class TypeRankHillClimbingFactory {

	private TypeRankHillClimbingFactory() {
		super();
	}
	
	private static class EPActionsFunction implements ActionsFunction {
		@Override
		public Set<Action> actions(final Object state) {
			final HillClimbingColumnDisambiguation coldis = (HillClimbingColumnDisambiguation) state;
			return coldis.generatePossibleActions();
		}
	}

	private static class EPResultFunction implements ResultFunction {
		@Override
		public Object result(final Object obj, final Action action) {
			final HillClimbingColumnDisambiguation colDis = (HillClimbingColumnDisambiguation) obj;
			final HillClimbingColumnDisambiguation newColDis = new HillClimbingColumnDisambiguation(
					colDis);
			newColDis.setChange(action);
			return newColDis;
		}
	}

	private static ActionsFunction actionsFunction = null;

	private static ResultFunction resultFunction = null;

	public synchronized static ActionsFunction getActionsFunction() {
		if (null == actionsFunction) {
			actionsFunction = new EPActionsFunction();
		}
		return actionsFunction;
	}

	public synchronized static ResultFunction getResultFunction() {
		if (null == resultFunction) {
			resultFunction = new EPResultFunction();
		}
		return resultFunction;
	}

}
