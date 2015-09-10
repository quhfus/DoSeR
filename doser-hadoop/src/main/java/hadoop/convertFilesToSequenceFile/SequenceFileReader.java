package hadoop.convertFilesToSequenceFile;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.ValueBytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ReflectionUtils;

public class SequenceFileReader {

	public static void main(String[] args) throws IOException {
		String uri = args[0];
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(URI.create(uri), conf);
		Path path = new Path(uri);
		
		SequenceFile.Reader reader = null;
		try {
			reader = new SequenceFile.Reader(fs, path, conf);
			Writable key = (Writable) ReflectionUtils.newInstance(reader.getKeyClass(),conf);
			BytesWritable value = (BytesWritable) ReflectionUtils.newInstance(reader.getValueClass(),conf);
//			long position = reader.getPosition();
			
//			stream.write(value.getBytes());
			while(reader.next(key, value)) {
				
				ByteArrayInputStream inputStream = new ByteArrayInputStream(value.getBytes());
				
				System.out.println("----------------------------------------------");
				int n = value.getLength();
				byte[] bytes = new byte[n];
				inputStream.read(bytes, 0, n);
				System.out.println(new String(bytes));
//				System.out.println(n);
//				System.out.println();

				FileOutputStream fos = new FileOutputStream("/home/quh/Arbeitsfläche/Test/"+key.toString().replaceAll("/home/quh/Arbeitsfläche/HadoopTestFiles/", ""));
				fos.write(bytes);
				fos.close();
			}
		} finally {
			IOUtils.closeStream(reader);
		}
	}
}
