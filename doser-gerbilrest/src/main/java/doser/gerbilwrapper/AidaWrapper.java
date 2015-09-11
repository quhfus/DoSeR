package doser.gerbilwrapper;
//
//import java.io.IOException;
//import java.io.Reader;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import mpi.aida.Disambiguator;
//import mpi.aida.access.DataAccess;
//import mpi.aida.config.settings.DisambiguationSettings;
//import mpi.aida.config.settings.PreparationSettings;
//import mpi.aida.config.settings.disambiguation.CocktailPartyDisambiguationSettings;
//import mpi.aida.config.settings.preparation.ManualPreparationSettings;
//import mpi.aida.data.DisambiguationResults;
//import mpi.aida.data.Entities;
//import mpi.aida.data.EntityMetaData;
//import mpi.aida.data.KBIdentifiedEntity;
//import mpi.aida.data.PreparedInput;
//import mpi.aida.data.ResultMention;
//import mpi.aida.preparator.Preparator;
//
//import org.aksw.gerbil.transfer.nif.Document;
//import org.aksw.gerbil.transfer.nif.Marking;
//import org.aksw.gerbil.transfer.nif.TurtleNIFDocumentCreator;
//import org.aksw.gerbil.transfer.nif.TurtleNIFDocumentParser;
//import org.aksw.gerbil.transfer.nif.data.NamedEntity;
//import org.aksw.gerbil.transfer.nif.data.SpanImpl;
//import org.restlet.representation.Representation;
//import org.restlet.resource.Post;
//import org.restlet.resource.ServerResource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import doser.tools.indexcreation.WikiPediaUriConverter;
//
//public class AidaWrapper extends ServerResource {
//
//	private static final Logger LOGGER = LoggerFactory
//			.getLogger(AidaWrapper.class);
//
//	private TurtleNIFDocumentParser parser = new TurtleNIFDocumentParser();
//	private TurtleNIFDocumentCreator creator = new TurtleNIFDocumentCreator();
//
//	@Post
//	public String accept(Representation request) {
//
//		Reader inputReader;
//		try {
//			inputReader = request.getReader();
//		} catch (IOException e) {
//			LOGGER.error("Exception while reading request.", e);
//			return "";
//		}
//		// ... this is only the parsing of an incoming document
//		Document document;
//		try {
//			document = parser.getDocumentFromNIFReader(inputReader);
//		} catch (Exception e) {
//			LOGGER.error("Exception while reading request.", e);
//			return "";
//		}
//		System.out.println("DocumentNr: " + document.getDocumentURI());
//		String documentText = document.getText();
//		StringBuilder markedText = new StringBuilder();
//
//		List<Marking> markings = document.getMarkings();
//		List<OrderedSpanImpl> oMarkings = new LinkedList<OrderedSpanImpl>();
//		for (Marking m : markings) {
//			SpanImpl impl = (SpanImpl) m;
//			oMarkings.add(new OrderedSpanImpl(impl.getStartPosition(), impl
//					.getLength()));
//		}
//		Collections.sort(oMarkings);
//		int lastPosition = 0;
//		for (OrderedSpanImpl mark : oMarkings) {
//			String sf = document.getText().substring(mark.getStartPosition(),
//					(mark.getStartPosition() + mark.getLength()));
//			if (lastPosition >= mark.getStartPosition()) {
////				markedText.append(documentText.substring(mark.getStartPosition(), mark.getStartPosition() + mark.getLength()));
//				markedText.append(" [[");
//				markedText.append(sf.trim());
//				markedText.append("]] ");
//				lastPosition = mark.getStartPosition() + mark.getLength();
//			} else {
//				markedText.append(documentText.substring(lastPosition,
//						mark.getStartPosition()));
//				markedText.append(" [[");
//				markedText.append(sf.trim());
//				markedText.append("]] ");
//				lastPosition = mark.getStartPosition() + mark.getLength();
//			}
//		}
//		markedText.append(documentText.substring(lastPosition,
//				documentText.length()));
//
//		String text = markedText.toString();
//		text = text.replaceAll(" +", " ");
//		System.out.println(text);
//		PreparationSettings prepSettings = new ManualPreparationSettings();
//		Preparator p = new Preparator();
//		List<Marking> entities = new ArrayList<Marking>(markings.size());
//		try {
//			PreparedInput input = p
//					.prepare(markedText.toString(), prepSettings);
//
//			// Disambiguate the input with the graph coherence algorithm.
//			DisambiguationSettings disSettings = new CocktailPartyDisambiguationSettings();
//			Disambiguator d = new Disambiguator(input, disSettings);
//			DisambiguationResults results = d.disambiguate();
//
//			Set<KBIdentifiedEntity> ents = new HashSet<KBIdentifiedEntity>();
//			for (ResultMention rm : results.getResultMentions()) {
//				ents.add(results.getBestEntity(rm).getKbEntity());
//			}
//
//			Map<KBIdentifiedEntity, EntityMetaData> entitiesMetaData = DataAccess
//					.getEntitiesMetaData(ents);
//
//			List<ResultMention> resList = results.getResultMentions();
//
//			if (oMarkings.size() > 0) {
//				try {
//					for (int i = 0; i < oMarkings.size(); ++i) {
//						SpanImpl span = (SpanImpl) oMarkings.get(i);
//						ResultMention mention = resList.get(i);
//						KBIdentifiedEntity ent = results.getBestEntity(mention)
//								.getKbEntity();
//						if (!Entities.isOokbEntity(ent)) {
//							String url = entitiesMetaData.get(ent).getUrl();
//							url = url.replaceAll(
//									"http://en.wikipedia.org/wiki/", "");
//							url = "http://dbpedia.org/resource/"
//									+ WikiPediaUriConverter
//											.createConformDBpediaUriEndingfromEncodedString(url);
//							entities.add(new NamedEntity(span
//									.getStartPosition(), span.getLength(), url));
//						}
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		// ... this new list is added to the document and the document is
//		// send back to GERBIL
//		document.setMarkings(entities);
//		String nifDocument = creator.getDocumentAsNIFString(document);
//		return nifDocument;
//	}
//
//	class OrderedSpanImpl extends SpanImpl implements
//			Comparable<OrderedSpanImpl> {
//
//		public OrderedSpanImpl(int startPosition, int length) {
//			super(startPosition, length);
//		}
//
//		@Override
//		public int compareTo(OrderedSpanImpl o) {
//			if (this.startPosition < o.getStartPosition()) {
//				return -1;
//			} else if (this.startPosition == o.getStartPosition()) {
//				return 0;
//			} else {
//				return 1;
//			}
//		}
//	}
//
//	public static void main(String[] args) {
//		String documentText = "Georg Bush is the president of the United States.";
//		List<Marking> markings = new LinkedList<Marking>();
//		markings.add(new SpanImpl(0, 13));
//		markings.add(new SpanImpl(6, 4));
//		markings.add(new SpanImpl(18, 9));
//		markings.add(new SpanImpl(35, 13));
//
//		StringBuilder markedText = new StringBuilder();
//
//		int lastPosition = 0;
//		for (Marking mark : markings) {
//			SpanImpl span = (SpanImpl) mark;
//			String sf = documentText.substring(span.getStartPosition(),
//					(span.getStartPosition() + span.getLength()));
//			if (lastPosition >= span.getStartPosition()) {
////				markedText.append(documentText.substring(span.getStartPosition(), span.getStartPosition() + span.getLength()));
//				markedText.append(" [[");
//				markedText.append(sf.trim());
//				markedText.append("]] ");
//				lastPosition = span.getStartPosition() + span.getLength();
//			} else {
//				markedText.append(documentText.substring(lastPosition,
//						span.getStartPosition()));
//				markedText.append(" [[");
//				markedText.append(sf.trim());
//				markedText.append("]] ");
//				lastPosition = span.getStartPosition() + span.getLength();
//			}
//		}
//		markedText.append(documentText.substring(lastPosition,
//				documentText.length()));
//		System.out.println(markedText.toString());
//	}
//}
