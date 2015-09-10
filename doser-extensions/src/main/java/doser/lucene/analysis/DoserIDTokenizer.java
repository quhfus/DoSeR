package doser.lucene.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;

public final class DoserIDTokenizer extends CharTokenizer {

	/**
	 * Construct a new WhitespaceTokenizer using a given
	 * {@link org.apache.lucene.util.AttributeSource.AttributeFactory}.
	 * 
	 * @param matchVersion
	 *            Lucene version to match See
	 *            {@link <a href="#version">above</a>}
	 * @param factory
	 *            the attribute factory to use for this {@link Tokenizer}
	 * @param in
	 *            the input to split up into tokens
	 */
	public DoserIDTokenizer(AttributeFactory factory, Reader in) {
		super(factory, in);
	}

	/**
	 * Construct a new WhitespaceTokenizer. * @param matchVersion Lucene version
	 * to match See {@link <a href="#version">above</a>}
	 * 
	 * @param in
	 *            the input to split up into tokens
	 */
	public DoserIDTokenizer(Reader in) {
		super(in);
	}

	/**
	 * Collects only characters which do not satisfy
	 * {@link Character#isWhitespace(int)}.
	 */
	@Override
	protected boolean isTokenChar(int c) {
		boolean check = true;
		if (Character.isWhitespace(c)) {
			check = false;
		}
		return check;
	}
}