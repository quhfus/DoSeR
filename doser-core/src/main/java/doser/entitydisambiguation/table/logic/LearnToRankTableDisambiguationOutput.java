package doser.entitydisambiguation.table.logic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;

import doser.entitydisambiguation.table.columndisambiguation.LearntoRankOutputObject;

/**
 * This class writes learn to rank feature values into an output file
 */
public class LearnToRankTableDisambiguationOutput {

	public static final String OUTPUTFILE = "/mnt/ssd1/disambiguation/misc/ltroutput";

	private Writer out;

	public LearnToRankTableDisambiguationOutput() {
		final File newfile = new File(OUTPUTFILE);
		try {
			this.out = new FileWriter(newfile, true);
		} catch (final IOException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
	}

	private void write(final String text) {
		try {
			this.out.write(text);
			this.out.write(System.getProperty("line.separator"));
			this.out.flush();
		} catch (final IOException e) {
			Logger.getRootLogger().error(e.getStackTrace());
		}
	}

	public void writeQueryResult(final LearntoRankOutputObject object) {
		final String[] field = new String[5];
		String relevance = "";
		if (object.isRelevant()) {
			relevance = "1";
		} else {
			relevance = "-1";
		}
		field[0] = relevance;
		
		final StringBuffer buffer = new StringBuffer(relevance + " qid:" + object.getqId() + " ");
		final double[] featureValues = object.getFeatureValues();
		for (int i = 0; i < featureValues.length; i++) {
			buffer.append((i + 1) + ":" + featureValues[i]);
			field[i + 1] = String.valueOf(featureValues[i]);
			if (i < (featureValues.length - 1)) {
				buffer.append(' ');
			}
		}
		this.write(buffer.toString());
	}

}
