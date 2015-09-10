package lda.wikievidence.dataconstruction;


import hbase.operations.HBaseOperations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class S3ConstructHBaseEntries {

	public void initializeSurfaceFormDirections(String f) {
		File file = new File(f);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			int counter = 0;
			while ((line = reader.readLine()) != null) {
				String splitter[] = line.split("\\t");
				String mentions[] = splitter[1].replaceFirst("\\|", "").split("\\|");
				String entity = splitter[0];
				entity.replaceAll(".html", "");
				counter++;
				for (int i = 0; i < mentions.length; i++) {
					String mentionSplit[] = mentions[i].split("---");
					String sf = mentionSplit[0];
					String context = mentionSplit[1];
					sf = sf.toLowerCase().trim();
					if(sf.length() > 2) {
						HBaseOperations.getInstance().addRecord("LDADC_EntToSf", entity, "data", String.valueOf(context.hashCode()), sf);
						HBaseOperations.getInstance().addRecord("LDADC_SFToEnt", sf, "data", String.valueOf(context.hashCode()), entity);
					}
				}
				if(counter % 10000 == 0) {
					System.out.println("Output: "+counter);
				}
//				counter++;
			}
			reader.close();
			System.out.println(counter);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {		
		S3ConstructHBaseEntries s = new S3ConstructHBaseEntries();
		s.initializeSurfaceFormDirections(args[0]);
	}
	
}
