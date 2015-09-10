package lda.wikievidence.modelcreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.log4j.Logger;

public abstract class LDAClient implements Runnable {

	protected String configPath;
	protected String datafilePath;
	protected String modeloutputPath;
	protected File threadDir;
	private int threadNr;

	public LDAClient(int threadnr) {
		super();
		// Create Thread Directory
		File threadDir = new File("Thread"
				+ String.valueOf(threadnr) + "/");
		
		threadDir.mkdir();
		this.threadDir = threadDir;

		this.datafilePath = threadDir.getAbsolutePath() + "/data.dat";
		this.configPath = threadDir.getAbsolutePath() + "/config.dat";
		this.modeloutputPath = threadDir.getAbsolutePath() + "/model/";
		this.threadNr = threadnr;
	}

	protected void deleteDir(File path) {
		for (File file : path.listFiles()) {
			if (file.isDirectory())
				deleteDir(file);
			file.delete();
		}
		path.delete();
	}

	protected void writeOutput(byte[] bytes, String configPath) {
		try {
			Files.write(Paths.get(configPath), bytes, StandardOpenOption.CREATE);
		} catch (IOException e) {
			Logger.getRootLogger().error("Error:", e);
		}
	}

	protected int extractTopicLine(String lastIterationPath, String topic) {
		File file = new File(lastIterationPath + "topic-index.txt");
		BufferedReader reader = null;
		int lineNr = -1;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			int nr = 0;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(topic)) {
					lineNr = nr;
					break;
				}
				++nr;
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

		return lineNr;
	}
}
