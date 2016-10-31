package doser.entitydisambiguation.algorithms.contextcomparison;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class TestStandardAnalyzer {

	public static void main(String[] args) {
		String text = "London is the capital and most plays populous city [135] of England and the United Kingdom.";
		Analyzer ana = new StandardAnalyzer();
		List<String> result = new ArrayList<String>();
		try {
			TokenStream stream = ana.tokenStream("", new StringReader(text));

			stream.reset();
			while (stream.incrementToken()) {
				result.add(stream.getAttribute(CharTermAttribute.class).toString());
			}

			for (String s : result) {
				System.out.println(s);
			}
		} catch (IOException e) {
			// not thrown b/c we're using a string reader...
		}

	}

}
