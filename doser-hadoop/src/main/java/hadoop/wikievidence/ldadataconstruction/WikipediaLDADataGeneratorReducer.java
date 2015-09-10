package hadoop.wikievidence.ldadataconstruction;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class WikipediaLDADataGeneratorReducer extends
		Reducer<Text, Text, Text, Text> {

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		Iterator<Text> it = values.iterator();
		StringBuilder b = new StringBuilder();
		while(it.hasNext()) {
//			b.append("---");
			b.append(" ");
			b.append(it.next());
		}
//		b.append("---");
		b.append(" ");
		context.write(key, new Text(b.toString()));		
	}
}
