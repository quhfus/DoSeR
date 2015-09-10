package experiments.collective.entdoccentric;

import java.io.IOException;
import java.util.Iterator;

import experiments.collective.entdoccentric.StandardQueryDataObject.EntityObject;
import experiments.collective.entdoccentric.calbc.CalbCPubMedID;
import experiments.collective.entdoccentric.calbc.Entity;

/**
 * Iteriert über den kleinen CalbC und liefert jedes Entity zur Evaluierung
 * 
 * @author zwicklbauer
 * 
 */
public class CompleteCalbCSGeneration extends QueryDataGeneration {

	private StandardQueryDataObject tempDataObject;

	private CalbCPubMedID entry;

	private Iterator<Entity> it;

	public CompleteCalbCSGeneration() {
		super();
		it = null;
	}

	@Override
	public StandardQueryDataObject hasNext() {
		tempDataObject = null;
		return extractDataObject();
	}

	private StandardQueryDataObject extractDataObject() {
		String str = "";
		String jsonString = "";
		if (it == null) {
			// Nächstes Dokument anfordern
			try {
				while ((str = bufferedReader.readLine()) != null) {
					if (!str.equalsIgnoreCase("")) {
						jsonString += str;
					} else {
						break;
					}
				}
				entry = gson.fromJson(jsonString, CalbCPubMedID.class);
				if (entry == null) {
					return null;
				}
				it = entry.getEntityList().iterator();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		int id = 0;
		tempDataObject = new StandardQueryDataObject(
				Integer.parseInt(entry.getId()));
		while (it.hasNext()) {
			Entity ent = it.next();
			EntityObject e = new EntityObject();
			if (ent.isTitle()) {
				e.setContext(extractText(ent.getPosition(), entry.getTitle()));
			} else {
				e.setContext(extractText(ent.getPosition(), entry.getAbs()));
			}
			e.setText(ent.getKeyword());
			e.setQueryId(Integer.parseInt(entry.getId()) + id);
			e.setResultLinks(setResultLinks(tempDataObject,
					ent.getConceptList()));
			tempDataObject.addEntity(e);
		}
		it = null;
		return tempDataObject;
	}
}
