package hadoop.convertFilesToSequenceFile;

import hadoop.extensions.WebTableInputFormat;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

public class ConvertWebTablesToSequenceFile {

	private SequenceFile.Writer writer;

	static int counter = 0;
	static int together = 0;
	static long size = 0;
	
	public ConvertWebTablesToSequenceFile(String s, String directoryStructure)
			throws IOException {
		super();
		String uri = s;
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(URI.create(uri), conf);
		Path path = new Path(uri);
		try {
			this.writer = SequenceFile.createWriter(fs, conf, path, Text.class,
					BytesWritable.class);
			processFiles(new File(directoryStructure));
		} finally {
			IOUtils.closeStream(writer);
		}
	}

	private void processFiles(File node) throws IOException {
		if (node.isDirectory()) {
			System.out.println("Directory: "+node.getAbsolutePath());
			String[] subNote = node.list();
			for (String filename : subNote) {
				processFiles(new File(node, filename));
			}
		} else {
			writeOut(node, node.getAbsoluteFile().getParentFile()
					.getAbsolutePath());
		}
	}

	private void writeOut(File file, String directoryName) throws IOException {
		GzipCompressorInputStream gzIn = null;
		TarArchiveInputStream tarIn = null;
		try {
			gzIn = new GzipCompressorInputStream(new FileInputStream(file));
			tarIn = new TarArchiveInputStream(gzIn);

			TarArchiveEntry entry = null;
			while (true) {
				entry = tarIn.getNextTarEntry();
				if (entry == null) {
					break;
				}
				String key = entry.getName();
				if (key.endsWith(".csv")) {
					together++;
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					byte[] temp = new byte[8192];
					boolean isTooLarge = false;
					while (true) {
						int bytesRead = 0;
						try {
							bytesRead = tarIn.read(temp, 0, 8192);
						} catch (EOFException e) {
							if (WebTableInputFormat.getLenient() == false) {
								throw e;
							}
							break;
						}
						if (bytesRead > 0) {
							bos.write(temp, 0, bytesRead);
						} else {
							break;
						}
						if (bos.size() > (100 * 100 * 8192)) {
							isTooLarge = true;
							break;
						}
					}
					if (!isTooLarge) {
						bos.flush();
						BytesWritable currentValue = new BytesWritable(
								bos.toByteArray());
						Text currentKey = new Text(directoryName + "/"
								+ entry.getName());
						this.writer.append(currentKey, currentValue);
						counter++;
					}
				}
			}
		} finally {
			try {
				tarIn.close();
				gzIn.close();
			} catch (Exception ignore) {
			}
		}
	}

	public static void main(String[] args) {
		try {
			new ConvertWebTablesToSequenceFile(args[1], args[0]);
			System.out.println(counter+ "     "+together );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
