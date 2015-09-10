package doser.gerbilwrapper;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.TurtleNIFDocumentCreator;
import org.aksw.gerbil.transfer.nif.TurtleNIFDocumentParser;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.SpanImpl;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
//import edu.illinois.cs.cogcomp.wikifier.common.GlobalParameters;
//import edu.illinois.cs.cogcomp.wikifier.inference.InferenceEngine;
//import edu.illinois.cs.cogcomp.wikifier.models.LinkingProblem;
//import edu.illinois.cs.cogcomp.wikifier.models.ReferenceInstance;
//
//public class IllinoisWrapper extends ServerResource {
//
//	private static final Logger LOGGER = LoggerFactory
//			.getLogger(IllinoisWrapper.class);
//
//	private TurtleNIFDocumentParser parser = new TurtleNIFDocumentParser();
//	private TurtleNIFDocumentCreator creator = new TurtleNIFDocumentCreator();
//
//	private InferenceEngine engine;
//
//	public IllinoisWrapper() {
//		super();
//		try {
//			GlobalParameters
//					.loadConfig("/home/zwicklbauer/Wikifier/configs/STAND_ALONE_GUROBI.xml");
//			this.engine = new InferenceEngine(false);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	@Post
//	public String accept(Representation request) {
//		List<ReferenceInstance> instances = new LinkedList<ReferenceInstance>();
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
//		String documentText = document.getText();
//		// If your system is only for entity linking, the document object
//		// should already contain a list of markings
//
//		// Now we have the text and a list of markings (this could be
//		// empty or contain Span objects which would mark the named
//		// entities inside the text) and could call your system for
//		// performing the entity linking task...
//
//		List<Marking> markings = document.getMarkings();
//		for (Marking mark : markings) {
//			SpanImpl span = (SpanImpl) mark;
//			String sf = document.getText().substring(span.getStartPosition(),
//					(span.getStartPosition() + span.getLength()));
//			ReferenceInstance instance = new ReferenceInstance(sf,
//					span.getStartPosition(),
//					(span.getStartPosition() + span.getLength()));
//			instances.add(instance);
//		}
//
//		
//		List<Marking> entities = new ArrayList<Marking>(markings.size());
//		if (markings.size() > 0) {
//			try {
//			TextAnnotation ta = GlobalParameters.curator
//					.getTextAnnotation(documentText);
//			LinkingProblem problem = new LinkingProblem("0", ta, instances);
//			engine.annotate(problem, null, false, false, 0.0);
//		
//			for (int i = 0; i < markings.size(); ++i) {
//				SpanImpl span = (SpanImpl) markings.get(i);
//				entities.add(new NamedEntity(span.getStartPosition(), span.getLength(), "http://dbpedia.org/resource/"+instances.get(i).getAssignedEntity().getTopTitle()));
//			}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		// ... this new list is added to the document and the document is
//		// send back to GERBIL
//		document.setMarkings(entities);
//		String nifDocument = creator.getDocumentAsNIFString(document);
//		return nifDocument;
//	}
//}
