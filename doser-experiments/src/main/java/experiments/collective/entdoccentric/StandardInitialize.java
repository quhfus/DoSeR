package experiments.collective.entdoccentric;


import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import experiments.collective.entdoccentric.StandardQueryDataObject.EntityObject;
import experiments.collective.entdoccentric.query.QuerySettings;

public class StandardInitialize {

	public StandardInitialize() {
	}

	public Query createQuery(EntityObject object,
			QuerySettings settings) {
		Query query = null;
		if (settings.isDocumentcentric()) {
			BooleanQuery bq = new BooleanQuery();
			if (settings.isDescriptionFuzzy()) {
				bq.add(new FuzzyQuery(new Term("titleandabs", object
						.getText())), Occur.MUST);
				if (settings.isUseDescription()) {
					String[] words = object.getContext().split(" ");
					for (int i = 0; i < words.length; i++) {
						bq.add(new FuzzyQuery(new Term("titleandabs", words[i])),
								Occur.SHOULD);
					}
				}
			} else {
				bq.add(new TermQuery(new Term("titleandabs", object
						.getText())), Occur.MUST);
				if (settings.isUseDescription()) {
					String[] words = object.getContext().split(" ");
					for (int i = 0; i < words.length; i++) {
						bq.add(new TermQuery(new Term("titleandabs", words[i])),
								Occur.SHOULD);
					}
				}
			}
			query = bq;
		} else if (!settings.isDocumentcentric()) {
			//StandardQuery Achtung nicht löschen!
			BooleanQuery bq = new BooleanQuery();
			if (settings.isDescriptionFuzzy()) {
				bq.add(new TermQuery(new Term("title", object.getText())),
						Occur.SHOULD);
				if (settings.isUseDescription()) {
					String[] words = object.getContext().split(" ");
					for (int i = 0; i < words.length; i++) {
						bq.add(new FuzzyQuery(new Term("description", words[i])),
								Occur.SHOULD);
					}
				}
			} else {
				bq.add(new FuzzyQuery(new Term("title", object.getText())),
						Occur.SHOULD);
				if (settings.isUseDescription()) {
					String[] words = object.getContext().split(" ");
					for (int i = 0; i < words.length; i++) {
						bq.add(new TermQuery(new Term("description", words[i])),
								Occur.SHOULD);
						System.out.println("description");
					}
				}
			}

//			SensePriorQuery q = new SensePriorQuery(object.getKeyword());
			query = bq;
		}
		return query;
	}

}
