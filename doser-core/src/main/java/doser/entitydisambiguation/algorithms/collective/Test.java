package doser.entitydisambiguation.algorithms.collective;

import java.util.LinkedList;
import java.util.List;

public class Test {

	private List<String> determineAbbreviationCandidates(String surfaceForm, List<CollectiveSFRepresentation> cans) {
		List<String> candidates = new LinkedList<String>();
		// Check Short surface form
		if(surfaceForm.length() <= 4 && (surfaceForm.replaceAll("[^a-zA-Z]", "")).length() <= 3 && surfaceForm.length() > 1) {
			String letters = surfaceForm.replaceAll("[^a-zA-Z]", "");
			for(CollectiveSFRepresentation sfrep : cans) {
				String sf = sfrep.getSurfaceForm();
				String splitter[] = sf.split(" ");
				if(splitter.length > 1) {
					boolean check = true;
					for(int i = 0; i < letters.length(); i++) {
						String letter = Character.toString(letters.charAt(i));
						System.out.println(splitter[i] + " "+letter.toLowerCase());
						if(!splitter[i].toLowerCase().startsWith(letter.toLowerCase())) {
							System.out.println("Hier nicht rein");
							check = false;
							break;
						}
					}
					if(check) {
						System.out.println("i add zweimal");
						System.out.println(sfrep.getCandidates());
						candidates.addAll(sfrep.getCandidates());
					} else {
						System.out.println("und einmal ned ");
					}
				}
			}
		}
		return candidates;
	}
}
