package hadoop.wikievidence.ldadataconstruction;

import hadoop.extensions.JobBuilder;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class WikipediaLDADataGeneratorDriver extends Configured implements Tool {
	
	
	@Override
	public int run(String[] args) throws Exception {
		Job job = JobBuilder.parseInputAndOutput(this, getConf(), args);
		if (job == null) {
			return -1;
		}

//		job.setNumReduceTasks(20);
		job.setJobName("WikipediaLDADataGeneratorDriver");
		job.setMapperClass(WikipediaLDADataGeneratorMapper.class);
		job.setReducerClass(WikipediaLDADataGeneratorReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setJarByClass(WikipediaLDADataGeneratorDriver.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new WikipediaLDADataGeneratorDriver(),
				args);
		System.exit(exitCode);
	}
}
