package test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class test {

	public static void main(String[] args) {
		File file = new File("/mnt/storage/zwicklbauer/WikiParse/temp/plain");
		List<String> toDel = new LinkedList<String>();
		String f[] = file.list();
		for (int i = 0; i < f.length; i++) {
			if(f[i].matches("(\\d){4,4}(\\'2C_)(\\w){4,4}(\\d)+(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(January)(_)(\\d){4,4}(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(February)(_)(\\d){4,4}(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(March)(_)(\\d){4,4}(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(April)(_)(\\d){4,4}(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(May)(_)(\\d){4,4}(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(June)(_)(\\d){4,4}(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(July)(_)(\\d){4,4}(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(August)(_)(\\d){4,4}(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(September)(_)(\\d){4,4}(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(October)(_)(\\d){4,4}(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(November)(_)(\\d){4,4}(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(December)(_)(\\d){4,4}(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(Archive)(\\d)+(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(IncidentArchive)(\\d)+(.html)")) {
				toDel.add(f[i]);
			} else if(f[i].matches("(\\d)+(.html)")) {
				toDel.add(f[i]);
			}
		}
		
		for (String s : toDel) {
			File deleteFile = new File("/mnt/storage/zwicklbauer/WikiParse/temp/plain/"+s);
			System.out.println(deleteFile.delete());
		}
	}

}
