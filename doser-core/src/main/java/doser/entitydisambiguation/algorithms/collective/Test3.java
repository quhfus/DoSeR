package doser.entitydisambiguation.algorithms.collective;

import java.io.IOException;
import java.util.Collection;

import org.aksw.agdistis.datatypes.Document;
import org.aksw.agdistis.datatypes.DocumentText;
import org.aksw.agdistis.datatypes.NamedEntitiesInText;
import org.aksw.agdistis.datatypes.NamedEntityInText;

import Agdistis.CandidateUtil;
import Agdistis.Node;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class Test3 {

	public static void main(String[] args) {
		Document doc = new Document();
		doc.setDocumentId(0);
		doc.addTest(new DocumentText("West Virginia"));
		NamedEntitiesInText intext = new NamedEntitiesInText();
		NamedEntityInText in = new NamedEntityInText(0, 13, null);
		intext.addNamedEntity(in);
		doc.addNamedEntitiesInText(intext);

		DirectedSparseGraph<Node, String> graph = new DirectedSparseGraph<Node, String>();

		// 0) insert candidates into Text
		try {
			CandidateUtil util = CandidateUtil.getInstance();
			util.insertCandidatesIntoText(graph, doc, 0.7, true);
			Collection<Node> nodes = graph.getVertices();
			for (Node node : nodes) {
				System.out.println(node.getCandidateURI());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
