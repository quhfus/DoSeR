package experiments.collective.entdoccentric.calbc;

import java.util.LinkedList;
import java.util.List;

public class Metadata {

	private List<Author> authorList;
	
	public Metadata() {		
		authorList = new LinkedList<Author>();
	}

	public List<Author> getAuthorList() {
		return authorList;
	}
	
	public void addAuthor(Author author) {
		authorList.add(author);
	}
	
}
