package doser.entitydisambiguation.table.celldisambiguation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.DisambiguationRequest;
import doser.entitydisambiguation.dpo.DisambiguationResponse;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.table.logic.TableCell;
import doser.entitydisambiguation.table.logic.TableColumn;
import doser.tools.ServiceQueries;

/**
 * Class that performs TableCell disambiguation on specialized domain (i.e.
 * custom generated Computer Science Table Disambiguation Index)
 * 
 * @author Stefan Zwicklbauer
 */
public final class CellDisAlgorithm_CSDomain implements
		CellDisambiguationInterface {

	// private final static String DISPROXYURL =
	// "http://theseus.dimis.fim.uni-passau.de:8080/code-disambiguationserver/disambiguation/disambiguate-proxy";
	private final static String DISPROXYURL = "http://localhost:8080/code-disambiguationserver/disambiguation/disambiguate-proxy";

	private static CellDisAlgorithm_CSDomain instance;

	public synchronized static CellDisAlgorithm_CSDomain getInstance() {
		if (instance == null) {
			instance = new CellDisAlgorithm_CSDomain();
		}
		return instance;
	}

	private CellDisAlgorithm_CSDomain() {
		super();
	}

	@Override
	public void disambiguateCells(final TableColumn col) {
		final List<TableCell> cellList = col.getCellList();

		// 1. Step
		// Disambiguate TableCells
		final DisambiguationResponse response = queryDisService(cellList);

		// 2. Step
		// Set disambiguated content
		for (int j = 0; j < cellList.size(); j++) {
			final TableCell cell = cellList.get(j);
			final Response entResponse = response
					.getTasks().get(j);
			final List<DisambiguatedEntity> entList = entResponse
					.getDisEntities();
			if (!entList.isEmpty()) {
				cell.setDisambiguatedContent(entResponse.getDisEntities()
						.get(0).getEntityUri());
				cell.setDisambigutedContentString(entResponse.getDisEntities()
						.get(0).getText());
			}
			cell.setDisambiguatedEntities(entList);
		}
	}

	private DisambiguationResponse queryDisService(
			final List<TableCell> tableCells) {
		DisambiguationResponse res = new DisambiguationResponse();
		final DisambiguationRequest disRequest = new DisambiguationRequest();
		disRequest.setDocumentUri("tableDisambiguationCSIndex");
		final List<EntityDisambiguationDPO> lst = new LinkedList<EntityDisambiguationDPO>();
		for (final TableCell cell : tableCells) {
			final EntityDisambiguationDPO ent = new EntityDisambiguationDPO();
			ent.setSetting("CSTable");
			String sfs = cell.getCellContent();
			ent.setSelectedText(sfs);
			ent.setStartPosition(-1);
			lst.add(ent);
		}
		disRequest.setSurfaceFormsToDisambiguate(lst);

		final ObjectMapper mapper = new ObjectMapper();
		String json = null;
		byte[] jsonByteString = null;
		try {
			json = mapper.writeValueAsString(disRequest);
			jsonByteString = json.getBytes("UTF-8");
		} catch (final JsonParseException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		} catch (final JsonMappingException e1) {
			Logger.getRootLogger().error(e1.getStackTrace());
		} catch (final IOException e2) {
			Logger.getRootLogger().error(e2.getStackTrace());
		}

		Header[] headers = { new BasicHeader("Accept", "application/json"),
				new BasicHeader("content-type", "application/json") };
		ByteArrayEntity ent = new ByteArrayEntity(jsonByteString,
				ContentType.create("application/json"));
		String resStr = ServiceQueries.httpPostRequest(DISPROXYURL, ent,
				headers);

		try {
			res = mapper.readValue(resStr, DisambiguationResponse.class);
		} catch (final JsonParseException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		} catch (final JsonMappingException e1) {
			Logger.getRootLogger().error(e1.getStackTrace());
		} catch (final IOException e2) {
			Logger.getRootLogger().error(e2.getStackTrace());
		}
		return res;
	}
}
