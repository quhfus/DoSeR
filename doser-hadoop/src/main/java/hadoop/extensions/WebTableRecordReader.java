package hadoop.extensions;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class WebTableRecordReader extends
		RecordReader<Text, BytesWritable> {

	private boolean isFinished = false;
	private FSDataInputStream fin;
	private GzipCompressorInputStream gzIn;
	private TarArchiveInputStream tarIn;
	private BytesWritable currentValue;
	private Text currentKey;
	private Path filePath;

	@Override
	public void close() throws IOException {
		try {
			tarIn.close();
			gzIn.close();
			fin.close();
		} catch (Exception ignore) {
		}
	}

	@Override
	public Text getCurrentKey() throws IOException,
			InterruptedException {
		return currentKey;
	}

	@Override
	public BytesWritable getCurrentValue() throws IOException,
			InterruptedException {
		return currentValue;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return isFinished ? 1 : 0;
	}

	@Override
	public void initialize(InputSplit arg0, TaskAttemptContext arg1)
			throws IOException, InterruptedException {
		FileSplit split = (FileSplit) arg0;
		Configuration conf = arg1.getConfiguration();
		filePath = split.getPath();
		FileSystem fs = filePath.getFileSystem(conf);

		fin = fs.open(filePath);
		gzIn = new GzipCompressorInputStream(fin);
		tarIn = new TarArchiveInputStream(gzIn);

	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		TarArchiveEntry entry = null;
		boolean ret = false;
		while (true) {
			entry = tarIn.getNextTarEntry();
			if (entry == null) {
				isFinished = true;
				ret = false;
				break;
			}
			String key = entry.getName();
			if (key.endsWith(".csv")) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] temp = new byte[8192];
				while (true) {
					int bytesRead = 0;
					try {
						bytesRead = tarIn.read(temp, 0, 8192);
					} catch (EOFException e) {
						if (WebTableInputFormat.getLenient() == false) {
							throw e;
						}
						return false;
					}
					if (bytesRead > 0) {
						bos.write(temp, 0, bytesRead);
					} else {
						break;
					}
				}
				bos.flush();
				ret = true;
				currentValue = new BytesWritable(bos.toByteArray());
				currentKey = new Text(filePath.getName()+"/"+entry.getName());
				break;
			}
		}
		return ret;
	}

}
