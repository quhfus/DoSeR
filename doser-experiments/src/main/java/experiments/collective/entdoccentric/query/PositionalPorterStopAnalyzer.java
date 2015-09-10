package experiments.collective.entdoccentric.query;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

public class PositionalPorterStopAnalyzer extends Analyzer {

	private Version matchVersion;
	
	/**
	 * An unmodifiable set containing some common English words that are usually
	 * not useful for searching.
	 */
	public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

	/**
	 * Builds an analyzer with the default stop words ({@link #STOP_WORDS_SET}).
	 * 
	 * @param matchVersion
	 *            Lucene version to match See
	 *            {@link <a href="#version">above</a>}
	 */
	public PositionalPorterStopAnalyzer(Version matchVersion) {
		super();
		this.matchVersion = matchVersion;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			Reader reader) {
		final CalbCTokenizer src = new CalbCTokenizer(matchVersion, reader);
		TokenStream stream = new PorterStemFilter(new StopFilter(matchVersion,
				src, STOP_WORDS_SET));
		return new TokenStreamComponents(src, stream) {
			@Override
			protected void setReader(final Reader reader) throws IOException {
				super.setReader(reader);
			}
		};
	}

}
