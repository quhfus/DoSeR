package hadoop.webtables.wordcount;

import hadoop.extensions.JobBuilder;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class WordcountJob extends Configured implements Tool  {
	
	@Override
	public int run(String[] args) throws Exception {
		Job job = JobBuilder.parseInputAndOutput(this, getConf(), args);
		if (job == null) {
			return -1;
		}

		job.setNumReduceTasks(20);
		job.setJobName("WebTables Wordcount Job");
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setMapperClass(WordcountMapper.class);
		job.setReducerClass(WordcountReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setJarByClass(WordcountJob.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new WordcountJob(),
				args);
		System.exit(exitCode);
	}
}
