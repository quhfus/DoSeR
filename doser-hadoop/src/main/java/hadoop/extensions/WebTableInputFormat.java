package hadoop.extensions;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class WebTableInputFormat extends FileInputFormat<Text, BytesWritable> {

	private static boolean isLenient = false;

	@Override
	protected boolean isSplitable(JobContext context, Path filename) {
		return false;
	}

	@Override
	public RecordReader<Text, BytesWritable> createRecordReader(
			InputSplit arg0, TaskAttemptContext arg1) throws IOException,
			InterruptedException {
		WebTableRecordReader reader = new WebTableRecordReader();
		reader.initialize(arg0, arg1);
		return reader;
	}

	public static void setLenient(boolean lenient) {
		isLenient = lenient;
	}

	public static boolean getLenient() {
		return isLenient;
	}

}
