package hadoop.webtables.wordcount;

import hadoop.extensions.JobBuilder;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class WordcountJob extends Configured implements Tool  {
	
	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();
	    GenericOptionsParser optionParser = new GenericOptionsParser(conf, args);
	    String[] remainingArgs = optionParser.getRemainingArgs();
	    if (!(remainingArgs.length != 2 || remainingArgs.length != 4)) {
	      System.err.println("Usage: wordcount <in> <out> [-skip skipPatternFile]");
	      System.exit(2);
	    }
	    Job job = J(conf, "word count");
	    job.setJarByClass(WordcountJob.class);
	    job.setMapperClass(WordcountMapper.class);
	    job.setCombinerClass(WordcountReducer.class);
	    job.setReducerClass(WordcountReducer.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);

	    List<String> otherArgs = new ArrayList<String>();
	    for (int i=0; i < remainingArgs.length; ++i) {
	      if ("-skip".equals(remainingArgs[i])) {
	        job.addCacheFile(new Path(remainingArgs[++i]).toUri());
	        job.getConfiguration().setBoolean("wordcount.skip.patterns", true);
	      } else {
	        otherArgs.add(remainingArgs[i]);
	      }
	    }
	    FileInputFormat.addInputPath(job, new Path(otherArgs.get(0)));
	    FileOutputFormat.setOutputPath(job, new Path(otherArgs.get(1)));

	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new WordcountJob(),
				args);
		System.exit(exitCode);
	}
}
