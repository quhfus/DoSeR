package doser.entitydisambiguation.table.celldisambiguation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import doser.algorithms.MajorityVoteAlgorithm;
import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.DisambiguationRequest;
import doser.entitydisambiguation.dpo.DisambiguationResponse;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.dpo.Position;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.table.logic.TableCell;
import doser.entitydisambiguation.table.logic.TableColumn;
import doser.entitydisambiguation.table.logic.Type;
import doser.tools.RDFGraphOperations;
import doser.tools.ServiceQueries;

public final class CellDisAlgorithm_Standard implements
		CellDisambiguationInterface {

	private final static String DISPROXYURL = "http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/disambiguation/disambiguate-proxy";

	private static CellDisAlgorithm_Standard instance;

	private final static String REGEXLABELBRACKET = "[A-Za-z0-9_ \\t\\r\\n\\v\\f]+([(][A-Za-z0-9_ \\t\\r\\n\\v\\f]+[)])[A-Za-z0-9_ \\t\\r\\n\\v\\f]*";

	private final static String[] UNRELEVANTTERMS = { "births", "deaths" };

	private final static String[] UNRRELEVANTTYPES = { "http://dbpedia.org/resource/Category:Living_people" };

	public synchronized static CellDisAlgorithm_Standard getInstance() {
		if (instance == null) {
			instance = new CellDisAlgorithm_Standard();
		}
		return instance;
	}

	private List<TableCell> cellsToDis;

	private List<TableCell> finishedDis;

	private Map<String, HashSet<Type>> memoryTypes;

	private List<Map.Entry<Type, Integer>> typeRanking;

	private MajorityVoteAlgorithm<Type> majorityVoteAlgorithm;

	private CellDisAlgorithm_Standard() {
		super();
		this.majorityVoteAlgorithm = new MajorityVoteAlgorithm<Type>();
	}

	private void checkFirstType() {
		final List<TableCell> toRemove = new LinkedList<TableCell>();
		if (!this.typeRanking.isEmpty()) {
			while (this.hasUnrelevantType()) {
				this.typeRanking.remove(0);
			}
			final Type majorType = this.typeRanking.get(0).getKey();
			for (int i = 0; i < this.cellsToDis.size(); i++) {
				final TableCell cell = this.cellsToDis.get(i);
				final List<DisambiguatedEntity> ents = cell
						.getDisambiguatedEntities();
				for (int j = 0; j < ents.size(); j++) {
					final DisambiguatedEntity ent = ents.get(j);
					HashSet<Type> hashSet = this.memoryTypes.get(ent
							.getEntityUri());
					boolean foundType = false;
					// BugFix - Crash NullPointerException on hashset access
					if (hashSet == null)
						hashSet = new HashSet<Type>();

					for (final Type currentType : hashSet) {
						if (currentType.equals(majorType)) {
							final String decodedString = this.decode(ents
									.get(j).getEntityUri());
							cell.setDisambiguatedContent(decodedString);
							cell.setDisambigutedContentString(ents.get(j)
									.getText());
							this.finishedDis.add(cell);
							foundType = true;
							break;
						}
					}
					if (foundType) {
						toRemove.add(cell);
						break;
					}
				}
			}
			for (int i = 0; i < toRemove.size(); i++) {
				for (int j = 0; j < this.cellsToDis.size(); j++) {
					if (this.cellsToDis.get(j).compareTo(toRemove.get(i)) == 0) {
						this.cellsToDis.remove(j);
						break;
					}
				}
			}
		}
	}

	private void createTypeRanking(final DisambiguationResponse res) {
		final List<Type> types = this.searchTypes(res);
		this.typeRanking = majorityVoteAlgorithm.getMajorityTypes(types);
	}

	private String decode(final String uri) {
		String res = uri;
		try {
			res = URLDecoder.decode(uri, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
		return res;
	}

	@Override
	public void disambiguateCells(final TableColumn col) {

		// 1. Disambiguate cell with Disambiguation service
		final DisambiguationResponse res = this.queryDisService(col
				.getCellList());
		for (int j = 0; j < res.getTasks().size(); j++) {
			col.getCellList()
					.get(j)
					.setDisambiguatedEntities(
							res.getTasks().get(j)
									.getDisEntities());
		}

		// 2. Type Extraction
		this.memoryTypes = new HashMap<String, HashSet<Type>>();
		this.createTypeRanking(res);

		// 3. Put all 1 item responses onto the stack
		this.finishedDis = new LinkedList<TableCell>();
		this.cellsToDis = new LinkedList<TableCell>();
		for (int j = 0; j < col.getCellList().size(); j++) {
			if (col.getCellList().get(j) != null) {
				final TableCell tableCell = col.getCellList().get(j);
				this.cellsToDis.add(tableCell);
			}
		}
		this.oneItemResponses(res, col);

		// checkConjunctions();

		// Check first string types
		// checkStringTypes();
		// 5. Check first type
		this.checkFirstType();

		// 6. Use first response item for all others.
		// disambiguateLevenshtein();
		this.disambiguateListItems();

		// 7. Remove all relevant type item of column
		col.resetTypes();
	}

	private void disambiguateListItems() {
		for (int i = 0; i < this.cellsToDis.size(); i++) {
			final List<DisambiguatedEntity> list = this.cellsToDis.get(i)
					.getDisambiguatedEntities();
			if (list.isEmpty()) {
				this.cellsToDis.get(i).setDisambiguatedContent(null);
				this.cellsToDis.get(i).setDisambigutedContentString(null);
			} else {
				final String decodedString = this.decode(list.get(0)
						.getEntityUri());
				this.cellsToDis.get(i).setDisambiguatedContent(decodedString);
				this.cellsToDis.get(i).setDisambigutedContentString(
						list.get(0).getText());
			}
		}
		// Optional: Put all cells to disambiguate on the final list
		this.finishedDis.addAll(this.cellsToDis);
	}

	private List<Type> getTypes(final List<DisambiguatedEntity> entList) {
		final Map<Type, Integer> hash = new HashMap<Type, Integer>();
		for (final DisambiguatedEntity entity : entList) {
			final String uri = entity.getEntityUri();
			final Set<Type> types = RDFGraphOperations
					.getDbpediaCategoriesFromEntity(uri);
			final HashSet<Type> hashSet = new HashSet<Type>();
			for (final Type type : types) {
				hash.put(type, 0);
				hashSet.add(type);
			}
			this.memoryTypes.put(uri, hashSet);
		}
		final List<Type> list = new LinkedList<Type>();
		for (final Entry<Type, Integer> entry : hash.entrySet()) {
			list.add(entry.getKey());
		}
		return list;
	}

	private boolean hasUnrelevantType() {
		boolean res = false;
		for (final String element : UNRRELEVANTTYPES) {
			if (element.equalsIgnoreCase(this.typeRanking.get(0).getKey()
					.getUri())) {
				res = true;
				break;
			}
		}
		if (!res) {
			for (final String element : UNRELEVANTTERMS) {
				if (this.typeRanking.get(0).getKey().getUri().contains(element)) {
					res = true;
					break;
				}
			}
		}
		return res;
	}

	/**
	 * ToDo Cell have disambiguated entities
	 * 
	 * @param res
	 * @param col
	 */
	private void oneItemResponses(final DisambiguationResponse res,
			final TableColumn col) {
		final List<Response> responses = res.getTasks();
		for (int i = 0; i < responses.size(); i++) {
			final Response ent = responses.get(i);
			final List<DisambiguatedEntity> entities = ent.getDisEntities();
			if (entities.size() == 1) {
				final String decodedString = this.decode(entities.get(0)
						.getEntityUri());
				col.getCellList().get(i).setDisambiguatedContent(decodedString);
				col.getCellList()
						.get(i)
						.setDisambigutedContentString(entities.get(0).getText());
				this.finishedDis.add(col.getCellList().get(i));
				if (col.getCellList().get(i) != null) {
					this.removeItemFromList(col.getCellList().get(i),
							this.cellsToDis);
				}
			}
		}
	}

	private DisambiguationResponse queryDisService(
			final List<TableCell> tableCells) {
		DisambiguationResponse res = new DisambiguationResponse();
		final DisambiguationRequest disRequest = new DisambiguationRequest();
		disRequest.setDocumentUri("tableDisambiguation");
		final List<EntityDisambiguationDPO> lst = new LinkedList<EntityDisambiguationDPO>();
		for (int i = 0; i < tableCells.size(); i++) {
			final EntityDisambiguationDPO ent = new EntityDisambiguationDPO();
			ent.setSetting("NoContext");
			String cellContent = tableCells.get(i).getCellContent();
			if (cellContent.matches(REGEXLABELBRACKET)) {
				final String splitter[] = cellContent
						.split("[(][A-Za-z0-9_ \\t\\r\\n\\v\\f]+[)]");
				if (splitter.length == 2) {
					cellContent = splitter[0] + " " + splitter[1];
				} else {
					cellContent = splitter[0];
				}
			}
			ent.setContext("");
			String sfs = cellContent;
			ent.setSelectedText(sfs);
			final List<Position> posList = new LinkedList<Position>();
			ent.setPosition(posList);
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

	private void removeItemFromList(final TableCell cell,
			final List<TableCell> list) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).compareTo(cell) == 0) {
				list.remove(i);
				break;
			}
		}
	}

	public List<Type> searchTypes(final DisambiguationResponse response) {
		final List<Response> lst = response.getTasks();
		final List<Type> res = new LinkedList<Type>();

		for (final Response entity : lst) {
			final List<DisambiguatedEntity> ent = entity.getDisEntities();
			res.addAll(this.getTypes(ent));
		}
		return res;
	}
}
