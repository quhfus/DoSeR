package doser.entitydisambiguation.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import doser.entitydisambiguation.algorithms.DisambiguationAlgorithm;
import doser.entitydisambiguation.algorithms.DisambiguationHandler;
import doser.entitydisambiguation.knowledgebases.KnowledgeBase;
import doser.entitydisambiguation.knowledgebases.KnowledgeBaseIdentifiers;
import doser.entitydisambiguation.properties.Properties;
import opennlp.tools.parser.Parser;

public final class DisambiguationMainService {

	public final static int MAXCLAUSECOUNT = 4096;

	private static final int TIMERPERIOD = 10000;

	private static DisambiguationMainService instance = null;

	private Model hdtdbpediaCats;
	private Model hdtdbpediaCatsL;
	private Model hdtdbpediaDesc;
	private Model hdtdbpediaLabels;
	private Model hdtdbpediaSkosCategories;
	private Model hdtdbpediaInstanceTypes;
	private Model hdtYagoCatsLab;
	private Model hdtyagoTaxonomy;
	private Model hdtyagoTransTypes;

	private Parser openNLP_parser;

	private Map<KnowledgeBaseIdentifiers, KnowledgeBase> knowledgebases;

	private List<Timer> timerList;

	/**
	 * The DisambiguationMainService Constructor specifies a set of knowledge
	 * bases which are used for disambiguation. Dynamic knowledge bases will be
	 * initialized in a background thread loader. The static knowledge bases are
	 * initialized within the PriorLoader class. The Apache Lucene searchers and
	 * readers are created in the constructor of the EntityCentricDisambiguation
	 * class. /**
	 */
	private DisambiguationMainService() {
		super();
		this.knowledgebases = new EnumMap<KnowledgeBaseIdentifiers, KnowledgeBase>(
				KnowledgeBaseIdentifiers.class);
//		this.knowledgebases.put(KnowledgeBaseIdentifiers.Standard,
//				new EntityCentricKnowledgeBaseDefault(Properties.getInstance()
//						.getEntityCentricKBWikipedia(), false,
//						new DefaultSimilarity()));
		// this.knowledgebases.put(KnowledgeBaseIdentifiers.CSTable,
		// new EnCenKBCStable(Properties.getInstance().getCSTableIndex(),
		// false, new DefaultSimilarity()));
		// this.knowledgebases.put(KnowledgeBaseIdentifiers.DbPediaBiomedCopy,
		// new EntityCentricKnowledgeBaseDefault(Properties.getInstance()
		// .getDbPediaBiomedCopyKB(), true,
		// new DefaultSimilarity()));
		// this.knowledgebases.put(
		// KnowledgeBaseIdentifiers.DocumentCentricDefault,
		// new DocumentCentricKnowledgeBaseDefault(Properties
		// .getInstance().getDocumentCentricKB(), false,
		// new DefaultSimilarity()));

		// Create Timer thread, which periodically calls the IndexReader updates
		// for dynamic knowledge bases
		this.timerList = new ArrayList<Timer>();
		for (KnowledgeBase kb : this.knowledgebases.values()) {
			Timer timer = new Timer();
			this.timerList.add(timer);
			timer.scheduleAtFixedRate(kb, 0, TIMERPERIOD);
		}

		int threadSize = knowledgebases.size();
		if (threadSize > 0) {
			BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
					threadSize);
			ThreadPoolExecutor ex = new ThreadPoolExecutor(threadSize,
					threadSize, 100, TimeUnit.SECONDS, queue);
			for (KnowledgeBase kb : knowledgebases.values()) {
				ex.execute(new KnowledgeBaseInitializationThread(kb));
			}
			ex.shutdown();
			try {
				while (!ex.awaitTermination(100, TimeUnit.SECONDS)) {
					Logger.getRootLogger().info(
							"InitializationPhase not completed yet! Still waiting "
									+ ex.getActiveCount());
				}
			} catch (InterruptedException e) {
				Logger.getRootLogger().warn(e.getStackTrace());
			}
		}

		// this.loadRelations();
		try {
			final HDT hdt = HDTManager.mapIndexedHDT(Properties.getInstance()
					.getDBPediaArticleCategories(), null);
//			final HDT hdt1 = HDTManager.mapIndexedHDT(Properties.getInstance()
//					.getYagoTransitiveTypes(), null);
//			final HDT hdt2 = HDTManager.mapIndexedHDT(Properties.getInstance()
//					.getYagoTaxonomy(), null);
			final HDT hdt3 = HDTManager.mapIndexedHDT(Properties.getInstance()
					.getDBPediaCategoryLabels(), null);
//			final HDT hdt4 = HDTManager.mapIndexedHDT(Properties.getInstance()
//					.getYagoCategoryLabels(), null);
			final HDT hdt5 = HDTManager.mapIndexedHDT(Properties.getInstance()
					.getDBPediaLabels(), null);
			final HDT hdt6 = HDTManager.mapIndexedHDT(Properties.getInstance()
					.getDBPediaDescriptions(), null);
			final HDT hdt7 = HDTManager.mapIndexedHDT(Properties.getInstance()
					.getDBpediaSkosCategories(), null);
			final HDT hdt8 = HDTManager.mapIndexedHDT(Properties.getInstance()
					.getDBpediaInstanceTypes(), null);
			final HDTGraph graph = new HDTGraph(hdt);
//			final HDTGraph graph1 = new HDTGraph(hdt1);
//			final HDTGraph graph2 = new HDTGraph(hdt2);
			final HDTGraph graph3 = new HDTGraph(hdt3);
//			final HDTGraph graph4 = new HDTGraph(hdt4);
			final HDTGraph graph5 = new HDTGraph(hdt5);
			final HDTGraph graph6 = new HDTGraph(hdt6);
			final HDTGraph graph7 = new HDTGraph(hdt7);
			final HDTGraph graph8 = new HDTGraph(hdt8);
			this.hdtdbpediaCats = ModelFactory.createModelForGraph(graph);
//			this.hdtyagoTransTypes = ModelFactory.createModelForGraph(graph1);
//			this.hdtyagoTaxonomy = ModelFactory.createModelForGraph(graph2);
			this.hdtdbpediaCatsL = ModelFactory.createModelForGraph(graph3);
//			this.hdtYagoCatsLab = ModelFactory.createModelForGraph(graph4);
			this.hdtdbpediaLabels = ModelFactory.createModelForGraph(graph5);
			this.hdtdbpediaDesc = ModelFactory.createModelForGraph(graph6);
			this.hdtdbpediaSkosCategories = ModelFactory
					.createModelForGraph(graph7);
			this.hdtdbpediaInstanceTypes = ModelFactory
					.createModelForGraph(graph8);
		} catch (final IOException e) {
			e.printStackTrace();
		}

		// Load OpenNLP Model
//		ParserModel model = null;
//		try {
//			model = new ParserModel(new FileInputStream(new File(Properties
//					.getInstance().getNounPhraseModel())));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		if (model != null) {
//			this.openNLP_parser = ParserFactory.create(model);
//		}
	}

	public synchronized static DisambiguationMainService getInstance() {
		if (instance == null) {
			instance = new DisambiguationMainService();
		}
		return instance;
	}

	public static void initialize() {
		instance = new DisambiguationMainService();
	}

	public void disambiguate(List<DisambiguationTask> taskList) {
		for (int i = 0; i < taskList.size(); i++) {
			DisambiguationTask task = taskList.get(i);
			DisambiguationAlgorithm algorithm = DisambiguationHandler
					.getInstance().getAlgorithm(task);
			if (algorithm != null) {
				task.setKb(this.knowledgebases.get(task.getKbIdentifier()));
				algorithm.disambiguate(task);
			}
		}
	}

	public Model getDBPediaArticleCategories() {
		return this.hdtdbpediaCats;
	}

	public Model getDBPediaCategoryLabels() {
		return this.hdtdbpediaCatsL;
	}

	public Model getDBPediaDescription() {
		return this.hdtdbpediaDesc;
	}

	public Model getDBPediaInstanceTypes() {
		return this.hdtdbpediaInstanceTypes;
	}

	public Model getDBPediaLabels() {
		return this.hdtdbpediaLabels;
	}

	public Model getDBpediaSkosCategories() {
		return this.hdtdbpediaSkosCategories;
	}

	public Model getYagoCategoryLabels() {
		return this.hdtYagoCatsLab;
	}

	public Model getYagoTaxonomy() {
		return this.hdtyagoTaxonomy;
	}

	public Model getYagoTransitiveTypes() {
		return this.hdtyagoTransTypes;
	}

	public Parser getOpenNLP_parser() {
		return openNLP_parser;
	}



	/**
	 * A seperate thread class to initialize our knowledgebases
	 * 
	 * @author Stefan Zwicklbauer
	 */
	class KnowledgeBaseInitializationThread implements Runnable {

		private KnowledgeBase kb;

		public KnowledgeBaseInitializationThread(KnowledgeBase kb) {
			super();
			this.kb = kb;
		}

		@Override
		public void run() {
			kb.initialize();
		}
	}

	public void shutDownDisambiguationService() {
		for (Timer timer : timerList) {
			timer.cancel();
		}
	}
}
