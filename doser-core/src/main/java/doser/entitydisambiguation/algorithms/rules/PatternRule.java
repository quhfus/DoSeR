package doser.entitydisambiguation.algorithms.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.knowledgebases.EntityCentricKBDBpedia;
import doser.general.HelpfulMethods;

class PatternRule extends AbstractRule {

	private static final int MINIMUMSURFACEFORMS = 14;
	private static final float OCCURRENCEPERCENTAGE = 0.25f;
	private static final String CUSTOMSTOPWORDLIST = "republic, people, party, national, a, about, above, across, after, again, against, all, almost, alone, along, already, also, although, always, am, among, an, and, another, any, anybody, anyone, anything, anywhere, are, area, areas, aren't, around, as, ask, asked, asking, asks, at, away, b, back, backed, backing, backs, be, became, because, become, becomes, been, before, began, behind, being, beings, below, best, better, between, big, both, but, by, c, came, can, cannot, can't, case, cases, certain, certainly, clear, clearly, come, could, couldn't, d, did, didn't, differ, different, differently, do, does, doesn't, doing, done, don't, down, downed, downing, downs, during, e, each, early, either, end, ended, ending, ends, enough, even, evenly, ever, every, everybody, everyone, everything, everywhere, f, face, faces, fact, facts, far, felt, few, find, finds, first, for, four, from, full, fully, further, furthered, furthering, furthers, g, gave, general, generally, get, gets, give, given, gives, go, going, good, goods, got, great, greater, greatest, group, grouped, grouping, groups, h, had, hadn't, has, hasn't, have, haven't, having, he, he'd, he'll, her, here, here's, hers, herself, he's, high, higher, highest, him, himself, his, how, however, how's, i, i'd, if, i'll, i'm, important, in, interest, interested, interesting, interests, into, is, isn't, it, its, it's, itself, i've, j, just, k, keep, keeps, kind, knew, know, known, knows, l, large, largely, last, later, latest, least, less, let, lets, let's, like, likely, long, longer, longest, m, made, make, making, man, many, may, me, member, members, men, might, more, most, mostly, mr, mrs, much, must, mustn't, my, myself, n, necessary, need, needed, needing, needs, never, new, newer, newest, next, no, nobody, non, noone, nor, not, nothing, now, nowhere, number, numbers, o, of, off, often, old, older, oldest, on, once, one, only, open, opened, opening, opens, or, order, ordered, ordering, orders, other, others, ought, our, ours, ourselves, out, over, own, p, part, parted, parting, parts, per, perhaps, place, places, point, pointed, pointing, points, possible, present, presented, presenting, presents, problem, problems, put, puts, q, quite, r, rather, really, right, room, rooms, s, said, same, saw, say, says, second, seconds, see, seem, seemed, seeming, seems, sees, several, shall, shan't, she, she'd, she'll, she's, should, shouldn't, show, showed, showing, shows, side, sides, since, small, smaller, smallest, so, some, somebody, someone, something, somewhere, state, states, still, such, sure, t, take, taken, than, that, that's, the, their, theirs, them, themselves, then, there, therefore, there's, these, they, they'd, they'll, they're, they've, thing, things, think, thinks, this, those, though, thought, thoughts, three, through, thus, to, today, together, too, took, toward, turn, turned, turning, turns, two, u, under, until, up, upon, us, use, used, uses, v, very, w, want, wanted, wanting, wants, was, wasn't, way, ways, we, we'd, well, we'll, wells, went, were, we're, weren't, we've, what, what's, when, when's, where, where's, whether, which, while, who, whole, whom, who's, whose, why, why's, will, with, within, without, won't, work, worked, working, works, would, wouldn't, x, y, year, years, yes, yet, you, you'd, you'll, young, younger, youngest, your, you're, yours, yourself, yourselves, you've, z";

	PatternRule(EntityCentricKBDBpedia eckb) {
		super(eckb);
	}

	@Override
	public boolean applyRule(List<SurfaceForm> rep) {
		if (rep.size() > MINIMUMSURFACEFORMS) {
			Map<String, Integer> map = generateDictionary(rep);
			@SuppressWarnings("deprecation")
			List<Map.Entry<String, Integer>> list = HelpfulMethods
					.sortByValue(map);
			if(list.size() == 0) {
				return false;
			}
			Map.Entry<String, Integer> entry = list.get(0);
			String termToWatch = entry.getKey();

			float perc = computePercentage(termToWatch, rep);
			if (perc > OCCURRENCEPERCENTAGE) {
				disambiguateTerms(termToWatch, rep);
			}
		}
		return false;
	}

	private Map<String, Integer> generateDictionary(List<SurfaceForm> rep) {
		// Check SurfaceForms HashMap
		Set<String> sfStrings = new HashSet<String>();

		// Generate Dictionary
		Map<String, Integer> dictionary = new HashMap<String, Integer>();
		for (SurfaceForm sf : rep) {
			List<String> strList = sf.getCandidates();
			String s = sf.getSurfaceForm().toLowerCase();
			if (!sfStrings.contains(s)) {
				Set<String> usedWords = new HashSet<String>();
				for (String str : strList) {
					String ending = str.replaceAll(
							"http://dbpedia.org/resource/", "").toLowerCase();
					String[] split = ending.split("_");
					if (split.length > 1) {
						for (int i = 1; i < split.length; i++) {
							if (!usedWords.contains(split[i])) {
								if (!CUSTOMSTOPWORDLIST.contains(split[i])) {
									if (dictionary.containsKey(split[i])) {
										Integer in = dictionary.get(split[i]);
										dictionary.put(split[i], ++in);
									} else {
										dictionary.put(split[i], 1);
									}
									usedWords.add(split[i]);
								}
							}
						}
					}
				}
				sfStrings.add(s);
			}
		}
		return dictionary;
	}

	private float computePercentage(String str, List<SurfaceForm> rep) {
		int occ = 0;
		HashSet<String> hash = new HashSet<String>();
		for (SurfaceForm sf : rep) {
			List<String> l = sf.getCandidates();
			String form = sf.getSurfaceForm().toLowerCase();
			if (!hash.contains(form)) {
				for (String s : l) {
					s = s.replaceAll("http://dbpedia.org/resource/", "")
							.toLowerCase();
					if (s.contains("_"+str)) {
						occ++;
						break;
					}
				}
				hash.add(form);
			}
		}
		float perc = (float) occ / (float) rep.size();
		return perc;
	}

	private void disambiguateTerms(String str, List<SurfaceForm> rep) {
		for (SurfaceForm sf : rep) {
			if (rep.size() > 1) {
				List<String> l = sf.getCandidates();
				List<String> candidates = new ArrayList<String>();
				for (String s : l) {
					String st = s
							.replaceAll("http://dbpedia.org/resource/", "")
							.toLowerCase();
					if (st.contains("_" + str)) {
						candidates.add(s);
					}
				}
				if (candidates.size() == 1
						&& !candidates.get(0).matches(".*\\d+.*")) {
					sf.setDisambiguatedEntity(candidates.get(0));
					sf.setInitial(true);
				}
			}
		}
	}

	public static void main(String[] args) {
		List<String> l1 = new ArrayList<String>();
		l1.add("http://dbpedia.org/resource/Leicestershire");
		l1.add("http://dbpedia.org/resource/Leicestershire_Cricket_Country_Club");
		l1.add("http://dbpedia.org/resource/Leicestershire_Testing_F.C.");
		List<String> l2 = new ArrayList<String>();
		l2.add("http://dbpedia.org/resource/Derbyshire");
		l2.add("http://dbpedia.org/resource/Derbyshire_Cricket_Country_Club");
		l2.add("http://dbpedia.org/resource/Derbyshire_Testing1_F.C.");
		List<String> l3 = new ArrayList<String>();
		l3.add("http://dbpedia.org/resource/Essex");
		l3.add("http://dbpedia.org/resource/Essex_Cricket_Country_Club");
		l3.add("http://dbpedia.org/resource/Essex_Testing2_F.C.");
		List<String> l4 = new ArrayList<String>();
		l4.add("London");
		l4.add("London_Theatre");
		l4.add("London_Theatre_Test_F.C.");
		SurfaceForm sf1 = new SurfaceForm("Leicestershire", "", l1, 0, 1);
		SurfaceForm sf2 = new SurfaceForm("Derbyshire", "", l2, 0, 1);
		SurfaceForm sf3 = new SurfaceForm("London", "", l3, 0, 1);
		SurfaceForm sf4 = new SurfaceForm("London", "", l4, 0, 1);
		List<SurfaceForm> sf = new LinkedList<SurfaceForm>();
		sf.add(sf1);
		sf.add(sf2);
		sf.add(sf3);
		sf.add(sf4);

		PatternRule pattern = new PatternRule(null);
		pattern.applyRule(sf);
	}
}
