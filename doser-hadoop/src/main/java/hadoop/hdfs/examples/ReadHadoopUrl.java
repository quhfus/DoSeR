package hadoop.hdfs.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

public class ReadHadoopUrl {
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Please insert filelocation");
		}
		
		Configuration config = new Configuration();

		FSDataInputStream in = null;
		BufferedReader breader = null;
		try {
			FileSystem fs = FileSystem.get(URI.create(args[0]), config);
			in = fs.open(new Path(args[0]));
			breader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while((line = breader.readLine()) != null) {
				System.out.println(line);
			}
			in.seek(0);
			
			breader = new BufferedReader(new InputStreamReader(in));
			while((line = breader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeStream(in);
			IOUtils.closeStream(breader);
		}
	}

}
