package lda.wikievidence.modelcreation;

import hbase.operations.HBaseOperations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

public class LDAClientExtractProbabilities extends LDAClient {

	private String lastIterationPath;
	private String topic;
	private String tableName;

	public LDAClientExtractProbabilities(int threadnr, byte[] dataset,
			String topic, String tableName) {
		super(threadnr);
		this.lastIterationPath = modeloutputPath + "00050";
		this.topic = topic;
		this.tableName = tableName;
	}

	@Override
	public void run() {
//		// Create LDA Configuration File
//		byte[] config = ConfigCreation.createStandardPLDAConfig(datafilePath,
//				modeloutputPath);
//		writeOutput(config, configPath);
//
//		// Create Datafile
////		writeOutput(data, datafilePath);
//		// ToDo Generate Data!
//
//		// Execute LDA
//		try {
//			Process proc = Runtime.getRuntime().exec(
//					"java -jar "
//							+ LDAProperties.getInstance().getLDAClientApp()
//							+ " " + configPath);
//			proc.waitFor();
//			// Then retrieve the process output
//			InputStream in = proc.getInputStream();
//			InputStream err = proc.getErrorStream();
//
//			byte b[] = new byte[in.available()];
//			in.read(b, 0, b.length);
//			// System.out.println(new String(b));
//
//			byte c[] = new byte[err.available()];
//			err.read(c, 0, c.length);
//			// System.out.println(new String(c));
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}
//
//		storeProbabilityDistribution();
//
//		// Delete Thread Directory
//		deleteDir(threadDir);
	}

	private void storeProbabilityDistribution() {
		BufferedReader buffered = null;
		try {
			InputStream fileStream = new FileInputStream(lastIterationPath
					+ "topic-term-distributions.csv.gz");
			InputStream gzipStream = new GZIPInputStream(fileStream);
			Reader decoder = new InputStreamReader(gzipStream,
					Charset.defaultCharset());
			buffered = new BufferedReader(decoder);
			int neccLine = extractTopicLine(lastIterationPath, topic);
			int it = 0;
			while (it < neccLine) {
				buffered.readLine();
				it++;
			}
			String line = buffered.readLine();
			String[] split = line.split(",");
			double[] vals = new double[split.length];
			for (int i = 0; i < split.length; i++) {
				vals[i] = Double.parseDouble(split[i]);
			}
			storeProbabilities(vals);
		} catch (FileNotFoundException e) {
			Logger.getRootLogger().error("Error:", e);
		} catch (IOException e) {
			Logger.getRootLogger().error("Error:", e);
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

	private void storeProbabilities(double[] probline) {
		File file = new File(lastIterationPath + "term-index.txt");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			HBaseOperations.getInstance().deleteIDRow(tableName, topic);
			Put put = new Put(topic.getBytes());
			int i = 0;
			while ((line = reader.readLine()) != null) {
				put.add(Bytes.toBytes("data"), Bytes.toBytes(line),
						Bytes.toBytes(probline[i]));
				++i;
			}
			HBaseOperations.getInstance().addCompleteEntry(tableName, put);
		} catch (FileNotFoundException e) {
			Logger.getRootLogger().error("Error:", e);
		} catch (IOException e) {
			Logger.getRootLogger().error("Error:", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Logger.getRootLogger().error("Error:", e);
				}
			}
		}
	}
}
