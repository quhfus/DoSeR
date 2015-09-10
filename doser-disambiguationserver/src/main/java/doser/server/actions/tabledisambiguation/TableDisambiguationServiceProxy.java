package doser.server.actions.tabledisambiguation;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import doser.entitydisambiguation.table.dpo.ColumnResponseItem;
import doser.entitydisambiguation.table.dpo.TableColumn;
import doser.entitydisambiguation.table.dpo.TableDisambiguationRequest;
import doser.entitydisambiguation.table.dpo.TableDisambiguationResponse;
import doser.entitydisambiguation.table.logic.TableDisambiguationMainService;
import doser.entitydisambiguation.table.logic.TableDisambiguationTask;

@Controller
@RequestMapping("/disambiguation/disambiguatetable-proxy")
public class TableDisambiguationServiceProxy {

	@RequestMapping(method = RequestMethod.POST)
	public @ResponseBody
	TableDisambiguationResponse disambiguate(
			@RequestBody final TableDisambiguationRequest request) {
		final TableDisambiguationResponse response = new TableDisambiguationResponse();
		final List<TableColumn> columnList = request.getColumnList();
		response.setDocumentId(request.getDocumentId());
		final List<TableDisambiguationTask> tasks = new LinkedList<TableDisambiguationTask>();
		final TableDisambiguationMainService tablems = TableDisambiguationMainService
				.getInstance();
		for (int i = 0; i < columnList.size(); i++) {
			final TableDisambiguationTask task = new TableDisambiguationTask(
					request.getDocumentId(), columnList.get(i).getCellList(),
					columnList.get(i).getTypeGroundtruth(), columnList.get(i)
							.getCellheader());
			tasks.add(task);
		}
		final List<ColumnResponseItem> lst = tablems.disambiguate(tasks);
		response.setColumns(lst);
		return response;
	}
}
