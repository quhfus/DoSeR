package doser.tools.indexcreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;

public class MergeEntityLists {

	public static void main(String[] args) throws Exception {
		HashSet<String> set = new HashSet<String>();
		PrintWriter writer = new PrintWriter(
				"/home/zwicklbauer/WikipediaEntities/entityListMerged.dat");
		BufferedReader reader1 = new BufferedReader(new FileReader(new File("/home/zwicklbauer/WikipediaEntities/entityList.dat")));
		String line = null;
		while((line = reader1.readLine()) != null) {
			set.add(line);
		}
		BufferedReader reader2 = new BufferedReader(new FileReader(new File("/home/zwicklbauer/WikipediaEntities/entityList_Big.dat")));
		line = null;
		while((line = reader2.readLine()) != null) {
			set.add(line);
		}
		
		for(String s : set) {
			writer.println(s);
		}
		reader1.close();
		reader2.close();
		writer.close();
	}

}
