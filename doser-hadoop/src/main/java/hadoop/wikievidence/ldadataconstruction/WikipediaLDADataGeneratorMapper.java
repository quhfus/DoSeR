package hadoop.wikievidence.ldadataconstruction;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.google.gson.Gson;

public class WikipediaLDADataGeneratorMapper extends
		Mapper<LongWritable, Text, Text, Text> {

	private Gson gson;
	
	public WikipediaLDADataGeneratorMapper() {
		super();
		this.gson = new Gson();
	}
	
	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		String line = value.toString();
		Output o = gson.fromJson(line, Output.class);
		
		// Eventual content string processing. We keep the format of 1000 words before and after the entity mention 
//		context.write(new Text(o.getUrl()), new Text(o.getMention()+"---"+o.getContent()));
		context.write(new Text(o.getUrl()), new Text(o.getContent()));
	}

	public class Output {
		private String entity;
		private String content;
		private String mention;

		public String getUrl() {
			return entity;
		}
		public void setUrl(String url) {
			this.entity = url;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public String getMention() {
			return mention;
		}
		public void setMention(String mention) {
			this.mention = mention;
		}
	}
	
}
