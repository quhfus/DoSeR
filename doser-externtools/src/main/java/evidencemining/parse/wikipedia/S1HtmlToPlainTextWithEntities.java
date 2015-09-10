package evidencemining.parse.wikipedia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * HTML to plain-text. Second step for Wikipedia evidence Mining. Each HTML file
 * is transformed into a plaintext file, only containing the plain text and
 * entity annotations. Structural elements like tables, headlines, references
 * will be removed.
 * 
 * @author Stefan Zwicklbauer
 */
public class S1HtmlToPlainTextWithEntities extends HTMLEditorKit.ParserCallback {

	public static String MAINDIR = "/mnt/storage/zwicklbauer/WikiParse/temp/dump/";
	public static String SAVEDIR = "/mnt/storage/zwicklbauer/WikiParse/temp/plain/";
	public static String REDIRECTFILE = "/home/zwicklbauer/HDTGeneration/redirects_en.nt";

	public static final int NUMBERTHREADS = 40;

	private StringBuffer stringBuffer;

	private Stack<IndexType> indentStack;

	private static HashMap<String, String> redirects = new HashMap<String, String>();

	private boolean isEntity;
	private boolean tableContent;
	private boolean isList;
	private boolean isHeader;
	private boolean isStyle;
	private StringBuffer substring;
	private static HashSet<String> filesHash = new HashSet<String>();
	private HashMap<String, Integer> writtenFiles;

	public static class IndexType {
		public String type;
		public int counter; // used for ordered lists

		public IndexType(String type) {
			this.type = type;
			counter = 0;
		}
	}

	public S1HtmlToPlainTextWithEntities() {
		super();
		stringBuffer = new StringBuffer();
		indentStack = new Stack<IndexType>();
		isEntity = false;
		tableContent = false;
		isList = false;
		isHeader = false;
		isStyle = false;
		substring = new StringBuffer();
		this.writtenFiles = new HashMap<String, Integer>();
	}

	public static void createFileHash(File dir) {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isDirectory()) {
				String filen = files[i].getName().replaceAll(".html", "");
				filen = filen.replaceAll("'", "%");
				try {
					filen = URLDecoder.decode(filen, "UTF-8");
					filen = URLEncoder.encode(filen, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				filesHash.add(filen);
			} else {
				createFileHash(files[i]);
			}
		}
	}

	public static void createRedirectHashMap(File main) {
		Model model = ModelFactory.createDefaultModel();
		model.read(REDIRECTFILE);
		StmtIterator iter = model.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			Resource subject = stmt.getSubject();
			String sourceurl = subject.toString().replaceAll(
					"http://dbpedia.org/resource/", "");

			try {
				sourceurl = URLDecoder.decode(sourceurl, "UTF-8");
				sourceurl = URLEncoder.encode(sourceurl, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			RDFNode object = stmt.getObject();
			String targeturl = "";
			if (object instanceof Resource) {
				targeturl = object.toString().replaceAll(
						"http://dbpedia.org/resource/", "");
				try {
					targeturl = URLDecoder.decode(targeturl, "UTF-8");
					targeturl = URLEncoder.encode(targeturl, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			if (filesHash.contains(targeturl)) {
				redirects.put(sourceurl, targeturl);
			}
		}
	}

	public static String convert(String html) {
		S1HtmlToPlainTextWithEntities parser = new S1HtmlToPlainTextWithEntities();
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
		return parser.getText();
	}

	public void parse(Reader in) throws IOException {
		ParserDelegator delegator = new ParserDelegator();
		// the third parameter is TRUE to ignore charset directive
		delegator.parse(in, this, Boolean.TRUE);
	}

	private boolean checkFile(String filename) {
		return filesHash.contains(filename);
	}

	public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
		if (t.toString().equals("a") && checkLink(a.toString())) {
			@SuppressWarnings("rawtypes")
			Enumeration e = a.getAttributeNames();
			Object object = null;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj.toString().equalsIgnoreCase("href")) {
					object = obj;
					break;
				}
			}
			if (!String.valueOf(a.getAttribute(object)).contains("#")) {
				String link = a.getAttribute(object).toString();
				link = link.replaceAll(".html", "");
				link = link.replaceAll("'", "%");
				try {
					link = URLDecoder.decode(link, "UTF-8");
					link = URLEncoder.encode(link, "UTF-8");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				if (checkFile(link)) {
					if (redirects.containsKey(link)) {
						String back = link;
						link = redirects.get(link);
						System.out.print("Redirect From: "+back+"To: "+link);
					}
					substring.append("<a " + object.toString() + "=\""
							+ link + "\">");
					isEntity = true;
				}
			}
		} else if (t.toString().equals("p")) {
			if (stringBuffer.length() > 0
					&& !stringBuffer.substring(stringBuffer.length() - 1)
							.equals("\n")) {
				newLine();
			}
			newLine();
		} else if (t.toString().equals("ol")) {
			indentStack.push(new IndexType("ol"));
			newLine();
		} else if (t.toString().equals("ul")) {
			isList = true;
			indentStack.push(new IndexType("ul"));
			newLine();
		} else if (t.toString().equals("li")) {
			indentStack.push(new IndexType("li"));
		} else if (t.toString().equals("dl")) {
			newLine();
		} else if (t.toString().equals("dt")) {
			newLine();
		} else if (t.toString().equals("dd")) {
			indentStack.push(new IndexType("dd"));
			newLine();
		} else if (t.toString().equals("h1") || t.toString().equals("h2")
				|| t.toString().equals("h3") || t.toString().equals("h4")
				|| t.toString().equals("h5") || t.toString().equals("h6")) {
			isHeader = true;
		} else if (t.toString().equals("style")) {
			isStyle = true;
		}
		if (t.toString().equals("table")) {
			tableContent = true;
		}
	}

	private boolean checkLink(String l) {
		if (l.contains("href=") && l.contains("id=w") && !tableContent
				&& !l.contains("template") && !l.contains("Template")
				&& !isList && !isHeader && !l.toLowerCase().contains("http:")
				&& !l.contains("/")) {
			return true;
		}
		return false;
	}

	private void newLine() {
	}

	public void handleEndTag(HTML.Tag t, int pos) {
		if (t.toString().equals("a") && isEntity) {
			String s[] = substring.toString().split(">");
			if (!s[1].matches("[ *]")) {
				substring.append("</a>");
				stringBuffer.append(substring.toString());
			}
			substring = new StringBuffer();
			isEntity = false;
		} else if (t.toString().equals("p")) {
			newLine();
		} else if (t.toString().equals("ol")) {
			indentStack.pop();
			;
			newLine();
		} else if (t.toString().equals("ul")) {
			indentStack.pop();
			;
			newLine();
			isList = false;
		} else if (t.toString().equals("li")) {
			indentStack.pop();
			;
			newLine();
		} else if (t.toString().equals("dd")) {
			indentStack.pop();
			;
		} else if (t.toString().equals("h1") || t.toString().equals("h2")
				|| t.toString().equals("h3") || t.toString().equals("h4")
				|| t.toString().equals("h5") || t.toString().equals("h6")) {
			isHeader = false;
		} else if (t.toString().equals("style")) {
			isStyle = false;
		}
		if (t.toString().equals("table")) {
			tableContent = false;
		}
	}

	public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
		if (t.toString().equals("br")) {
			newLine();
		}
	}

	public void handleText(char[] text, int pos) {

		String s = new String(text);
		s = s.replaceAll("\\[\\d*\\]", " ");
		s = s.replaceAll("<[0-9A-Za-z \\ /\n\r\t]*>", " ");
		s = s.replaceAll("[^A-Za-z0-9 \n\t.,!?]", " ");
		s = s.replaceAll("[\\s]+", " ");
		if (!tableContent && !isList && !s.contains("Template") && !isHeader
				&& !isStyle) {
			if (isEntity) {
				substring.append(s);
			} else {
				stringBuffer.append(s);
			}
		}
	}

	public String getText() {
		return stringBuffer.toString();
	}

	public static void main(String args[]) {
		File mainDirectory = new File(MAINDIR);
		S1HtmlToPlainTextWithEntities.createFileHash(mainDirectory);
		S1HtmlToPlainTextWithEntities.createRedirectHashMap(mainDirectory);
		S1HtmlToPlainTextWithEntities s1 = new S1HtmlToPlainTextWithEntities();
		s1.readFile(mainDirectory);
		;
	}

	public void readFile(File file) {
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				readFile(files[i]);
			} else {
				String oldFileName = files[i].getName();
				File outputFile = new File(SAVEDIR + oldFileName);
				processFile(files[i], outputFile);
			}
		}
	}

	public void processFile(File input, File outputFile) {
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
		String output = convert(c);
		output = output.replaceAll("DEFAULTSORT", "");
		if (output.length() > 10) {
			int length = -1;
			if (writtenFiles.containsKey(outputFile.getName())) {
				length = writtenFiles.get(outputFile.getName());
			}
			if (length == -1 || length < output.length()) {
				PrintWriter writer;
				try {
					writer = new PrintWriter(new FileWriter(outputFile, false));
					writer.write("<?xml version='1.0' encoding='utf-8'?><content>"
							+ output + "</content>");
					writer.flush();
					writer.close();
					writtenFiles.put(outputFile.getName(), output.length());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class Thread implements Runnable {

		private List<File> toprocess;

		public Thread(List<File> toprocess) {
			super();
			this.toprocess = toprocess;
		}

		@Override
		public void run() {
			for (int i = 0; i < toprocess.size(); i++) {
				doAction(toprocess.get(i));
			}
		}

		private void doAction(File f) {
			if (!f.isDirectory()) {
				String oldFileName = f.getName();
				File outputFile = new File(SAVEDIR + oldFileName);
				processFile(f, outputFile);
			} else {
				processDir(f);
			}
		}

		private void processDir(File f) {
			File[] dir = f.listFiles();
			for (int i = 0; i < dir.length; i++) {
				if (!dir[i].isDirectory()) {
					doAction(dir[i]);
				} else {
					processDir(dir[i]);
				}
			}
		}
	}
}