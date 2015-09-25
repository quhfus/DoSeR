package word2vec.evidencecomputation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import doser.word2vec.Word2VecModel;

@SuppressWarnings("deprecation")
public class W2VEvidenceMain {

	private Word2VecModel model;

	private PrintWriter writer;

	public W2VEvidenceMain(Word2VecModel model, String outputfile) {
		this.model = model;
		try {
			this.writer = new PrintWriter(new File(outputfile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void extractEvidences() {
		HashMap<String, Integer> map = this.model.getVocab();
		List<String> list = new ArrayList<String>();
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			String s = entry.getKey();
			if (s.contains("http://dbpedia.org/resource/")) {
				list.add(s);
			}
		}
		System.out.println("Entities to cover: "+list.size());
		int proc = Runtime.getRuntime().availableProcessors();
		int portion = (int) Math.ceil(((double) list.size()) / ((double) proc));

		System.out.println("Portion: "+portion);
		
		List<EvidenceThread> lstThread = new LinkedList<EvidenceThread>();
		
		int allTogether = 0;
		for(int i = 0; i < proc - 1; ++i) {
			int startindex = i * portion;
			List<String> sub = list.subList(startindex, startindex + portion);
			EvidenceThread thread = new EvidenceThread(model, sub, writer);
			lstThread.add(thread);
			System.out.println("Thread: "+sub.size());
			allTogether += sub.size();
		}
		List<String> lastsub = list.subList(portion * (proc - 1), list.size());
		EvidenceThread thread = new EvidenceThread(model, lastsub, writer);
		lstThread.add(thread);
		allTogether += lastsub.size();
		System.out.println("Thread: "+lastsub.size());
		System.out.println("Covered by Threads: "+allTogether);
		
		int poolsize = lstThread.size();
		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
				poolsize);
		ThreadPoolExecutor ex = new ThreadPoolExecutor(poolsize, poolsize, 3000000,
				TimeUnit.SECONDS, queue);
		for (EvidenceThread client : lstThread) {
			ex.execute(client);
		}
		ex.shutdown();
		try {
			while (!ex.awaitTermination(365, TimeUnit.DAYS)) {
				System.out.println("InitializationPhase not completed yet! Still waiting ");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.writer.close();
	}

	public static void main(String[] args) {
		Word2VecModel model = Word2VecModel.createWord2VecModel(args[0]);
		System.out.println("Model loaded!");
		W2VEvidenceMain main = new W2VEvidenceMain(model, args[1]);
		main.extractEvidences();
	}

}
