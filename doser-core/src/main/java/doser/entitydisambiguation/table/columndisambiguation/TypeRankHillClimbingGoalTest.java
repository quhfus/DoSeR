package doser.entitydisambiguation.table.columndisambiguation;

import aima.core.search.framework.GoalTest;

public class TypeRankHillClimbingGoalTest implements GoalTest {

	public static final int MAXITERATIONS = 10;

	private int iterations;

	public TypeRankHillClimbingGoalTest() {
		this.iterations = 0;
	}

	@Override
	public boolean isGoalState(final Object arg0) {
		boolean result = false;
		if (iterations == MAXITERATIONS) {
			result = true;
		} else {
			this.iterations++;
		}
		return result;
	}

}
