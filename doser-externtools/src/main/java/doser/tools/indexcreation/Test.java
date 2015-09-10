package doser.tools.indexcreation;

public class Test {

	public static void main(String[] args) {
		String ent = "Mission: Impossible 2:2";
		String[] occ = ent.split(":");

		StringBuilder builder = new StringBuilder();
		for (int j = 0; j < occ.length - 1; j++) {
			builder.append(occ[j]+":");
		}
		int nr = Integer.valueOf(occ[occ.length - 1]);
		String mention = builder.toString();
		mention = mention.substring(0, mention.length() - 1);
		System.out.println(mention);
		System.out.println(nr);
	}

}
