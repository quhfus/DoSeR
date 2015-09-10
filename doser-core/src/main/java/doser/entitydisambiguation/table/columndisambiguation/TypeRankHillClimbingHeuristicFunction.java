package doser.entitydisambiguation.table.columndisambiguation;

import java.util.Set;

import aima.core.search.framework.HeuristicFunction;
import doser.entitydisambiguation.table.logic.TableColumn;

public class TypeRankHillClimbingHeuristicFunction implements HeuristicFunction {

	@Override
	public double h(final Object arg0) { // NOPMD by quh on 03.03.14 13:56
		final HillClimbingColumnDisambiguation obj = (HillClimbingColumnDisambiguation) arg0;
		final Set<TableColumn> set = obj.getTableColumns();
		double sumNodePotentials = 0;
		for (final TableColumn column : set) {
			sumNodePotentials += column.getScoreOfLeadingType();
		}

		double sumClPotentials = 0;
		for (final TableColumn col1 : set) {
			for (final TableColumn col2 : set) {
				if (col1.getColumnNr() != col2.getColumnNr()) {
					sumClPotentials += this.relatedness(col1, col2);
				}
			}
		}
		return (sumNodePotentials + sumClPotentials);
	}

	private double relatedness(final TableColumn col1, final TableColumn col2) {
		double result = 0;
		result = this.relationScore(col1.getURIOfLeadingType(),
				col2.getURIOfLeadingType());
		return result;
	}

	/**
	 * So far we only use existing occurrences between types. We compute the log
	 * of the amount of leadingType1 occurring in the vicinity of leadingType2
	 * 
	 * @param leadingType1
	 *            Type 1
	 * @param leadingType2
	 *            Type 2
	 * @return the score
	 */
	private double relationScore(final String leadingType1,
			final String leadingType2) {
		//ToDo
		return 0;
	}
}
