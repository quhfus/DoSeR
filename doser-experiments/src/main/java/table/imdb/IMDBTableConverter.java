package table.imdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class IMDBTableConverter {

	public static String RAWDIRECTORY = "/home/quh/Arbeitsfläche/Table Disambiguation Data sets/imdb_raw/";
	public static String TRIPPLEFILE = "/home/quh/Arbeitsfläche/Table Disambiguation Data sets/freebase_links_en.nt";
	public static String GTDIRECTORY = "/home/quh/Arbeitsfläche/Table Disambiguation Data sets/imdb_entity_keys/";
	public static final String OUTPUTFILE = "/home/quh/Arbeitsfläche/Table Disambiguation Data sets/imdb_columns.txt";
	
	private HashMap<String, String> uriconversion;
	private HashMap<Integer, String> groundtruth;

	private PrintWriter writer;

	public IMDBTableConverter() {
		super();
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
			writer.append(files[i].getName());
			writer.append(System.lineSeparator());
			File gtfile = new File(GTDIRECTORY + files[i].getName() + ".keys");
//			System.out.println(GTDIRECTORY + files[i].getName() + ".keys");
			readGroundtruthFile(gtfile);
			processFile(files[i]);
			writer.append(System.lineSeparator());
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
		Converter parser = new Converter(writer, groundtruth);
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
				if(uriconversion.containsKey(freebaseOrig)) {
					String uris = uriconversion.get(freebaseOrig);
					uris += ","+dbpediaUri;
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
		this.groundtruth = new HashMap<Integer, String>();
		try {
			reader = new BufferedReader(new FileReader(gt));
			String line = null;
			while ((line = reader.readLine()) != null) {
				int row = Integer.valueOf(line.split(",")[0]);
				// int row = Integer.valueOf(line.replaceAll(",*", ""));
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
		
		private boolean isRelevantCell = false;
		private boolean isCorrectTable = false;
		private boolean checkText = false;
		private int rowCounter = -1;
		
		private PrintWriter writer;
		
		Map<Integer, String> groundtruth;
		
		Converter(PrintWriter writer, HashMap<Integer, String> groundtruth) {
			super();
			this.writer = writer;
			this.groundtruth = groundtruth;
		}
		
		public void parse(Reader in) throws IOException {
			ParserDelegator delegator = new ParserDelegator();
			// the third parameter is TRUE to ignore charset directive
			delegator.parse(in, this, Boolean.TRUE);
		}

		private boolean isRelevantTable(String table) {
			return table.contains("cast_list");
		}

		public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
			if (t.toString().equals("table") && isRelevantTable(a.toString())) {
				isCorrectTable = true;
				rowCounter = -1;
			} else if (isCorrectTable && t.toString().equals("a")) {
				@SuppressWarnings("rawtypes")
				Enumeration e = a.getAttributeNames();
				boolean isUrl = false;
				boolean ishref = false;
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj.toString().equalsIgnoreCase("href")) {
						ishref = true;
					} else if (obj.toString().equalsIgnoreCase("itemprop")) {
						String prop = a.getAttribute(obj).toString();
						if (prop.equals("url")) {
							isUrl = true;
						}
					}
				}
				if (isUrl && ishref) {
					isRelevantCell = true;
				}
			} else if (isCorrectTable && isRelevantCell && t.toString().equals("span")) {
				@SuppressWarnings("rawtypes")
				Enumeration e = a.getAttributeNames();
				boolean isName = false;
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj.toString().equalsIgnoreCase("itemprop")) {
						String prop = a.getAttribute(obj).toString();
						if (prop.equals("name")) {
							isName = true;
							break;
						}
					}
				}
				if (isName) {
					rowCounter++;
					checkText = true;
				}

			}
		}

		public void handleEndTag(HTML.Tag t, int pos) {
			if (t.toString().equals("table") && isCorrectTable) {
				isCorrectTable = false;
			} else if (t.toString().equals("a") && isCorrectTable && isRelevantCell) {
				isRelevantCell = false;
			} else if (t.toString().equals("span") && isCorrectTable && isRelevantCell && checkText) {
				checkText = false;
			}
		}

		public void handleText(char[] text, int pos) {
			String s = new String(text);
			if (checkText) {
				writer.append(s);
				writer.append("\t");
				String gt = this.groundtruth.get(rowCounter);
				System.out.println(this.groundtruth.toString());
				System.out.println(rowCounter);
				String set = uriconversion.get(gt);
				if (set == null) {
					System.out.println("Appebden tun mir");
					writer.append("\n");
				} else {
					StringBuilder builder = new StringBuilder();
//					for (String se : set) {
						builder.append(set);
//					}
					String convertedGt = builder.toString();
					writer.append(convertedGt);
					writer.append("\n");
				}
				writer.flush();
			}
		}
		
	}

	public static void main(String args[]) {
		IMDBTableConverter imdbConverter = new IMDBTableConverter();
		imdbConverter.readTripples();
		System.out.println("Finished Tripple Reading");
		imdbConverter.readFile(new File(RAWDIRECTORY));
	}
}
