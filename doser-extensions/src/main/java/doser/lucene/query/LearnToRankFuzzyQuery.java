package doser.lucene.query;

import java.io.IOException;

import org.apache.lucene.index.SingleTermsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyTermsEnum;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.TopTermsRewrite;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.ToStringUtils;
import org.apache.lucene.util.automaton.LevenshteinAutomata;

/**
 * Implements the fuzzy search query. The similarity measurement is based on the
 * Damerau-Levenshtein (optimal string alignment) algorithm, though you can
 * explicitly choose classic Levenshtein by passing <code>false</code> to the
 * <code>transpositions</code> parameter.
 * 
 * <p>
 * This query uses {@link MultiTermQuery.TopTermsScoringBooleanQueryRewrite} as
 * default. So terms will be collected and scored according to their edit
 * distance. Only the top terms are used for building the {@link BooleanQuery}.
 * It is not recommended to change the rewrite mode for fuzzy queries.
 * 
 * <p>
 * At most, this query will match terms up to
 * {@value org.apache.lucene.util.automaton.LevenshteinAutomata#MAXIMUM_SUPPORTED_DISTANCE} edits. 
 * Higher distances (especially with transpositions enabled), are generally not useful and 
 * will match a significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the 
 * <a href="{@docRoot}
 * edits. Higher distances (especially with transpositions enabled), are
 * generally not useful and will match a significant amount of the term
 * dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot}
 * edits. Higher distances (especially with transpositions enabled), are
 * generally not useful and will match a significant amount of the term
 * dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot}
 * edits. Higher distances (especially with transpositions enabled), are
 * generally not useful and will match a significant amount of the term
 * dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot}
 * edits. Higher distances (especially with transpositions enabled), are
 * generally not useful and will match a significant amount of the term
 * dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot}
 * edits. Higher distances (especially with transpositions enabled), are
 * generally not useful and will match a significant amount of the term
 * dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot}
 * edits. Higher distances (especially with transpositions enabled), are
 * generally not useful and will match a significant amount of the term
 * dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot}
 * edits. Higher distances (especially with transpositions enabled), are
 * generally not useful and will match a significant amount of the term
 * dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot}
 * edits. Higher distances (especially with transpositions enabled), are
 * generally not useful and will match a significant amount of the term
 * dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot} edits. Higher distances (especially
 * with transpositions enabled), are generally not useful and will match a
 * significant amount of the term dictionary. If you really want this, consider
 * using an n-gram indexing technique (such as the SpellChecker in the <a
 * href="{@docRoot} edits. Higher distances (especially with transpositions
 * enabled), are generally not useful and will match a significant amount of the
 * term dictionary. If you really want this, consider using an n-gram indexing
 * technique (such as the SpellChecker in the <a href="{@docRoot} edits. Higher
 * distances (especially with transpositions enabled), are generally not useful
 * and will match a significant amount of the term dictionary. If you really
 * want this, consider using an n-gram indexing technique (such as the
 * SpellChecker in the <a href="{@docRoot}
 * /../suggest/overview-summary.html">suggest module</a>) instead.
 */
public class LearnToRankFuzzyQuery extends MultiTermQuery {

	public static final class LTRTopTermsScoringBooleanQueryRewrite extends
			TopTermsRewrite<BooleanQuery> {

		private final Similarity sim;

		/**
		 * Create a TopTermsScoringBooleanQueryRewrite for at most
		 * <code>size</code> terms.
		 * <p>
		 * NOTE: if {@link BooleanQuery#getMaxClauseCount} is smaller than
		 * <code>size</code>, then it will be used instead.
		 */
		public LTRTopTermsScoringBooleanQueryRewrite(final int size,
				final Similarity sim) {
			super(size);
			this.sim = sim;
		}

		@Override
		protected void addClause(final BooleanQuery topLevel, final Term term,
				final int docCount, final float boost, final TermContext states) {
			final LearnToRankTermQuery termq = new LearnToRankTermQuery(term,
					states, sim);
			termq.setBoost(boost);
			topLevel.add(termq, BooleanClause.Occur.SHOULD);
		}

		@Override
		protected int getMaxSize() {
			return BooleanQuery.getMaxClauseCount();
		}

		@Override
		protected BooleanQuery getTopLevelQuery() {
			return new BooleanQuery(true);
		}

		// @Override
		// protected void addClause(BooleanQuery topLevel, Term term, int
		// docCount, float boost, TermContext states) {
		// final TermQuery tq = new TermQuery(term, states);
		// tq.setBoost(boost);
		// topLevel.add(tq, BooleanClause.Occur.SHOULD);
		// }
	}

	public final static int DEFAULTMAXEDITS = LevenshteinAutomata.MAXIMUM_SUPPORTED_DISTANCE;
	/**
	 * @deprecated pass integer edit distances instead.
	 */
	@Deprecated
	public final static float DEFAULTMINSIM = LevenshteinAutomata.MAXIMUM_SUPPORTED_DISTANCE;
	public final static int DEFMAXEXPANSIONS = 50;

	public final static int DEFPREFIXLEN = 0;
	public final static boolean DEFTRANSPOS = true;

	/**
	 * Helper function to convert from deprecated "minimumSimilarity" fractions
	 * to raw edit distances.
	 * 
	 * @param minimumSimilarity
	 *            scaled similarity
	 * @param termLen
	 *            length (in unicode codepoints) of the term.
	 * @return equivalent number of maxEdits
	 * @deprecated pass integer edit distances instead.
	 */
	@Deprecated
	public static int floatToEdits(final float minimumSimilarity,
			final int termLen) {
		if (minimumSimilarity >= 1f) {
			return (int) Math.min(minimumSimilarity,
					LevenshteinAutomata.MAXIMUM_SUPPORTED_DISTANCE);
		} else if (minimumSimilarity == 0.0f) {
			return 0; // 0 means exact, not infinite # of edits!
		} else {
			return Math.min((int) ((1D - minimumSimilarity) * termLen),
					LevenshteinAutomata.MAXIMUM_SUPPORTED_DISTANCE);
		}
	}

	private final int maxEdits;
	private final int maxExpansions;
	private final int prefixLength;

	private final Term term;

	private final boolean transpositions;

	/**
	 * Create a new FuzzyQuery that will match terms with an edit distance of at
	 * most <code>maxEdits</code> to <code>term</code>. If a
	 * <code>prefixLength</code> &gt; 0 is specified, a common prefix of that
	 * length is also required.
	 * 
	 * @param term
	 *            the term to search for
	 * @param maxEdits
	 *            must be >= 0 and <=
	 *            {@link LevenshteinAutomata#MAXIMUM_SUPPORTED_DISTANCE}.
	 * @param prefixLength
	 *            length of common (non-fuzzy) prefix
	 * @param maxExpansions
	 *            the maximum number of terms to match. If this number is
	 *            greater than {@link BooleanQuery#getMaxClauseCount} when the
	 *            query is rewritten, then the maxClauseCount will be used
	 *            instead.
	 * @param transpositions
	 *            true if transpositions should be treated as a primitive edit
	 *            operation. If this is false, comparisons will implement the
	 *            classic Levenshtein algorithm.
	 */
	public LearnToRankFuzzyQuery(final Term term, final int maxEdits,
			final int prefixLength, final int maxExpansions,
			final boolean transpositions, final Similarity sim) {
		super(term.field());

		if ((maxEdits < 0)
				|| (maxEdits > LevenshteinAutomata.MAXIMUM_SUPPORTED_DISTANCE)) {
			throw new IllegalArgumentException(
					"maxEdits must be between 0 and "
							+ LevenshteinAutomata.MAXIMUM_SUPPORTED_DISTANCE);
		}
		if (prefixLength < 0) {
			throw new IllegalArgumentException(
					"prefixLength cannot be negative.");
		}
		if (maxExpansions < 0) {
			throw new IllegalArgumentException(
					"maxExpansions cannot be negative.");
		}

		this.term = term;
		this.maxEdits = maxEdits;
		this.prefixLength = prefixLength;
		this.transpositions = transpositions;
		this.maxExpansions = maxExpansions;
		setRewriteMethod(new LearnToRankFuzzyQuery.LTRTopTermsScoringBooleanQueryRewrite(
				maxExpansions, sim));
		// setRewriteMethod(new
		// LearnToRankFuzzyQuery.LTRTopTermsScoringBooleanQueryRewrite(
		// maxExpansions));
	}

	/**
	 * Calls {@link #FuzzyQuery(Term, int, int, int, boolean) FuzzyQuery(term,
	 * minimumSimilarity, prefixLength, DEFMAXEXPANSIONS, DEFTRANSPOS)}.
	 */
	public LearnToRankFuzzyQuery(final Term term, final int maxEdits,
			final int prefixLength, final Similarity sim) {
		this(term, maxEdits, prefixLength, DEFMAXEXPANSIONS, DEFTRANSPOS, sim);
	}

	/**
	 * Calls {@link #FuzzyQuery(Term, int, int) FuzzyQuery(term, maxEdits,
	 * DEFPREFIXLEN)}.
	 */
	public LearnToRankFuzzyQuery(final Term term, final int maxEdits,
			final Similarity sim) {
		this(term, maxEdits, DEFPREFIXLEN, sim);
	}

	/**
	 * Calls {@link #FuzzyQuery(Term, int) FuzzyQuery(term, DEFAULTMAXEDITS)}.
	 */
	public LearnToRankFuzzyQuery(final Term term, final Similarity sim) {
		this(term, DEFAULTMAXEDITS, sim);
	}

	@Override
	public boolean equals(final Object obj) { // NOPMD by quh on 28.02.14 10:55
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final LearnToRankFuzzyQuery other = (LearnToRankFuzzyQuery) obj;
		if (maxEdits != other.maxEdits) {
			return false;
		}
		if (prefixLength != other.prefixLength) {
			return false;
		}
		if (maxExpansions != other.maxExpansions) {
			return false;
		}
		if (transpositions != other.transpositions) {
			return false;
		}
		if (term == null) {
			if (other.term != null) {
				return false;
			}
		} else if (!term.equals(other.term)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the maximum number of edit distances allowed for this query to
	 *         match.
	 */
	public int getMaxEdits() {
		return maxEdits;
	}

	/**
	 * Returns the non-fuzzy prefix length. This is the number of characters at
	 * the start of a term that must be identical (not fuzzy) to the query term
	 * if the query is to match that term.
	 */
	public int getPrefixLength() {
		return prefixLength;
	}

	/**
	 * Returns the pattern term.
	 */
	public Term getTerm() {
		return term;
	}

	@Override
	protected TermsEnum getTermsEnum(final Terms terms,
			final AttributeSource atts) throws IOException {
		if ((maxEdits == 0) || (prefixLength >= term.text().length())) { // can
																			// only
			// match
			// if
			// it's
			// exact
			return new SingleTermsEnum(terms.iterator(null), term.bytes());
		}
		return new FuzzyTermsEnum(terms, atts, getTerm(), maxEdits,
				prefixLength, transpositions);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + maxEdits;
		result = (prime * result) + prefixLength;
		result = (prime * result) + maxExpansions;
		result = (prime * result) + (transpositions ? 0 : 1);
		result = (prime * result) + ((term == null) ? 0 : term.hashCode());
		return result;
	}

	@Override
	public String toString(final String field) {
		final StringBuilder buffer = new StringBuilder();
		if (!term.field().equals(field)) {
			buffer.append(term.field());
			buffer.append(':');
		}
		buffer.append(term.text());
		buffer.append('~');
		buffer.append(Integer.toString(maxEdits));
		buffer.append(ToStringUtils.boost(getBoost()));
		return buffer.toString();
	}
}
