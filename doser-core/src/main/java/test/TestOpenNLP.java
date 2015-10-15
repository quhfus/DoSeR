package test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

public class TestOpenNLP {

	static String sentence = "The United States Armed Forces are the federal military forces of the United States. They consist of the Army, Navy, Marine Corps, Air Force, and Coast Guard. The United States has a strong tradition of civilian control of the military. The President of the United States is the military's overall head, and helps form military policy with the U.S. Department of Defense (DoD), a federal executive department, acting as the principal organ by which military policy is carried out. The DoD is headed by the Secretary of Defense, who is a civilian and Cabinet member. The Defense Secretary is second in the military's chain of command, just below the President, and serves as the principal assistant to the President in all DoD-related matters. To coordinate military action with diplomacy, the President has an advisory National Security Council headed by a National Security Advisor. Both the President and Secretary of Defense are advised by a seven-member Joint Chiefs of Staff, which includes the head of each of the Defense Department's service branches as well as the chief of the National Guard Bureau. Leadership is provided by the Chairman of the Joint Chiefs of Staff and the Vice Chairman of the Joint Chiefs of Staff. The Commandant of the Coast Guard is not a member of the Joint Chiefs of Staff.";
//	static String sentence = "LEICESTERSHIRE TAKE OVER AT TOP AFTER INNINGS VICTORY ";
	static Set<String> nounPhrases = new HashSet<>();

	public static void main(String[] args) {

		InputStream modelInParse = null;
		try {
			// load chunking model
			modelInParse = new FileInputStream(
					"/home/quh/Downloads/en-parser-chunking.bin"); // from
																	// http://opennlp.sourceforge.net/models-1.5/
			ParserModel model = new ParserModel(modelInParse);

			// create parse tree
			Parser parser = ParserFactory.create(model);

			long time = System.currentTimeMillis();
			Parse topParses[] = ParserTool.parseLine(sentence, parser, 1);

			// call subroutine to extract noun phrases
			for (Parse p : topParses)
				getNounPhrases(p);

			System.out.println(System.currentTimeMillis() - time);
			
			// print noun phrases
			for (String s : nounPhrases)
				System.out.println(s);

			// The Call
			// the Wild?
			// The Call of the Wild? //punctuation remains on the end of
			// sentence
			// the author of The Call of the Wild?
			// the author
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelInParse != null) {
				try {
					modelInParse.close();
				} catch (IOException e) {
				}
			}
		}
	}

	// recursively loop through tree, extracting noun phrases
	public static void getNounPhrases(Parse p) {

		if (p.getType().equals("NN") || p.getType().equals("NNP")) { // NP=noun phrase
			nounPhrases.add(p.getCoveredText());
		}
		for (Parse child : p.getChildren())
			getNounPhrases(child);
	}
}