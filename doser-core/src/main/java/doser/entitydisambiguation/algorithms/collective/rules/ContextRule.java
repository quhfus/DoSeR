package doser.entitydisambiguation.algorithms.collective.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import doser.entitydisambiguation.algorithms.collective.hybrid.SurfaceForm;
import doser.entitydisambiguation.algorithms.collective.hybrid.Word2Vec;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;

public class ContextRule extends Rule {

	private static final int MINDISAMBIGUATEDSURFACEFORMS = 3;

	private static final int MINIMUMSURFACEFORMS = 10;

	private static final String CUSTOMSTOPWORDLIST = "republic, people, party, national, a, about, above, across, after, again, against, all, almost, alone, along, already, also, although, always, am, among, an, and, another, any, anybody, anyone, anything, anywhere, are, area, areas, aren't, around, as, ask, asked, asking, asks, at, away, b, back, backed, backing, backs, be, became, because, become, becomes, been, before, began, behind, being, beings, below, best, better, between, big, both, but, by, c, came, can, cannot, can't, case, cases, certain, certainly, clear, clearly, come, could, couldn't, d, did, didn't, differ, different, differently, do, does, doesn't, doing, done, don't, down, downed, downing, downs, during, e, each, early, either, end, ended, ending, ends, enough, even, evenly, ever, every, everybody, everyone, everything, everywhere, f, face, faces, fact, facts, far, felt, few, find, finds, first, for, four, from, full, fully, further, furthered, furthering, furthers, g, gave, general, generally, get, gets, give, given, gives, go, going, good, goods, got, great, greater, greatest, group, grouped, grouping, groups, h, had, hadn't, has, hasn't, have, haven't, having, he, he'd, he'll, her, here, here's, hers, herself, he's, high, higher, highest, him, himself, his, how, however, how's, i, i'd, if, i'll, i'm, important, in, interest, interested, interesting, interests, into, is, isn't, it, its, it's, itself, i've, j, just, k, keep, keeps, kind, knew, know, known, knows, l, large, largely, last, later, latest, least, less, let, lets, let's, like, likely, long, longer, longest, m, made, make, making, man, many, may, me, member, members, men, might, more, most, mostly, mr, mrs, much, must, mustn't, my, myself, n, necessary, need, needed, needing, needs, never, new, newer, newest, next, no, nobody, non, noone, nor, not, nothing, now, nowhere, number, numbers, o, of, off, often, old, older, oldest, on, once, one, only, open, opened, opening, opens, or, order, ordered, ordering, orders, other, others, ought, our, ours, ourselves, out, over, own, p, part, parted, parting, parts, per, perhaps, place, places, point, pointed, pointing, points, possible, present, presented, presenting, presents, problem, problems, put, puts, q, quite, r, rather, really, right, room, rooms, s, said, same, saw, say, says, second, seconds, see, seem, seemed, seeming, seems, sees, several, shall, shan't, she, she'd, she'll, she's, should, shouldn't, show, showed, showing, shows, side, sides, since, small, smaller, smallest, so, some, somebody, someone, something, somewhere, state, states, still, such, sure, t, take, taken, than, that, that's, the, their, theirs, them, themselves, then, there, therefore, there's, these, they, they'd, they'll, they're, they've, thing, things, think, thinks, this, those, though, thought, thoughts, three, through, thus, to, today, together, too, took, toward, turn, turned, turning, turns, two, u, under, until, up, upon, us, use, used, uses, v, very, w, want, wanted, wanting, wants, was, wasn't, way, ways, we, we'd, well, we'll, wells, went, were, we're, weren't, we've, what, what's, when, when's, where, where's, whether, which, while, who, whole, whom, who's, whose, why, why's, will, with, within, without, won't, work, worked, working, works, would, wouldn't, x, y, year, years, yes, yet, you, you'd, you'll, young, younger, youngest, your, you're, yours, yourself, yourselves, you've, z";

	private static final float SIMILARITYTHRESHOLD = 1.57f;

	private Word2Vec w2v;

	public ContextRule(EntityCentricKnowledgeBaseDefault eckb, Word2Vec w2v) {
		super(eckb);
		this.w2v = w2v;
	}

	@Override
	public boolean applyRule(List<SurfaceForm> rep) {
		if (rep.size() > MINIMUMSURFACEFORMS) {
			List<String> list = new LinkedList<String>();
			for (SurfaceForm sf : rep) {
				if (rep.size() > 1 && sf.getCandidates().size() == 1 && sf.isInitial()) {
					list.add(sf.getCandidates().get(0));
				}
			}
			if (list.size() >= MINDISAMBIGUATEDSURFACEFORMS) {
				Set<String> w2vFormatStrings = new HashSet<String>();
				for (SurfaceForm sf : rep) {
					if (rep.size() > 1 && sf.getCandidates().size() > 1) {
						List<String> l = sf.getCandidates();
						List<String> bestCandidate = new LinkedList<String>();
//						System.out.println("CANDIDATES SURFACEFORM: "+sf.getSurfaceForm()+"    "+l.toString());
						for (String s : l) {
							String query = this.w2v.generateWord2VecFormatString(list, s);
							w2vFormatStrings.add(query);
							Map<String, Float> similarityMap = this.w2v.getWord2VecSimilarities(w2vFormatStrings);
							float simValue = similarityMap.get(query);
							// Check for Appropriate entities
//							System.out.println("RESULTS: "+list.toString() + "  "+s+"     "+simValue);
							if (simValue > SIMILARITYTHRESHOLD) {
								bestCandidate.add(s);
							}
						}
						// Disambiguate and assign entity
						if (!bestCandidate.isEmpty()) {
							sf.setCandidates(bestCandidate);
							System.out.println("Es bleibt übrig SurfaceForm: "+sf.getSurfaceForm() + "   +"+bestCandidate.toString());
						}
					}
				}
			}
		}
		return false;
	}

	// Make it faster maybe
	private void disambiguateTerms(String str, List<SurfaceForm> rep) {
		List<String> list = new LinkedList<String>();
		for (SurfaceForm sf : rep) {
			if (rep.size() > 1 && sf.getCandidates().size() == 1) {
				list.add(sf.getCandidates().get(0));
			}
		}

		if (list.size() >= MINDISAMBIGUATEDSURFACEFORMS) {
			Set<String> w2vFormatStrings = new HashSet<String>();
			for (SurfaceForm sf : rep) {
				if (rep.size() > 1 && sf.getCandidates().size() > 1) {
					List<String> l = sf.getCandidates();
					List<String> bestCandidate = new LinkedList<String>();
					for (String s : l) {
						String st = s.replaceAll("http://dbpedia.org/resource/", "").toLowerCase();
						if (st.contains("_" + str)) {
							w2vFormatStrings.clear();
							String query = this.w2v.generateWord2VecFormatString(list, s);
							w2vFormatStrings.add(query);
							Map<String, Float> similarityMap = this.w2v.getWord2VecSimilarities(w2vFormatStrings);
							float simValue = similarityMap.get(query);
							// Check for Appropriate entities
							System.out.println(
									"SurfaceForm" + sf.getSurfaceForm() + "Candidate: " + s + "    " + simValue);
							if (simValue > SIMILARITYTHRESHOLD) {
								bestCandidate.add(s);
							}
						}
					}
					// Disambiguate and assign entity
					if (!bestCandidate.isEmpty()) {
						sf.setCandidates(bestCandidate);
					}
				}
			}
		}
	}

	private float computePercentage(String str, List<SurfaceForm> rep) {
		int occ = 0;
		HashSet<String> hash = new HashSet<String>();
		for (SurfaceForm sf : rep) {
			List<String> l = sf.getCandidates();
			String form = sf.getSurfaceForm().toLowerCase();
			if (!hash.contains(form)) {
				for (String s : l) {
					s = s.replaceAll("http://dbpedia.org/resource/", "").toLowerCase();
					if (s.contains("_" + str)) {
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
					String ending = str.replaceAll("http://dbpedia.org/resource/", "").toLowerCase();
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

}
