//
//import java.util.concurrent.AtomicLong;
//import java.util.concurrent.ConcurrentSkipListMap;
//
//class ParIterator[+A](iter : Iterator[A], queueSize : Int = concurrent.cores * 4)
//(implicit pool : Executor = pool) {
//  def map[B](fn : A=>B) : Iterator[B] = {
//    // number pulled from incoming iterator
//    val pulledN = new AtomicLong(0l);
//    
//    // signal for having put something in pulledQ
//    val pulledQI = new Object;
//    
//    // signal for having taken something out of pulledQ
//    val pulledQO = new Object;
//    
//    // queue of pulled values
//    val pulledQ = new ConcurrentSkipListMap[Long,Any];
//    
//    val pulledDone = new AtomicBoolean(iter.hasNext);
//    
//    // number processed by worker threads
//    val processedN = new AtomicLong(0l);
//    
//    // signal for having processed something from iterator
//    val processedS = new Object;
//    
//    // queue of processed values
//    val processedQ = new ConcurrentSkipListMap[Long,Any];
//    
//    // number pushed to consumer
//    val pushedN = new AtomicLong(0l);
//    
//    // signal for final consumer having read from us
//    val pushedS = new Object;
//    
//    // start the reader thread
//    daemon {
//      while (iter.hasNext) {
//        while (pulledQ.size >= queueSize) {
//          pulledQO.synchronized { pulledQO.wait }
//        }
//        pulledQ.put(pulledN.getAndIncrement, iter.next);
//        pulledQI.synchronized {
//          pulledQI.notify;
//        }
//      }
//      pulledDone.set(true);
//      pulledQI.synchronized { pulledQI.notifyAll; }
//    }
//    
//    // start the consumer threads
//    for (i <- 0 until concurrent.cores) {
//      pool.execute(new Runnable() {
//        def run = {
//          while (!pulledDone.get || processedN.get < pulledN.get) {
//            while (pulledQ.size == 0
//            pulledQI.synchronized { pulledQI.wait; }
//          }
//        }
//      })
//    }
//  }
//}
//
