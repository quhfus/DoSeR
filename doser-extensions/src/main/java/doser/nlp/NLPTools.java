package doser.nlp;

import java.util.List;
import java.util.Properties;

//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.pipeline.StanfordCoreNLP;
//import edu.stanford.nlp.util.Pair;
//
//
//public class NLPTools {
//
//	private static volatile NLPTools instance;
//	
//	private StanfordCoreNLP pipeline;
//	
//	private NLPTools() { 
//		super();
//		Properties props = new Properties();
//		props.put("annotators", "tokenize, ssplit, pos, lemma, stopword");
//		props.setProperty("customAnnotatorClass.stopword",
//				"doser.nlp.StopWordAnnotator");
//		props.setProperty(StopWordAnnotator.STOPWORDS_LIST, StopWordAnnotator.customStopWordList);
//		props.setProperty(StopWordAnnotator.CHECK_LEMMA, "true");
//		
//		this.pipeline = new StanfordCoreNLP(props);
//	}
//
//    public static NLPTools getInstance() {
//        if (instance == null ) {
//            synchronized (NLPTools.class) {
//                if (instance == null) {
//                    instance = new NLPTools();
//                }
//            }
//        }
//        return instance;
//    }
//	
//	public String performLemmatizationAndStopWordRemoval(String str) {
//		Annotation document = new Annotation(str);
//		this.pipeline.annotate(document);
//		List<CoreLabel> tokens = document
//				.get(CoreAnnotations.TokensAnnotation.class);
//		StringBuilder builder = new StringBuilder();
//		for (CoreLabel token : tokens) {
//			 Pair<Boolean, Boolean> stopword = token.get(StopWordAnnotator.class);
//			 String lemma = token.lemma().toLowerCase();
//			 if(!stopword.first()) {
//				 builder.append(lemma);
//				 builder.append(" ");
//			 }
//		}
//		return builder.toString().trim();
//	}
//}
