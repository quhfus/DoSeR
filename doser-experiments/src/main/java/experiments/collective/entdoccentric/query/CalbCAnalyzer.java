package experiments.collective.entdoccentric.query;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;


public final class CalbCAnalyzer extends StopwordAnalyzerBase {

	/** Default maximum allowed token length */
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	/**
	 * An unmodifiable set containing some common English words that are usually
	 * not useful for searching.
	 */
	public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

	/**
	 * Builds an analyzer with the given stop words.
	 * 
	 * @param matchVersion
	 *            Lucene version to match See
	 *            {@link <a href="#version">above</a>}
	 * @param stopWords
	 *            stop words
	 */
	public CalbCAnalyzer(Version matchVersion, CharArraySet stopWords) {
		super(matchVersion, stopWords);
	}

	/**
	 * Builds an analyzer with the default stop words ({@link #STOP_WORDS_SET}).
	 * 
	 * @param matchVersion
	 *            Lucene version to match See
	 *            {@link <a href="#version">above</a>}
	 */
	public CalbCAnalyzer(Version matchVersion) {
		this(matchVersion, STOP_WORDS_SET);
	}

	/**
	 * Builds an analyzer with the stop words from the given reader.
	 * 
	 * @see WordlistLoader#getWordSet(Reader, Version)
	 * @param matchVersion
	 *            Lucene version to match See
	 *            {@link <a href="#version">above</a>}
	 * @param stopwords
	 *            Reader to read stop words from
	 */
	public CalbCAnalyzer(Version matchVersion, Reader stopwords)
			throws IOException {
		this(matchVersion, loadStopwordSet(stopwords, matchVersion));
	}

	/**
	 * Set maximum allowed token length. If a token is seen that exceeds this
	 * length then it is discarded. This setting only takes effect the next time
	 * tokenStream or tokenStream is called.
	 */
	public void setMaxTokenLength(int length) {
		maxTokenLength = length;
	}

	/**
	 * @see #setMaxTokenLength
	 */
	public int getMaxTokenLength() {
		return maxTokenLength;
	}
	

	@Override
	protected TokenStreamComponents createComponents(final String fieldName,
			final Reader reader) {
		final CalbCTokenizer src = new CalbCTokenizer(matchVersion, reader);
		return new TokenStreamComponents(src) {
			@Override
			protected void setReader(final Reader reader) throws IOException {
				super.setReader(reader);
			}
		};
	}
}