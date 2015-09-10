package lda.wikievidence.modelcreation;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class LDAExecutor {

	public void executeThreadPool(List<LDAClient> lst) {
		int poolsize = lst.size();
		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
				poolsize);
		ThreadPoolExecutor ex = new ThreadPoolExecutor(poolsize, poolsize, 300,
				TimeUnit.SECONDS, queue);
		for (LDAClient client : lst) {
			ex.execute(client);
		}
		ex.shutdown();
		try {
			while (!ex.awaitTermination(300, TimeUnit.SECONDS)) {
				Logger.getRootLogger().info(
						"InitializationPhase not completed yet! Still waiting "
								+ ex.getActiveCount());
			}
		} catch (InterruptedException e) {
			Logger.getRootLogger().warn(e.getStackTrace());
		}

	}

}
