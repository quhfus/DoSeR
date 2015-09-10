package hadoop.webtables.wordcount;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Counter;

public class WordcountMapper extends
		Mapper<Text, BytesWritable, Text, IntWritable> {

	static enum CountersEnum {
		INPUT_WORDS
	}

	private final static IntWritable one = new IntWritable(1);
	private Text word = new Text();

	@Override
	public void map(Text key, BytesWritable value, Context context)
			throws IOException, InterruptedException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				value.getBytes());
		int n = value.getLength();
		byte[] bytes = new byte[n];
		inputStream.read(bytes, 0, n);
		CSVParser parser = CSVParser
				.parse(new String(bytes), CSVFormat.DEFAULT);
		try {
			for (CSVRecord csvRecord : parser) {
				for (String string : csvRecord) {
					word.set(string);
					context.write(word, one);
					Counter counter = context.getCounter(
							CountersEnum.class.getName(),
							CountersEnum.INPUT_WORDS.toString());
					counter.increment(1);
				}
			}
		} catch (Exception e) {
		}
	}
}
