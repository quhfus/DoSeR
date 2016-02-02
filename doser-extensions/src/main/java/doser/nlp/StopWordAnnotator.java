package doser.nlp;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

//import edu.stanford.nlp.ling.CoreAnnotation;
//import edu.stanford.nlp.pipeline.Annotator;
//
//import org.apache.lucene.analysis.core.StopAnalyzer;
//import org.apache.lucene.analysis.util.CharArraySet;
//import org.apache.lucene.util.Version;
//
//import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.util.Pair;

/**
 * User: jconwell CoreNlp Annotator that checks if in coming token is a stopword
 */
//public class StopWordAnnotator implements Annotator,
//		CoreAnnotation<Pair<Boolean, Boolean>> {
//
//	public static final String customStopWordList = "a, about, above, across, after, again, against, all, almost, alone, along, already, also, although, always, am, among, an, and, another, any, anybody, anyone, anything, anywhere, are, area, areas, aren't, around, as, ask, asked, asking, asks, at, away, b, back, backed, backing, backs, be, became, because, become, becomes, been, before, began, behind, being, beings, below, best, better, between, big, both, but, by, c, came, can, cannot, can't, case, cases, certain, certainly, clear, clearly, come, could, couldn't, d, did, didn't, differ, different, differently, do, does, doesn't, doing, done, don't, down, downed, downing, downs, during, e, each, early, either, end, ended, ending, ends, enough, even, evenly, ever, every, everybody, everyone, everything, everywhere, f, face, faces, fact, facts, far, felt, few, find, finds, first, for, four, from, full, fully, further, furthered, furthering, furthers, g, gave, general, generally, get, gets, give, given, gives, go, going, good, goods, got, great, greater, greatest, group, grouped, grouping, groups, h, had, hadn't, has, hasn't, have, haven't, having, he, he'd, he'll, her, here, here's, hers, herself, he's, high, higher, highest, him, himself, his, how, however, how's, i, i'd, if, i'll, i'm, important, in, interest, interested, interesting, interests, into, is, isn't, it, its, it's, itself, i've, j, just, k, keep, keeps, kind, knew, know, known, knows, l, large, largely, last, later, latest, least, less, let, lets, let's, like, likely, long, longer, longest, m, made, make, making, man, many, may, me, member, members, men, might, more, most, mostly, mr, mrs, much, must, mustn't, my, myself, n, necessary, need, needed, needing, needs, never, new, newer, newest, next, no, nobody, non, noone, nor, not, nothing, now, nowhere, number, numbers, o, of, off, often, old, older, oldest, on, once, one, only, open, opened, opening, opens, or, order, ordered, ordering, orders, other, others, ought, our, ours, ourselves, out, over, own, p, part, parted, parting, parts, per, perhaps, place, places, point, pointed, pointing, points, possible, present, presented, presenting, presents, problem, problems, put, puts, q, quite, r, rather, really, right, room, rooms, s, said, same, saw, say, says, second, seconds, see, seem, seemed, seeming, seems, sees, several, shall, shan't, she, she'd, she'll, she's, should, shouldn't, show, showed, showing, shows, side, sides, since, small, smaller, smallest, so, some, somebody, someone, something, somewhere, state, states, still, such, sure, t, take, taken, than, that, that's, the, their, theirs, them, themselves, then, there, therefore, there's, these, they, they'd, they'll, they're, they've, thing, things, think, thinks, this, those, though, thought, thoughts, three, through, thus, to, today, together, too, took, toward, turn, turned, turning, turns, two, u, under, until, up, upon, us, use, used, uses, v, very, w, want, wanted, wanting, wants, was, wasn't, way, ways, we, we'd, well, we'll, wells, went, were, we're, weren't, we've, what, what's, when, when's, where, where's, whether, which, while, who, whole, whom, who's, whose, why, why's, will, with, within, without, won't, work, worked, working, works, would, wouldn't, x, y, year, years, yes, yet, you, you'd, you'll, young, younger, youngest, your, you're, yours, yourself, yourselves, you've, z";
//
//	/**
//	 * stopword annotator class name used in annotators property
//	 */
//	public static final String ANNOTATOR_CLASS = "stopword";
//
//	public static final String STANFORD_STOPWORD = ANNOTATOR_CLASS;
//	public static final Requirement STOPWORD_REQUIREMENT = new Requirement(
//			STANFORD_STOPWORD);
//
//	/**
//	 * Property key to specify the comma delimited list of custom stopwords
//	 */
//	public static final String STOPWORDS_LIST = "stopword-list";
//
//	/**
//	 * Property key to specify if stopword list is case insensitive
//	 */
//	public static final String IGNORE_STOPWORD_CASE = "ignore-stopword-case";
//
//	/**
//	 * Property key to specify of StopwordAnnotator should check word lemma as
//	 * stopword
//	 */
//	public static final String CHECK_LEMMA = "check-lemma";
//
//	private static Class<? extends Pair> boolPair = Pair.makePair(true, true)
//			.getClass();
//
//	private Properties props;
//	private CharArraySet stopwords;
//	private boolean checkLemma;
//
//	public StopWordAnnotator(String annotatorClass, Properties props) {
//		this.props = props;
//
//		this.checkLemma = Boolean.parseBoolean(props.getProperty(CHECK_LEMMA,
//				"false"));
//
//		if (this.props.containsKey(STOPWORDS_LIST)) {
//			String stopwordList = props.getProperty(STOPWORDS_LIST);
//			boolean ignoreCase = Boolean.parseBoolean(props.getProperty(
//					IGNORE_STOPWORD_CASE, "false"));
//			this.stopwords = getStopWordList(Version.LATEST, stopwordList,
//					ignoreCase);
//		} else {
//			System.out.println("Ich hole mir die normalen Lucene StopWords");
//			this.stopwords = (CharArraySet) StopAnalyzer.ENGLISH_STOP_WORDS_SET;
//			System.out.println(this.stopwords.toString());
//		}
//	}
//
//	@Override
//	public void annotate(Annotation annotation) {
//		if (stopwords != null && stopwords.size() > 0
//				&& annotation.containsKey(TokensAnnotation.class)) {
//			List<CoreLabel> tokens = annotation.get(TokensAnnotation.class);
//			for (CoreLabel token : tokens) {
//				boolean isWordStopword = stopwords.contains(token.word()
//						.toLowerCase());
//				boolean isLemmaStopword = checkLemma ? stopwords.contains(token
//						.word().toLowerCase()) : false;
//				Pair<Boolean, Boolean> pair = Pair.makePair(isWordStopword,
//						isLemmaStopword);
//				token.set(StopWordAnnotator.class, pair);
//			}
//		}
//	}
//
//	@Override
//	public Set<Requirement> requirementsSatisfied() {
//		return Collections.singleton(STOPWORD_REQUIREMENT);//
//	}
//
//	@Override
//	public Set<Requirement> requires() {
//		if (checkLemma) {
//			return TOKENIZE_SSPLIT_POS_LEMMA;
//		} else {
//			return TOKENIZE_AND_SSPLIT;
//		}
//	}
//
//	@Override
//	@SuppressWarnings("unchecked")
//	public Class<Pair<Boolean, Boolean>> getType() {
//		return (Class<Pair<Boolean, Boolean>>) boolPair;
//	}
//
//	public static CharArraySet getStopWordList(Version luceneVersion,
//			String stopwordList, boolean ignoreCase) {
//		String[] terms = stopwordList.split(",");
//		CharArraySet stopwordSet = new CharArraySet(luceneVersion,
//				terms.length, ignoreCase);
//		for (String term : terms) {
//			stopwordSet.add(term.trim());
//		}
//		return CharArraySet.unmodifiableSet(stopwordSet);
//	}
//}
