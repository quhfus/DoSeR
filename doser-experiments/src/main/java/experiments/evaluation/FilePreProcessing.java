package experiments.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class FilePreProcessing {

	private File qrels;

	private File result;

	private BufferedReader readerQrels;

	private BufferedReader readerResult;

	private Query query;

	private WorkingChain chain;

	private String lastQrelsLine;

	private String lastResultLine;

	private FileInputStream fis;

	private UnicodeBOMInputStream ubis;

	private InputStreamReader isr;

	public FilePreProcessing(String qrels, String result, WorkingChain chain)
			throws NullPointerException, IOException {
		System.out.println(qrels);
		System.out.println(result);
		this.qrels = new File(qrels);
		this.result = new File(result);
		this.chain = chain;
		if (!this.qrels.exists() || !this.result.exists()) {
			throw new FileNotFoundException();
		}
		fis = new FileInputStream(qrels);
		ubis = new UnicodeBOMInputStream(fis);
		isr = new InputStreamReader(ubis);
		readerQrels = new BufferedReader(isr);

		fis = new FileInputStream(result);
		ubis = new UnicodeBOMInputStream(fis);
		isr = new InputStreamReader(ubis);
		readerResult = new BufferedReader(isr);
	}

	public void process() throws IOException, LineParsingException {
		while (true) {
			boolean qrels = processQrels();
			boolean result = processResult();
			if (!qrels || !result) {
				if (query != null) {
					chain.work(query);
				}
				break;
			} else {
				chain.work(query);
			}
		}
	}

	private boolean processQrels() throws IOException, LineParsingException {
		int qryNr = 0;
		query = new Query();
		// Read In Last line
		if (lastQrelsLine != null) {
			String[] optStr = checkQrelsLine(lastQrelsLine);
			CorrectEntry ce = new CorrectEntry(optStr[2],
					Integer.parseInt(optStr[3]));
			this.query.addCorrectEntry(ce);
			qryNr = Integer.valueOf(optStr[0]);
			this.query.setQryNr(qryNr);
		} else {
			String currentLine = null;
			// First Lineread to save Queryid!
			currentLine = readerQrels.readLine();
			String[] optStr = checkQrelsLine(currentLine);
			CorrectEntry ce = new CorrectEntry(optStr[2],
					Integer.parseInt(optStr[3]));
			this.query.addCorrectEntry(ce);
			qryNr = Integer.parseInt(optStr[0]);
			this.query.setQryNr(qryNr);
		}

		String currentLine = null;
		while ((currentLine = readerQrels.readLine()) != null) {
			String[] optStr = checkQrelsLine(currentLine);
			if (Integer.valueOf(optStr[0]) != qryNr) {
				break;
			}
			CorrectEntry ce = new CorrectEntry(optStr[2],
					Integer.parseInt(optStr[3]));
			this.query.addCorrectEntry(ce);
		}
		if (currentLine == null) {
			return false;
		} else {
			lastQrelsLine = currentLine;
		}
		return true;
	}

	private boolean processResult() throws IOException, LineParsingException {
		int qryNr = 0;
		// End of File Check
		if (lastResultLine != null) {
			String[] optStr = checkResultLine(lastResultLine);
			if (query.getQryNr() < Integer.valueOf(optStr[0])) {
				return true;
			}
			ResultEntry re = new ResultEntry(optStr[2],
					Double.parseDouble(optStr[4]));
			this.query.addResultEntry(re);
			qryNr = Integer.parseInt(optStr[0]);
		} else {
			String currentLine = null;
			currentLine = readerResult.readLine();
			String[] optStr = checkResultLine(currentLine);
			if (query.getQryNr() < Integer.valueOf(optStr[0])) {
				System.out.println(currentLine);
				return true;
			}
			ResultEntry re = new ResultEntry(optStr[2],
					Double.parseDouble(optStr[4]));
			this.query.addResultEntry(re);
			qryNr = Integer.parseInt(optStr[0]);
		}

		String currentLine = null;
		while ((currentLine = readerResult.readLine()) != null) {
			String[] optStr = checkResultLine(currentLine);
			if (Integer.valueOf(optStr[0]) != qryNr) {
				break;
			}
			ResultEntry re = new ResultEntry(optStr[2],
					Double.parseDouble(optStr[4]));
			this.query.addResultEntry(re);
		}

		if (currentLine == null) {
			return false;
		} else {
			lastResultLine = currentLine;
		}
		return true;
	}

	// 2 verschiedene Methoden, falls zusätzliche Kriterien auszuwerten sind.
	// Bisher sind die Methoden allerdings noch identisch.
	private String[] checkQrelsLine(String line) {
		String[] splitter = line.split(" ");
		List<String> result = new LinkedList<String>();
		for (int i = 0; i < splitter.length; i++) {
			if (splitter[i] != null && !splitter[i].equalsIgnoreCase("")
					&& !splitter[i].equalsIgnoreCase(" ")) {
				result.add(splitter[i]);
			}
		}
		if (result.size() == 0) {
			return null;
		}
		String[] res = new String[result.size()];
		return result.toArray(res);
	}

	private String[] checkResultLine(String line) {

		String[] splitter = line.split(" ");
		List<String> result = new LinkedList<String>();
		for (int i = 0; i < splitter.length; i++) {
			if (splitter[i] != null && !splitter[i].equalsIgnoreCase("")
					&& !splitter[i].equalsIgnoreCase(" ")) {
				result.add(splitter[i]);
			}
		}
		if (result.size() == 0) {
			return null;
		}
		String[] res = new String[result.size()];
		return result.toArray(res);
	}

	public void close() {
		try {
			if (fis != null) {
				fis.close();
			}
			if (ubis != null) {
				ubis.close();
			}
			if (isr != null) {
				isr.close();
			}
			if (readerQrels != null) {
				readerQrels.close();
			}
			if (readerResult != null) {
				readerResult.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
