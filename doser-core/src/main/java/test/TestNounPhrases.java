package test;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class TestNounPhrases {

	private LexicalizedParser parser;
	
	public static void main(String[] args) {
//		String text = "During the Second World War, Turing worked for the Government Code and Cypher School (GC&CS) at Bletchley Park, Britain's codebreaking centre. For a time he led Hut 8, the section responsible for German naval cryptanalysis. He devised a number of techniques for breaking German ciphers, including improvements to the pre-war Polish bombe method and an electromechanical machine that could find settings for the Enigma machine.";
		String text = "Leicestershire take over at top after Innings victory. London 1996-08-30 West Indian all-rounder Phil Simmons took four for 38 on Friday as Leicestershire beat Somerset by an innings and 39 runs in two days to take over at the head of the county championship. Their stay on top, though, may be short-lived as title rivals Essex, Derbyshire and Surrey all closed in on victory while Kent made up for lost time in their rain-affected match against Nottinghamshire.";
//		String text = "Cricket is their favorite play.";
		TestNounPhrases n = new TestNounPhrases();
		long time = System.currentTimeMillis();
		Tree t = n.doTest(text);
		List<String> list = n.getNounPhrases(t);
		System.out.println(System.currentTimeMillis() - time);
		for(String s : list) {
			System.out.println(s);
		}
		time = System.currentTimeMillis();
		t = n.doTest(text);
		list = n.getNounPhrases(t);
		System.out.println(System.currentTimeMillis() - time);
		for(String s : list) {
			System.out.println(s);
		}
	}
	
	public TestNounPhrases () {
		this.parser =  LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
	}

	public Tree doTest(String text) {
		Tree parseS = (Tree) parser.parse(text);
		return parseS;
	}

	private List<String> getNounPhrases(Tree parse) {
		List<String> result = new ArrayList<>();
		TregexPattern pattern = TregexPattern.compile("@NN");
		TregexMatcher matcher = pattern.matcher(parse);
		while (matcher.find()) {
			Tree match = matcher.getMatch();
			List<Tree> leaves = match.getLeaves();
			String nounPhrase = Joiner.on(' ').join(
					Lists.transform(leaves, Functions.toStringFunction()));
			result.add(nounPhrase);
		}
		pattern = TregexPattern.compile("@NNP");
		matcher = pattern.matcher(parse);
		while (matcher.find()) {
			Tree match = matcher.getMatch();
			List<Tree> leaves = match.getLeaves();
			String nounPhrase = Joiner.on(' ').join(
					Lists.transform(leaves, Functions.toStringFunction()));
			result.add(nounPhrase);
		}
		return result;
	}

}
