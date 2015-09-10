package doser.lucene.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.Version;

public final class DoserStandardAnalyzer extends StopwordAnalyzerBase {

	/** Default maximum allowed token length */
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	/**
	 * An unmodifiable set containing some common English words that are usually
	 * not useful for searching.
	 */
	public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	/**
	 * Builds an analyzer with the default stop words ({@link #STOP_WORDS_SET}).
	 * 
	 */
	public DoserStandardAnalyzer() {
		this(STOP_WORDS_SET);
	}

	/**
	 * Builds an analyzer with the given stop words.
	 * 
	 * @param stopWords
	 *            stop words
	 */
	public DoserStandardAnalyzer(CharArraySet stopWords) {
		super(stopWords);
	}

	/**
	 * Builds an analyzer with the stop words from the given reader.
	 * 
	 * @see WordlistLoader#getWordSet(Reader, Version)
	 * @param stopwords
	 *            Reader to read stop words from
	 */
	public DoserStandardAnalyzer(Reader stopwords)
			throws IOException {
		this(loadStopwordSet(stopwords));
	}

	@Override
	protected TokenStreamComponents createComponents(final String fieldName,
			final Reader reader) {
		final DoserStandardTokenizer src = new DoserStandardTokenizer(reader);
		TokenStream tok = new StandardFilter(src);
		tok = new LowerCaseFilter(tok);
		return new TokenStreamComponents(src, tok) {
			@Override
			protected void setReader(final Reader reader) throws IOException {
				super.setReader(reader);
			}
		};
	}

	/**
	 * @see #setMaxTokenLength
	 */
	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	/**
	 * Set maximum allowed token length. If a token is seen that exceeds this
	 * length then it is discarded. This setting only takes effect the next time
	 * tokenStream or tokenStream is called.
	 */
	public void setMaxTokenLength(int length) {
		maxTokenLength = length;
	}
}