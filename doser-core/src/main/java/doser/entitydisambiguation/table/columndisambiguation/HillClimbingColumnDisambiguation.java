package doser.entitydisambiguation.table.columndisambiguation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import aima.core.agent.Action;
import aima.core.agent.impl.DynamicAction;
import doser.entitydisambiguation.table.logic.TableColumn;
import doser.entitydisambiguation.table.logic.Type;

class HillClimbingColumnDisambiguation {

	class TypeChangeRepresentation {

		private final int columnNr;

		private final Type toChangeType;

		public TypeChangeRepresentation(final int columnNr,
				final Type currentType) {
			this.columnNr = columnNr;
			this.toChangeType = currentType;
		}
	}

	private final Set<TableColumn> cols;

	private final int currentChangeCol;

	public HillClimbingColumnDisambiguation(
			final HillClimbingColumnDisambiguation hcDis) {
		super();
		final Set<TableColumn> columns = new LinkedHashSet<TableColumn>();
		final Set<TableColumn> oldCols = hcDis.getTableColumns();
		for (final TableColumn col : oldCols) {
			try {
				final TableColumn clone = col.clone();
				columns.add(clone);
			} catch (CloneNotSupportedException e) {
				Logger.getRootLogger().error(e.getStackTrace());
			}
		}
		this.cols = columns;
		// Choose Random Column for Change
		if (cols.size() == 1) {
			this.currentChangeCol = 0;
		} else {
			final Random random = new Random();
			int randomVal = random.nextInt();
			while (randomVal == hcDis.getCurrentChangeCol()) {
				randomVal = random.nextInt();
			}
			this.currentChangeCol = randomVal;
		}
	}

	public HillClimbingColumnDisambiguation(final Set<TableColumn> columns) {
		super();
		this.cols = columns;

		// Choose Random Column for Change
		final Random random = new Random();
		final int randomVal = random.nextInt();
		this.currentChangeCol = randomVal % columns.size();
	}

	/**
	 * Evaluate which perturbing column label can be (significantly) improved
	 * 
	 * @return The list with its disambiguated and possible types.
	 */
	public Set<Action> generatePossibleActions() {
		final Set<Action> actions = new LinkedHashSet<Action>();

		final List<Type> columnTypes = cols
				.toArray(new TableColumn[cols.size()])[currentChangeCol]
				.getColumnTypes();
		for (final Type ctype : columnTypes) {
			final DynamicAction action = new DynamicAction(ctype.getUri());
			final TypeChangeRepresentation rep = new TypeChangeRepresentation(
					currentChangeCol, ctype);
			action.setAttribute("Representation", rep);
			actions.add(action);
		}
		return actions;
	}

	public Set<TableColumn> getTableColumns() {
		return this.cols;
	}

	public void setChange(final Action act) {
		final DynamicAction action = (DynamicAction) act;
		final TypeChangeRepresentation rep = (TypeChangeRepresentation) action
				.getAttribute("Representation");
		for (final TableColumn col : this.cols) {
			if (col.getColumnNr() == rep.columnNr) {
				col.setNewLeadingType(rep.toChangeType);
			}
		}
	}

	public int getCurrentChangeCol() {
		return this.currentChangeCol;
	}
}
