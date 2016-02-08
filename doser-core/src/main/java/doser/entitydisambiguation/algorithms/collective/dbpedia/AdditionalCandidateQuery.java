package doser.entitydisambiguation.algorithms.collective.dbpedia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import doser.entitydisambiguation.algorithms.SurfaceForm;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.knowledgebases.EntityCentricKBDBpedia;
import doser.entitydisambiguation.properties.Properties;
import doser.lucene.query.TermQuery;

public class AdditionalCandidateQuery {

	private Pattern punctuationPattern;
	private Pattern parenthesesPattern;

	private EntityCentricKBDBpedia eckb;

	private int taskNumber;

	AdditionalCandidateQuery(EntityCentricKBDBpedia eckb) {
		super();
		this.eckb = eckb;
		this.punctuationPattern = Pattern.compile(" ([,!\\?\\.])");
		this.parenthesesPattern = Pattern.compile("(.+)[\\(\\[]+.*");
	}

	public SurfaceForm checkAdditionalSurfaceForms(EntityDisambiguationDPO dpo, int taskNumber) {
		if (Properties.getInstance().getCandidateExpansion()) {
			this.taskNumber = taskNumber;
			String mention = dpo.getSelectedText().replaceAll(" +", " ");
			
			/* Eliminate e.g. Bill , Gates to Bill, Gates */
			Matcher regexMatcher = punctuationPattern.matcher(mention);
			StringBuffer buffer = new StringBuffer();
			while (regexMatcher.find()) {
				String replacer = regexMatcher.group(1);
				replacer = Matcher.quoteReplacement(replacer);
				regexMatcher.appendReplacement(buffer, replacer);
			}
			regexMatcher.appendTail(buffer);
			String newSf = buffer.toString().trim();
			if (!dpo.getSelectedText().equalsIgnoreCase(newSf) && !newSf.equalsIgnoreCase("")) {
				ScoreDoc[] scoredocs = queryIndex(newSf, false);
				if (scoredocs != null && scoredocs.length > 0) {
					SurfaceForm sf = prepareSurfaceForm(scoredocs, dpo, newSf);
					if (sf != null) {
						return sf;
					}
				}
			}

			/* Parenteses replacement */
			regexMatcher = parenthesesPattern.matcher(mention);
			buffer = new StringBuffer();
			try {
				if (regexMatcher.find()) {
					String replacer = regexMatcher.group(1);
					replacer = Matcher.quoteReplacement(replacer);
					regexMatcher.appendReplacement(buffer, replacer);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			regexMatcher.appendTail(buffer);
			newSf = buffer.toString().trim();
			if (!dpo.getSelectedText().equalsIgnoreCase(newSf) && !newSf.equalsIgnoreCase("")) {
				ScoreDoc[] scoredocs = queryIndex(newSf, false);
				if (scoredocs != null && scoredocs.length > 0) {
					SurfaceForm sf = prepareSurfaceForm(scoredocs, dpo, newSf);
					if (sf != null) {
						return sf;
					}
				}
			}

			/* Replace numerations */
			mention = mention.replaceAll("\\d\\.*", "");
			mention = mention.replaceAll("\"", "");
			mention = mention.replaceAll(" +", " ");
			mention = mention.trim();
			if (!dpo.getSelectedText().equalsIgnoreCase(mention) && !mention.equalsIgnoreCase("")) {
				ScoreDoc[] scoredocs = queryIndex(mention, false);
				if (scoredocs != null && scoredocs.length > 0) {
					SurfaceForm sf = prepareSurfaceForm(scoredocs, dpo, mention);
					if (sf != null) {
						return sf;
					}
				}
			}
			
			/* Replace all special chars and normalize */
			mention = mention.replaceAll("[^a-zA-Z ]", "");
			mention = mention.replaceAll(" +", " ");
			mention = mention.trim();
			if (!dpo.getSelectedText().equalsIgnoreCase(mention) && !mention.equalsIgnoreCase("")) {
				ScoreDoc[] scoredocs = queryIndex(mention, false);
				if (scoredocs != null && scoredocs.length > 0) {
					SurfaceForm sf = prepareSurfaceForm(scoredocs, dpo, mention);
					if (sf != null) {
						return sf;
					}
				}
			}
			
			/*
			 * Perform FuzzyQuery if surface forms provides specific
			 * characteristics
			 */
			String originalSf = dpo.getSelectedText();
			if (originalSf.length() > 5 && originalSf.length() < 22) {
				ScoreDoc[] scoredocs = queryIndex(dpo.getSelectedText(), true);
				if (scoredocs != null && scoredocs.length > 0) {
					SurfaceForm sf = prepareSurfaceForm(scoredocs, dpo, dpo.getSelectedText());
					if (sf != null) {
						return sf;
					}
				}
			}
		}
		
		/* Create Empty Surface Form */
		ArrayList<String> l = new ArrayList<String>();
		SurfaceForm sf = new SurfaceForm(dpo.getSelectedText(), dpo.getContext(), l, taskNumber,
				dpo.getStartPosition());
		return sf;
	}

	private SurfaceForm prepareSurfaceForm(ScoreDoc[] score, EntityDisambiguationDPO dpo, String newsf) {
		IndexReader reader = eckb.getSearcher().getIndexReader();
		SurfaceForm f = null;
		try {
			if (score.length == 1) {
				final Document doc = reader.document(score[0].doc);
				ArrayList<String> l = new ArrayList<String>();
				l.add(doc.get("Mainlink"));
				f = new SurfaceForm(newsf, dpo.getContext(), l, taskNumber, dpo.getStartPosition());
			} else if (score.length > 1) {
				ArrayList<String> l = new ArrayList<String>();
				for (int j = 0; j < score.length; j++) {
					final Document doc = reader.document(score[j].doc);
					l.add(doc.get("Mainlink"));
				}
				f = new SurfaceForm(dpo.getSelectedText(), dpo.getContext(), l, taskNumber, dpo.getStartPosition());

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}

	private ScoreDoc[] queryIndex(String mention, boolean fuzzy) {
		ScoreDoc[] scoredocs = null;
		Query query = null;
		if (!fuzzy) {
			query = createQuery(mention, eckb);
		} else {
			query = new FuzzyQuery(new Term("UniqueLabel", mention.toLowerCase()));
		}
		IndexSearcher searcher = eckb.getSearcher();
		TopDocs top = null;
		try {
			top = searcher.search(query, 1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (top != null) {
			scoredocs = top.scoreDocs;
		}
		return scoredocs;
	}

	private Query createQuery(String sf, EntityCentricKBDBpedia kb) {
		String surfaceform = sf.toLowerCase();
		TermQuery query = new TermQuery(new Term("UniqueLabel", surfaceform));

		return query;
	}

	public static void main(String args[]) throws Exception {
		String test = "\\(";
		Pattern p = Pattern.compile("(.+)[\\(\\[]+.*");
		Matcher regexMatcher = p.matcher(test);
		StringBuffer builder = new StringBuffer();
		if (regexMatcher.find()) {
			String replacer = regexMatcher.group(1);
			replacer = Matcher.quoteReplacement(replacer);
			regexMatcher.appendReplacement(builder, replacer);
		}
		regexMatcher.appendTail(builder);
		System.out.println(builder.toString());
	}
}
