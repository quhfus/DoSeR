package word2vec.corpuscreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;

import word2vec.corpuscreation.CreateBiomedicalEntityCorpus.CalbCPubMedID.Concept;
import word2vec.corpuscreation.CreateBiomedicalEntityCorpus.CalbCPubMedID.Entity;

public class CreateBiomedicalEntityCorpus {

	private static final String inputFile = "/home/quh/Arbeitsfläche/Entpackung/Arbeitsfläche/Code_Data/Calbc/output.json";
	private static final String output = "/home/quh/Arbeitsfläche/CalbcSmallEntityCorpus.dat";

	public static void main(String[] args) {
		CreateBiomedicalEntityCorpus corpus = new CreateBiomedicalEntityCorpus();
		corpus.action();
	}

	public CreateBiomedicalEntityCorpus() {
		super();
	}

	public void action() {
		Gson gson = new Gson();
		File f = new File(inputFile);
		PrintWriter writer = null;
		String line = null;
		BufferedReader reader = null;

		try {
			writer = new PrintWriter(output);
			reader = new BufferedReader(new FileReader(f));
			while ((line = reader.readLine()) != null) {
				if (!line.equals("")) {
					CalbCPubMedID id = gson.fromJson(line, CalbCPubMedID.class);
					List<Entity> entities = id.getEntityList();
					for (Entity e : entities) {
						List<Concept> conceptList = e.getConceptList();
						for (Concept c : conceptList) {
							if (!generateID(c.getUrl()).equals("")) {
								writer.print(c.getUrl() + " ");
							}
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				writer.close();
			}
		}
	}

	private String generateID(String line) {
		String[] splitter = line.split(":");

		String link = "";
		if (splitter[1].equalsIgnoreCase("uniprot") && !splitter[2].equalsIgnoreCase("") && splitter[2] != null) {
			link = "UN_" + splitter[2];
		} else if (splitter[1].equalsIgnoreCase("entrezgene") && !splitter[2].equalsIgnoreCase("")
				&& splitter[2] != null) {
			link = "NC_" + splitter[2];
		} else if (splitter[1].equalsIgnoreCase("umls") && !splitter[2].equalsIgnoreCase("") && splitter[2] != null) {
			link = "LI_" + splitter[2];
		} else if (splitter[1].equalsIgnoreCase("ncbi") && !splitter[2].equalsIgnoreCase("") && splitter[2] != null) {
			link = "NC_" + splitter[2];
		} else if (splitter[1].equalsIgnoreCase("disease") && !splitter[2].equalsIgnoreCase("")
				&& splitter[2] != null) {
			link = "LI_" + splitter[2];
		}
		return link;
	}

	class CalbCPubMedID {

		private String title;

		private String abs;

		private Metadata metadata;

		private List<Entity> entityList;

		private String id;

		public CalbCPubMedID() {
			abs = "";
			entityList = new LinkedList<Entity>();
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public String getId() {
			return id;
		}

		public String getAbs() {
			return abs;
		}

		public void setAbs(String abs) {
			this.abs = abs;
		}

		public List<Entity> getEntityList() {
			return entityList;
		}

		public void addConcept(Entity entity) {
			entityList.add(entity);
		}

		public Metadata getMetadata() {
			return metadata;
		}

		public void setMetadata(Metadata metadata) {
			this.metadata = metadata;
		}

		public void setEntityList(List<Entity> entityList) {
			this.entityList = entityList;
		}

		public void concatAbstract(String newabs) {
			abs += newabs;
		}

		public class Entity {

			private String keyword;

			private List<Concept> conceptList;

			private boolean isTitle;

			private int position;

			public Entity() {
				conceptList = new LinkedList<Concept>();
				position = 0;
			}

			public Entity(String keyword, boolean isTitle, int position) {
				this.keyword = keyword;
				this.isTitle = isTitle;
				this.position = position;
				conceptList = new LinkedList<Concept>();
			}

			public void addConcept(Concept concept) {
				conceptList.add(concept);
			}

			public String getKeyword() {
				return keyword;
			}

			public void setKeyword(String keyword) {
				this.keyword = keyword;
			}

			public List<Concept> getConceptList() {
				return conceptList;
			}

			public void setConceptList(List<Concept> conceptList) {
				this.conceptList = conceptList;
			}

			public boolean isTitle() {
				return isTitle;
			}

			public void setTitle(boolean isTitle) {
				this.isTitle = isTitle;
			}

			public int getPosition() {
				return position;
			}

			public void setPosition(int position) {
				this.position = position;
			}

			///////////////// Experiment Methode //////////////////////////////
			public boolean hasNCBIConcepts() {
				boolean hasNCBI = false;
				for (Iterator<Concept> iterator = conceptList.iterator(); iterator.hasNext();) {
					Concept con = iterator.next();
					if (con.getUrl().contains("ncbi")) {
						hasNCBI = true;
						break;
					}
				}
				return hasNCBI;
			}
		}

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

		public class Author {

			private String name;

			private String forename;

			private String shortname;

			public Author() {
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public String getForename() {
				return forename;
			}

			public void setForename(String forename) {
				this.forename = forename;
			}

			public String getShortname() {
				return shortname;
			}

			public void setShortname(String shortname) {
				this.shortname = shortname;
			}

		}

		public class Concept {

			private String url;

			public Concept() {
			}

			public String getUrl() {
				return url;
			}

			public void setUrl(String url) {
				this.url = url;
			}
		}
	}

}
