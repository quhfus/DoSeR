package table.imdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import experiments.table.limaye.corrected.Table;
import experiments.table.limaye.corrected.Table.Column;
import experiments.table.limaye.corrected.Table.Column.Cell;

public class MusicBrainzConverter {

	public static String RAWDIRECTORY = "/home/quh/Arbeitsfläche/Table Disambiguation Data sets/musicBrainz_raw/";
	public static String TRIPPLEFILE = "/home/quh/Arbeitsfläche/Table Disambiguation Data sets/freebase_links_en.nt";
	public static String GTDIRECTORY = "/home/quh/Arbeitsfläche/Table Disambiguation Data sets/musicbrainz_entity_keys/";
	public static final String OUTPUTFILE = "/home/quh/Arbeitsfläche/Table Disambiguation Data sets/musicbrainz_columns.txt";

	private HashMap<String, String> uriconversion;
	private HashMap<String, String> groundtruth;

	private PrintWriter writer;

	private String filename;

	public MusicBrainzConverter() {
		super();
		this.filename = "";
		this.uriconversion = new HashMap<String, String>();
		try {
			writer = new PrintWriter(new File(OUTPUTFILE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void readFile(File file) {
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			this.filename = files[i].getName();
			File gtfile = new File(GTDIRECTORY + files[i].getName() + ".keys");
			readGroundtruthFile(gtfile);
			processFile(files[i]);
		}
	}

	public void processFile(File input) {
		String c = "";
		try {
			String line = null;
			BufferedReader reader = new BufferedReader(new FileReader(input));
			while ((line = reader.readLine()) != null) {
				c += line;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		convert(c);
	}

	public void convert(String html) {
		Converter parser = new Converter(groundtruth);
		Reader in = new StringReader(html);
		try {
			// the HTML to convert
			parser.parse(in);
		} catch (Exception e) {
		} finally {
			try {
				in.close();
			} catch (IOException ioe) {
				// this should never happen
			}
		}
		parser.integrateGT();
		Table t = parser.getTable();
		int colnr = t.getNumberofColumns();
		int max = 2;
		if (colnr < max) {
			max = colnr;
		}
		for (int i = 0; i < max; i++) {
			writer.append(filename);
			writer.append(System.lineSeparator());
			Column c = t.getColumn(i);
			List<Cell> cellList = c.getCellList();
			for (Cell cell : cellList) {
				writer.append(cell.getCellContent().replaceAll("\\n\\r", "") + "\t" + cell.getGt());
				writer.append(System.lineSeparator());
			}
			writer.append(System.lineSeparator());
		}
	}

	public void readTripples() {
		File nfile = new File(TRIPPLEFILE);
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(nfile));
			String line = null;
			reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] splitter = line.split(" ");
				String freebaseOrig = splitter[splitter.length - 2];
				// Freebase uri
				freebaseOrig = freebaseOrig.replaceAll("<http://rdf.freebase.com/ns", "").replaceAll(">", "")
						.replaceAll("\\.", "/");

				String dbpediaUri = splitter[0];
				dbpediaUri = dbpediaUri.replaceAll("<|>", "");
				if (uriconversion.containsKey(freebaseOrig)) {
					String uris = uriconversion.get(freebaseOrig);
					uris += "," + dbpediaUri;
					uriconversion.put(freebaseOrig, uris);
				} else {
					uriconversion.put(freebaseOrig, dbpediaUri);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void readGroundtruthFile(File gt) {
		BufferedReader reader = null;
		this.groundtruth = new HashMap<String, String>();
		try {
			reader = new BufferedReader(new FileReader(gt));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String row = line.split("=")[0];
				String freebaseGT = line.replaceAll(".*=", "");
				freebaseGT = freebaseGT.substring(0, freebaseGT.length() - 1);
				groundtruth.put(row, freebaseGT);
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
		}
	}

	class Converter extends HTMLEditorKit.ParserCallback {

		private boolean isCorrectTable = false;
		private boolean checkText = false;
		private boolean isCorrectCell = false;
		private int columnCounter = 0;

		private Table table;

		Map<String, String> groundtruth;

		Converter(HashMap<String, String> groundtruth) {
			super();
			this.groundtruth = groundtruth;
			this.table = new Table();
		}

		public void parse(Reader in) throws IOException {
			ParserDelegator delegator = new ParserDelegator();
			// the third parameter is TRUE to ignore charset directive
			delegator.parse(in, this, Boolean.TRUE);
		}

		public Table getTable() {
			return table;
		}

		public void integrateGT() {
			for (Map.Entry<String, String> entry : groundtruth.entrySet()) {
				String pos = entry.getKey();
				String[] splitter = pos.split(",");
				int columnNr = Integer.valueOf(splitter[1]);
				int cellNr = Integer.valueOf(splitter[0]);
				Column col = table.getColumn(columnNr);
				if (col != null) {
					List<Cell> cellList = col.getCellList();
					if (uriconversion.containsKey(entry.getValue())) {
						String wikigt = uriconversion.get(entry.getValue());
						cellList.get(cellNr).setGt(wikigt);
					} else {
						cellList.get(cellNr).setGt("NULL");
					}
				}
			}
		}

		private boolean isRelevantTable(String table) {
			return table.contains("tbl");
		}

		public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
			if (t.toString().equals("table") && isRelevantTable(a.toString())) {
				isCorrectTable = true;
			} else if (isCorrectTable && t.toString().equals("tr")) {
			} else if (isCorrectTable && t.toString().equals("td")) {
				isCorrectCell = true;
			} else if (isCorrectTable && t.toString().equals("a") && isCorrectCell) {
				int tablecols = table.getNumberofColumns();
				if (tablecols < (columnCounter + 1)) {
					System.out.println("ALSO BEIM ADDEN SIND WIR");
					table.addColumn("");
				}
				columnCounter++;
				checkText = true;
			}

		}

		public void handleEndTag(HTML.Tag t, int pos) {
			if (t.toString().equals("table") && isCorrectTable) {
				isCorrectTable = false;
			} else if (t.toString().equals("a") && isCorrectTable && checkText && isCorrectCell) {
				checkText = false;
			} else if (t.toString().equals("td") && isCorrectTable) {
				isCorrectCell = false;
			} else if (t.toString().equals("tr") && isCorrectTable) {
				System.out.println("ICH REETTTTTTEEEE");
				columnCounter = 0;
			}
		}

		public void handleText(char[] text, int pos) {
			String s = new String(text);
			if (checkText) {
				Column c = table.getColumn(columnCounter - 1);
				System.out.println(columnCounter - 1);
				System.out.println(s);
				c.addCell(s);
			}
		}

	}

	public static void main(String args[]) {
		MusicBrainzConverter imdbConverter = new MusicBrainzConverter();
		imdbConverter.readTripples();
		System.out.println("Finished Tripple Reading");
		imdbConverter.readFile(new File(RAWDIRECTORY));
	}
}
