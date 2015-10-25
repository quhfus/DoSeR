package doser.nlp;

public class Test {

	public static void main(String[] args) {
		String test = "Turing was prosecuted in 1952 for homosexual acts, when such behaviour was still a criminal act in the UK. He accepted treatment with oestrogen injections (chemical castration) as an alternative to prison. Turing died in 1954, 16 days before his 42nd birthday, from cyanide poisoning. An inquest determined his death as suicide, but it has been noted that the known evidence is equally consistent with accidental poisoning.[8] In 2009, following an Internet campaign, British Prime Minister Gordon Brown made an official public apology on behalf of the British government for the appalling way he was treated. Queen Elizabeth II granted him a posthumous pardon in 2013.";
		System.out.println(NLPTools.getInstance().toString());
		long time = System.currentTimeMillis();
		System.out.println(NLPTools.getInstance().performLemmatizationAndStopWordRemoval(test));
		System.out.println("Zeit: "+(System.currentTimeMillis() - time));
	}

}
