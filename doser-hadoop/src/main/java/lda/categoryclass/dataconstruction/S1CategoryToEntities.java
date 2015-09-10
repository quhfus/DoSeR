package lda.categoryclass.dataconstruction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import hbase.operations.HBaseOperations;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class S1CategoryToEntities {

	public static final void main(String[] args) throws MalformedURLException, FileNotFoundException {
		Model model = ModelFactory.createDefaultModel();
		model.read(args[0]);
		StmtIterator iter = model.listStatements();
		System.out.println("Start");
		while (iter.hasNext()) {
			Statement stmt = iter.next();
			Resource subject = stmt.getSubject();
			String url[] = subject.getURI().split("/");
			String subjectIdent = url[url.length - 1];
			RDFNode object = stmt.getObject();
			String s[] = object.asResource().getURI().split("/");
			String objIdent = s[s.length - 1];
			try {
				System.out.println(objIdent+"    "+subjectIdent);
				HBaseOperations.getInstance().addRecord("DBPEDIA_CatToEnts", objIdent, "data", String.valueOf(subjectIdent.hashCode()), subjectIdent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
