package doser.lucene.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class DoserIDFilter extends TokenFilter {

	public DoserIDFilter(TokenStream in) {
		super(in);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (!input.incrementToken()) {
			return false;
		}
		return true;
	}

}
