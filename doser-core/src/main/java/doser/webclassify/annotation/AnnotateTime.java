package doser.webclassify.annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.Time;
//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.pipeline.AnnotationPipeline;
//import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
//import edu.stanford.nlp.pipeline.PTBTokenizerAnnotator;
//import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
//import edu.stanford.nlp.time.TimeAnnotations;
//import edu.stanford.nlp.time.TimeAnnotator;
//import edu.stanford.nlp.util.CoreMap;

//public class AnnotateTime {
//
//	private static volatile AnnotateTime instance = null;
//
//	private AnnotationPipeline pipeline;
//
//	private AnnotateTime() {
//		super();
//		Properties props = new Properties();
//		this.pipeline = new AnnotationPipeline();
//		pipeline.addAnnotator(new PTBTokenizerAnnotator());
//		pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
//		pipeline.addAnnotator(new POSTaggerAnnotator(false));
//		pipeline.addAnnotator(new TimeAnnotator("sutime", props));
//	}
//
//	public List<Time> annotateTime(Map<DisambiguatedEntity, Integer> map,
//			String context) {
//		List<Time> time = new ArrayList<Time>();
//		List<String> sentences = splitText(context);
//		for (int i = 0; i < sentences.size(); ++i) {
//			String sentence = sentences.get(i);
//			Annotation annotation = new Annotation(sentence);
//			annotation.set(CoreAnnotations.DocDateAnnotation.class,
//					"2015-09-16");
//			this.pipeline.annotate(annotation);
//			List<CoreMap> timexAnnsAll = annotation
//					.get(TimeAnnotations.TimexAnnotations.class);
//			for (CoreMap cm : timexAnnsAll) {
//				Time t = new Time();
////				List<CoreLabel> tokens = cm
////						.get(CoreAnnotations.TokensAnnotation.class);
//				t.setMention(cm.toString());
//
//				String firstSentence = null;
//				String lastSentence = null;
//				if (i == 0) {
//					firstSentence = sentence;
//				} else {
//					firstSentence = sentences.get(i - 1);
//				}
//
//				if (i == sentences.size() - 1) {
//					lastSentence = sentence;
//				} else {
//					lastSentence = sentences.get(i + 1);
//				}
//				t.setRelevantEntities(extractRelevantEntities(firstSentence,
//						lastSentence, context, map));
//				System.out.println("TIME: "+cm.toString());
//				List<DisambiguatedEntity> l = t.getRelevantEntities();
//				for(DisambiguatedEntity e : l) {
//					System.out.println(e.getEntityUri());
//				}
//				time.add(t);
//			}
//		}
//		return time;
//	}
//
//	// Stanford Parser modifies sentences. 
//	// Quick and Dirty: Split sentences at ., ?, !
//	private List<String> splitText(String context) {
//		String[] splitter = context.split("[\\?\\!\\.]");
//		
////		Reader reader = new StringReader(context);
////		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
////		List<String> sentenceList = new ArrayList<String>();
////
////		for (List<HasWord> sentence : dp) {
////			String sentenceString = Sentence.listToString(sentence);
////			sentenceList.add(sentenceString.toString().replaceAll(" [\\,\\?\\!\\'\\.]", );
////		}
//		return Arrays.asList(splitter);
//	}
//
//	private List<DisambiguatedEntity> extractRelevantEntities(
//			String firstSentence, String lastSentence, String context,
//			Map<DisambiguatedEntity, Integer> map) {
//		int startOffset = context.indexOf(firstSentence);
//		int lastOffset = context.indexOf(lastSentence) + lastSentence.length();
////		System.out.println("Sentence: "+firstSentence + " StartOffSet: "+startOffset);
////		System.out.println("Sentence2: "+lastSentence + " EndOffSet: "+lastOffset);
//		List<DisambiguatedEntity> entityList = new ArrayList<DisambiguatedEntity>();
//		for (Map.Entry<DisambiguatedEntity, Integer> entry : map.entrySet()) {
//			DisambiguatedEntity ent = entry.getKey();
//			Set<Integer> s = ent.getOffset();
//			for (Integer i : s) {
//				if (startOffset <= i && lastOffset >= i) {
//					entityList.add(ent);
//				}
//			}
//		}
//		return entityList;
//	}
//
//	public static synchronized AnnotateTime getInstance() {
//		if (instance == null) {
//			instance = new AnnotateTime();
//		}
//		return instance;
//	}
//	
//	public static void main(String[] args) {
//		String context = "Her names were unusually complex. She was born Anne Isabella Milbanke, the only child of Sir Ralph Milbanke, 6th Baronet, and his wife the Hon. Judith Milbanke, sister of Thomas Noel, Viscount Wentworth.[1] When Lord Wentworth died, a few months after Anne's marriage to Lord Byron, Judith and her cousin Lord Scarsdale jointly inherited his estate. The family subsequently took the surname Noel over Milbanke.";
//		String[] splitter = context.split("[\\?\\!\\.]");
//		for (int i = 0; i < splitter.length; i++) {
//			System.out.println(splitter[i]);
//		}
//	}
//
//}
