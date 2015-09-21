package lda.wikievidence.modelcreation;

import hbase.operations.HBaseOperations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class WikipediaLDAThreadExtractEvidenceTerms extends LDAClient {

	public static final int RANDOMDOCUMENTS = 1000;

	public static final String WIKIPEDIAPAGESDIR = "/mnt/storage/zwicklbauer/WikiParse/temp/plain_reduced/";

	private String lastIterationPath;
	private String evidenceFilePath;
	private int topTerms;
	private String[] evidence;
	private Map<Integer, Output> hashmap;
	private byte[] dataset;
	private String[] circleEnts;
	private Map<String, String> realFileNames;

	public WikipediaLDAThreadExtractEvidenceTerms(int threadnr, int topTerms,
			Map<Integer, Output> map, String[] circleEnts,
			Map<String, String> realFileNames) {
		super(threadnr);
		this.topTerms = topTerms;
		this.lastIterationPath = modeloutputPath + "00025/";
		this.evidenceFilePath = lastIterationPath + "summary.txt";
		this.hashmap = map;
		this.circleEnts = circleEnts;
		this.realFileNames = realFileNames;
	}

	@Override
	public void run() {
		// Create LDA Configuration File
		byte[] config = ConfigCreation.createEvidenceExtractionConfig(
				datafilePath, modeloutputPath, topTerms);
		writeOutput(config, configPath);

		this.dataset = createDataFile(circleEnts);

		// Create Datafile
		writeOutput(this.dataset, datafilePath);

		// Execute LDA
		try {
			Process proc = Runtime.getRuntime().exec(
					"java -jar tmt-assembly-0.4.0.jar " + configPath);

			proc.waitFor();
			// Then retrieve the process output
			InputStream in = proc.getInputStream();
			InputStream err = proc.getErrorStream();

			byte b[] = new byte[in.available()];
			in.read(b, 0, b.length);

			byte c[] = new byte[err.available()];
			err.read(c, 0, c.length);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		extractEvidence();
		writeEvidence(this.evidence, this.circleEnts[0]);
		// Delete Thread Directory
		deleteDir(threadDir);
	}

	private void extractEvidence() {
		BufferedReader buffered = null;
		try {
			buffered = new BufferedReader(new FileReader(new File(
					evidenceFilePath)));
			int neccLine = extractTopicLine(lastIterationPath, circleEnts[0]);
			int it = 0;
			String line = null;
			while (it < neccLine) {
				line = buffered.readLine();
				if (line.equalsIgnoreCase("")) {
					++it;
				}
			}
			StringBuffer buff = new StringBuffer();
			while ((line = buffered.readLine()) != null
					&& !line.equalsIgnoreCase("")) {
				buff.append(line + ";");
			}
			String ev = buff.toString();
			String[] split = ev.split(";");
			String[] vals = new String[split.length - 1];
			for (int i = 1; i < split.length; i++) {
				vals[i - 1] = split[i];
			}
			this.evidence = vals;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buffered != null) {
				try {
					buffered.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void writeEvidence(String[] evidence, String entityName) {
		try {
			PrintWriter writer = new PrintWriter(new File(
					MineEvidences.EVIDENCEDIR + "/" + entityName));
			for (int i = 0; i < evidence.length; i++) {
				writer.println(evidence[i]);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public byte[] createDataFile(String[] entities) {
		StringBuffer buffer = new StringBuffer();
		// Write main file content
		writeMainFileContent(entities[0], buffer);
		// Write main content
		int entryNumber = createBasicData(entities, "LDADC_Context", buffer);
		// Write randomly selected documents
		Set<Output> set = selectRandomDocuments(RANDOMDOCUMENTS);
		for (Output o : set) {
			Set<String> setRows = new HashSet<String>();
			try {
				HBaseOperations.getInstance().getRow("LDADC_Context",
						(o.getUrl() + ".html"), "data", setRows, 200);
				for (String s : setRows) {
					buffer.append(entryNumber++ + "," + o.getUrl() + ",\"" + s
							+ "\"");
					buffer.append(System.lineSeparator());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (entryNumber > 1000) {
				break;
			}
		}
		return buffer.toString().getBytes();
	}

	private void writeMainFileContent(String c, StringBuffer buffer) {
		// System.out.println(c +"    "+realFileNames.containsKey(c));
		if (realFileNames.containsKey(c)) {
			File f = new File(WIKIPEDIAPAGESDIR + realFileNames.get(c));
			String content = "";
			try {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line = null;
				while ((line = reader.readLine()) != null) {
					content += line;
				}
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				Handler handler = new Handler();
				InputSource inputSource = new InputSource(new StringReader(
						content));
				xmlReader.setContentHandler(handler);
				xmlReader.parse(inputSource);
				buffer.append(1 + "," + c + ",\"" + handler.getDocumentText()
						+ "\"");
				buffer.append(System.lineSeparator());
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			buffer.append(1 + "," + c + ",\"\"");
			buffer.append(System.lineSeparator());
		}
	}

	private Set<Output> selectRandomDocuments(int nrRandomDocs) {
		Set<Output> outputSet = new HashSet<Output>();
		int[] docs = createRandomNrs(nrRandomDocs, (hashmap.size() - 1));
		for (int i = 0; i < docs.length; i++) {
			outputSet.add(hashmap.get(docs[i]));
		}
		return outputSet;
	}

	private int[] createRandomNrs(int nrRandomDocs, int lines) {
		Random ran = new Random();
		int[] res = new int[nrRandomDocs];
		for (int i = 0; i < nrRandomDocs; i++) {
			res[i] = ran.nextInt(lines);
		}
		Arrays.sort(res);
		return res;
	}

	private int createBasicData(String[] entities, String hbaseTable,
			StringBuffer buffer) {
		int entryNumber = 2;
		for (int i = 0; i < entities.length; i++) {
			Set<String> set = new HashSet<String>();
			try {
				HBaseOperations.getInstance().getRow(hbaseTable,
						(entities[i] + ".html"), "data", set, 300);
			} catch (IOException e) {
				Logger.getRootLogger().error("Error:", e);
			}
			for (String l : set) {
				buffer.append(entryNumber++ + "," + entities[i] + ",\"" + l
						+ "\"");
				buffer.append(System.lineSeparator());
			}
		}
		return entryNumber;
	}

	public class Output {
		private String url;
		private String content;
		private String mention;

		public String getUrl() {
			return url.replaceAll(".html", "");
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getMention() {
			return mention;
		}

		public void setMention(String mention) {
			this.mention = mention;
		}

		@Override
		public int hashCode() {
			return content.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return (this.content == ((Output) obj).getContent());
		}
	}

	class Handler implements ContentHandler {

		private StringBuffer documentText;

		public Handler() {
			super();
			this.documentText = new StringBuffer();
		}

		@Override
		public void characters(char[] arg0, int arg1, int arg2)
				throws SAXException {
			this.documentText.append(new String(arg0, arg1, arg2));
		}

		@Override
		public void endDocument() throws SAXException {
		}

		@Override
		public void endElement(String arg0, String arg1, String arg2)
				throws SAXException {
		}

		@Override
		public void endPrefixMapping(String arg0) throws SAXException {
		}

		@Override
		public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
				throws SAXException {
		}

		@Override
		public void processingInstruction(String arg0, String arg1)
				throws SAXException {
		}

		@Override
		public void setDocumentLocator(Locator arg0) {
		}

		@Override
		public void skippedEntity(String arg0) throws SAXException {
		}

		@Override
		public void startDocument() throws SAXException {
		}

		@Override
		public void startElement(String arg0, String arg1, String arg2,
				Attributes arg3) throws SAXException {
		}

		@Override
		public void startPrefixMapping(String arg0, String arg1)
				throws SAXException {
		}

		public String getDocumentText() {
			return documentText.toString();
		}
	}

	public String[] getEvidence() {
		return evidence;
	}

	public String getTopic() {
		return circleEnts[0];
	}
}
