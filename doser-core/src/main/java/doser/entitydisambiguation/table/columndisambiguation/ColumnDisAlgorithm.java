package doser.entitydisambiguation.table.columndisambiguation;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import doser.entitydisambiguation.properties.Properties;
import doser.entitydisambiguation.table.logic.LearnToRankTableDisambiguationOutput;
import doser.entitydisambiguation.table.logic.Table;
import doser.entitydisambiguation.table.logic.TableCell;
import doser.entitydisambiguation.table.logic.TableColumn;
import doser.entitydisambiguation.table.logic.Type;
import doser.tools.RDFGraphOperations;

public class ColumnDisAlgorithm {

	class RankedType implements Comparable<RankedType> {

		private final double[] featureVals;

		private final Type type;

		RankedType(final Type type, final double[] featureVals) {
			super();
			this.type = type;
			double sum = 0;
			for (final double featureVal : featureVals) {
				sum += featureVal;
			}
			this.featureVals = featureVals;
			this.type.setWeightedScore(sum);
		}

		@Override
		public int compareTo(final RankedType arg0) {
			if (this.type.getWeightedScore() < arg0.getScore()) {
				return 1;
			} else if (this.type.getWeightedScore() == arg0.getScore()) {
				return 0;
			} else {
				return -1;
			}
		}

		public double[] getFeatureVals() {
			return this.featureVals;
		}

		public double getScore() {
			return this.type.getWeightedScore();
		}

		public Type getType() {
			return this.type;
		}
	}

	private final static int GRAPHDEPTH = 4;

	/**
	 * SubtypeVersion 0 = Use Yago/Wordnet abstract types with max additional
	 * depth = 1 SubtypeVersion 1 = Use Yago/Wordnet abstract types with max
	 * additional depth = GRAPHDEPTH // Not implemented yet SubtypeVersion 2 =
	 * Use DBPedia Skos Broader abstract types // not implemented yed
	 */
	private final static int SUBTYPEVERSION = 0;

	private final AbstractTypeDisFeatures[] features;

	private List<String> gt;

	private InverseDocumentFrequencyFeature invDocFeature;

	private final LearnToRankTableDisambiguationOutput ltroutput;

	private final int numberBestOf;

	private int qryId;

	public ColumnDisAlgorithm() {
		super();
		this.features = new AbstractTypeDisFeatures[2];
		this.numberBestOf = 1;
		this.ltroutput = new LearnToRankTableDisambiguationOutput();
		this.qryId = 0;
	}

	public ColumnDisAlgorithm(final int bestOf) {
		super();
		this.features = new AbstractTypeDisFeatures[2];
		this.numberBestOf = bestOf;
		this.ltroutput = new LearnToRankTableDisambiguationOutput();
		this.qryId = 0;
	}

	private boolean checkColumnHeader(final Type type,
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g,
			final String header) {
		if (!header.equalsIgnoreCase("")) {
			if (type.getLayer() == 1) {
				if (!this.checkHeaderOnTransitiveTypes(g, type, header, 0)) {
					return false;
				}
			} else if (type.getLayer() > 1) {
				if (!this.matchHeaderString(type, header)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean checkHeaderOnTransitiveTypes(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g,
			final Type type, final String header, final int call) {
		if (this.matchHeaderString(type, header)) {
			return true;
		} else if (call == 3) {
			return false;
		} else {
			final Set<DefaultWeightedEdge> edgeSet = g.outgoingEdgesOf(type);
			final List<Type> l = new LinkedList<Type>();
			for (final DefaultWeightedEdge defaultWeightedEdge : edgeSet) {
				l.add(g.getEdgeTarget(defaultWeightedEdge));
			}
			for (final Type type2 : l) {
				if (this.checkHeaderOnTransitiveTypes(g, type2, header,
						(call + 1))) {
					return true;
				}
			}
			return false;
		}
	}

	private boolean checkYagoCategory(final Type type,
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g) {
		final Set<Type> vSet = g.vertexSet();
		for (final Type type2 : vSet) {
			if (type2.getLayer() == 1) {
				String falseType = type2.getUri();
				String correctedString = "";
				if (falseType.contains("Category:")) {
					final String[] splitter = falseType.split(":");
					correctedString = "http://yago-knowledge.org/resource/wikicategory_"
							+ splitter[2];
				} else {
					correctedString = falseType;
				}
				final List<Type> t = RDFGraphOperations
						.queryYagoCategories(correctedString);
				for (final Type type3 : t) {
					falseType = type.getUri();
					correctedString = "";
					if (falseType.contains("Category:")) {
						final String[] splitter = falseType.split(":");
						correctedString = "http://yago-knowledge.org/resource/wikicategory_"
								+ splitter[2];
					} else {
						correctedString = falseType;
					}
					if (type3.getUri().equalsIgnoreCase(correctedString)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void correctFollowingEdges(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g,
			final Type basicVertex, final double weight) {
		List<Type> fVertexes = new LinkedList<Type>();
		final Set<DefaultWeightedEdge> set = g.outgoingEdgesOf(basicVertex);
		for (final DefaultWeightedEdge defaultWeightedEdge : set) {
			fVertexes.add(g.getEdgeTarget(defaultWeightedEdge));
		}
		while (true) {
			final List<Type> followingcVertexes = new LinkedList<Type>();
			for (final Type type : fVertexes) {
				this.findAccumulatedWeightAndSet(g, type, weight);
				final Set<DefaultWeightedEdge> eSet = g.outgoingEdgesOf(type);
				for (final DefaultWeightedEdge defaultWeightedEdge : eSet) {
					followingcVertexes
							.add(g.getEdgeTarget(defaultWeightedEdge));
				}
			}
			if (followingcVertexes.isEmpty()) {
				break;
			} else {
				fVertexes = followingcVertexes;
			}
		}
	}

	private List<Type> createSubtypeVersion0(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g,
			final List<Type> lastlayerTypes) {
		final List<Type> newCreatedTypes = new LinkedList<Type>();
		for (final Type type : lastlayerTypes) {
			final List<Type> preD = RDFGraphOperations
					.getWordnetPredecessorTypesOfDbPediaCategory(type.getUri(),
							2);
			for (final Type t : preD) {
				if (g.containsVertex(t)) {
					final double accWeight = this
							.findAccumulatedWeight(g, type);
					this.findAccumulatedWeightAndSet(g, t, accWeight);
					final DefaultWeightedEdge e = new DefaultWeightedEdge();
					g.setEdgeWeight(e, 1.0);
					g.addEdge(type, t, e);
				} else {
					g.addVertex(t);
					final double accWeight = this
							.findAccumulatedWeight(g, type);
					this.findAccumulatedWeightAndSet(g, t, accWeight);
					final DefaultWeightedEdge e = new DefaultWeightedEdge();
					g.setEdgeWeight(e, 1.0);
					g.addEdge(type, t, e);
					newCreatedTypes.add(t);
				}
			}
		}
		return newCreatedTypes;
	}

	private List<Type> createSubtypeVersion1(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g,
			final List<Type> lastlayerTypes) {
		final List<Type> newCreatedTypes = new LinkedList<Type>();
		for (final Type type : lastlayerTypes) {
			final List<Type> preD = RDFGraphOperations
					.queryWordnetTypePredecessor(type.getUri(), 3);
			for (final Type t : preD) {
				if (g.containsVertex(t)) {
					final double accWeight = this
							.findAccumulatedWeight(g, type);
					this.findAccumulatedWeightAndSet(g, t, accWeight);
					final DefaultWeightedEdge e = new DefaultWeightedEdge();
					g.setEdgeWeight(e, 1.0);
					g.addEdge(type, t, e);
					this.correctFollowingEdges(g, t, accWeight);
				} else {
					g.addVertex(t);
					final double accWeight = this
							.findAccumulatedWeight(g, type);
					this.findAccumulatedWeightAndSet(g, t, accWeight);
					final DefaultWeightedEdge e = new DefaultWeightedEdge();
					g.setEdgeWeight(e, 1.0);
					g.addEdge(type, t, e);
					newCreatedTypes.add(t);
				}
			}
		}
		return newCreatedTypes;
	}

	private void createSubtypeVersion2(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g,
			final List<Type> lastlayerTypes) {
		for (final Type type : lastlayerTypes) {
			final List<Type> preD = RDFGraphOperations
					.queryWordnetTypePredecessor(type.getUri(), 3);
			for (final Type t : preD) {
				if (g.containsVertex(t)) {
					final double accWeight = this
							.findAccumulatedWeight(g, type);
					this.findAccumulatedWeightAndSet(g, t, accWeight);
					final DefaultWeightedEdge e = new DefaultWeightedEdge();
					g.setEdgeWeight(e, 1.0);
					g.addEdge(type, t, e);
					this.correctFollowingEdges(g, t, accWeight);
				} else {
					g.addVertex(t);
					final double accWeight = this
							.findAccumulatedWeight(g, type);
					this.findAccumulatedWeightAndSet(g, t, accWeight);
					final DefaultWeightedEdge e = new DefaultWeightedEdge();
					g.setEdgeWeight(e, 1.0);
					g.addEdge(type, t, e);
				}
			}
		}
	}

	public void disambiguateTypes(final Table table,
			final List<String> columnGroundTruth) {
		this.gt = columnGroundTruth;
		final int nrC = table.getNumberofColumns();

		final Set<TableColumn> s = new LinkedHashSet<TableColumn>();
		for (int i = 0; i < nrC; i++) {
			final TableColumn c = table.getColumn(i);

			// Generate Type tree
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g = this
					.generateTypeGraph(c.getCellList());
			final FloydWarshallShortestPaths<Type, DefaultWeightedEdge> floydWarshall = new FloydWarshallShortestPaths<Type, DefaultWeightedEdge>(
					g);

			// Initialize features
			this.features[0] = new NumberOfEntitiesFeature(g);
			// this.features[0] = new IncreaseOfEntitiesFeature(g);
			this.features[1] = new InverseDocumentFrequencyFeature(g);
			// this.features[2] = new LayerVarianceFeature(g);

			this.invDocFeature = new InverseDocumentFrequencyFeature(g);

			// Rank CandidateList
			RankedType[] resultSet = null;
			final Set<Type> typeSet = this.filterRelevantTypes(g.vertexSet(),
					g, c.getHeader());
			if (typeSet.size() == 0) {
				resultSet = this.rankTypes(g.vertexSet(), this.numberBestOf, i);
			} else {
				resultSet = this.rankTypes(typeSet, this.numberBestOf, i);
				// resultSet = new RankedType[0];
			}

			for (final RankedType element : resultSet) {
				if (element != null) {
					c.addPossibleType(element.getType());
				}
			}
			this.qryId++;
			s.add(c);
		}

//		HillClimbingColumnDisambiguation dis = new HillClimbingColumnDisambiguation(
//				s);
//		Problem p = new Problem(dis,
//				TypeRankHillClimbingFactory.getActionsFunction(),
//				TypeRankHillClimbingFactory.getResultFunction(),
//				new TypeRankHillClimbingGoalTest());
//		Search search = new HillClimbingSearch(
//				new TypeRankHillClimbingHeuristicFunction());
//		try {
//			SearchAgent agent = new SearchAgent(p, search);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	private Set<Type> filterRelevantTypes(final Set<Type> types,
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g,
			final String header) {
		final Set<Type> relevantTypes = new HashSet<Type>();
		for (final Type type : types) {
			if ((type.getAccumulatedWeight() > 1.0)
					&& this.checkColumnHeader(type, g, header)
					&& this.checkYagoCategory(type, g)) {
				relevantTypes.add(type);
			}
		}
		return relevantTypes;
	}

	private double findAccumulatedWeight(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g,
			final Type t) {
		final Set<Type> typeSet = g.vertexSet();
		for (final Type type : typeSet) {
			if (type.hashCode() == t.hashCode()) {
				return type.getAccumulatedWeight();
			}
		}
		return 0;
	}

	private void findAccumulatedWeightAndSet(
			final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g,
			final Type t, final double val) {
		final Set<Type> typeSet = g.vertexSet();
		for (final Type type : typeSet) {
			if (type.hashCode() == t.hashCode()) {
				type.setAccumulatedWeight(type.getAccumulatedWeight() + val);
			}
		}
	}

	private DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> generateTypeGraph(
			final List<TableCell> cellList) {
		final List<Type> lastLayer = new LinkedList<Type>();
		final DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g = new DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		final Set<Type> tSet = new HashSet<Type>();
		// Add all entity instances
		for (int i = 0; i < cellList.size(); i++) {
			final TableCell c = cellList.get(i);
			final Type t = new Type(c.getDisambigutedContentString(),
					c.getDisambiguatedContent(), false, 0);
			t.setAccumulatedWeight(0.0);
			if (!tSet.contains(t)) {
				g.addVertex(t);
				tSet.add(t);
			}

			final Set<Type> types = RDFGraphOperations
					.getDbpediaCategoriesFromEntity(c.getDisambiguatedContent());
			for (final Type type : types) {
				if (this.typeFilter(type)) {
					if (g.containsVertex(type)) {
						this.findAccumulatedWeightAndSet(g, type, 1.0);
						final DefaultWeightedEdge e = new DefaultWeightedEdge();
						g.setEdgeWeight(e, 1.0);
						g.addEdge(t, type, e);
					} else {
						g.addVertex(type);
						type.setAccumulatedWeight(1.0);
						final DefaultWeightedEdge e = new DefaultWeightedEdge();
						g.setEdgeWeight(e, 1.0);
						g.addEdge(t, type, e);
						lastLayer.add(type);
					}
				}
			}
		}

		final List<Type> lastCreatedTypes = this.createSubtypeVersion0(g,
				lastLayer);
		// lastCreatedTypes = createSubtypeVersion1(g, lastCreatedTypes);
		// createSubtypeVersion2(g, lastCreatedTypes);
		// // Step deeper into the graph
		// int depth = 1;
		// while (depth < GRAPHDEPTH) {
		// HashSet<Type> hashset = new HashSet<Type>();
		// for (int i = 0; i < lastLayer.size(); i++) {
		// Type type = lastLayer.get(i);
		// List<Type> sTypes = RDFGraphOperations
		// .getYagoSuperTypesFromType(type);
		// List<Type> newTypes = new LinkedList<Type>();
		// for (int j = 0; j < sTypes.size(); j++) {
		// Type t = sTypes.get(j);
		// if (g.containsVertex(t)) {
		// double accWeight = findAccumulatedWeight(g, type);
		// findAccumulatedWeightAndSet(g, t, accWeight);
		// DefaultWeightedEdge e = new DefaultWeightedEdge();
		// g.setEdgeWeight(e, 1.0);
		// g.addEdge(type, t, e);
		// // We still have to find the ascending types and adapt
		// // them
		// correctFollowingEdges(g, t, accWeight);
		// } else {
		// g.addVertex(t);
		// type.setLayer(depth + 1);
		// findAccumulatedWeightAndSet(g, t,
		// findAccumulatedWeight(g, type));
		// DefaultWeightedEdge e = new DefaultWeightedEdge();
		// g.setEdgeWeight(e, 1.0);
		// g.addEdge(type, t, e);
		// newTypes.add(t);
		// }
		// }
		// hashset.addAll(newTypes);
		// // List<Type> cLayer = new LinkedList<Type>();
		// // cLayer.addAll(sTypes);
		// }
		// lastLayer.clear();
		// lastLayer.addAll(hashset);
		// depth++;
		// }

		return g;
	}

	// private void markRelevantTypes(
	// DefaultDirectedWeightedGraph<Type, DefaultWeightedEdge> g,
	// String header) {
	// if (!header.equalsIgnoreCase("")) {
	// boolean hasCandidate = false;
	// Set<Type> set = g.vertexSet();
	// for (Iterator<Type> iterator = set.iterator(); iterator.hasNext();) {
	// Type type = (Type) iterator.next();
	// if (!checkTransitiveTypes(g, type, header, 0)) {
	// type.setRelevant(false);
	// } else {
	// hasCandidate = true;
	// }
	// }
	// if (!hasCandidate) {
	// for (Iterator<Type> iterator = set.iterator(); iterator
	// .hasNext();) {
	// Type type = (Type) iterator.next();
	// type.setRelevant(true);
	// }
	// }
	// }
	// }

	private boolean matchHeaderString(final Type type, final String header) {
		final Set<String> hash = new HashSet<String>();
		final String[] split = header.split(" ");
		for (final String element : split) {
			final String[] specialCharsplit = element.split("\\\\|\\(|\\)|/");
			// Remove plural s
			if (specialCharsplit.length > 0) {
				if (specialCharsplit[0].endsWith("s")) {
					specialCharsplit[0] = specialCharsplit[0].substring(0,
							specialCharsplit[0].length() - 1);
				}
				hash.add(specialCharsplit[0].toLowerCase());
			}
		}
		final File file = new File(Properties.getInstance()
				.getTypeLuceneIndex());
		Directory dir;
		try {
			dir = FSDirectory.open(file);
			final IndexReader iReader = DirectoryReader.open(dir);
			final IndexSearcher iSearcher = new IndexSearcher(iReader);
			for (final String string : hash) {
				BooleanQuery bq = new BooleanQuery();
				bq.add(new TermQuery(new Term("Type", type.getUri()
						.toLowerCase())), Occur.MUST);
				FuzzyQuery fq = new FuzzyQuery(new Term("PrefLabel", string));
				bq.add(fq, Occur.MUST);
				TopDocs top = iSearcher.search(bq, 1);
				ScoreDoc[] score = top.scoreDocs;
				if (score.length > 0) {
					return true;
				} else {
					bq = new BooleanQuery();
					bq.add(new TermQuery(new Term("Type", type.getUri()
							.toLowerCase())), Occur.MUST);
					fq = new FuzzyQuery(new Term("Synonyms", string));
					bq.add(fq, Occur.MUST);
					top = iSearcher.search(bq, 1);
					score = top.scoreDocs;
					if (score.length > 0) {
						return true;
					}
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Compute linear combination of various features. Those features are
	 * weighted by Sofia-Ml Learn To Rank Machine Learning software
	 * 
	 * @param type
	 *            Type to rank
	 * @return final scoreValue
	 */
	private double[] rankType(final Type type, final int column) {
		try {
			final String gt = this.gt.get(column);
			if (!gt.equalsIgnoreCase("")) {
				final String[] splitter = gt.split(" ");
				for (final String element : splitter) {
					String typeUri = type.getUri();
					if (typeUri.contains("dbpedia")) {
						final String[] splitter1 = typeUri.split(":");
						typeUri = "http://yago-knowledge.org/resource/wikicategory_"
								+ splitter1[2];
					}
					if (element.equalsIgnoreCase(typeUri)) {
						type.setCorrect();
					}
				}
			}
		} catch (final IndexOutOfBoundsException e) {
		}

		final double[] featureVals = new double[this.features.length];
		for (int i = 0; i < this.features.length; i++) {
			featureVals[i] = this.features[i].computeFeature(type);
		}
		return featureVals;
	}

	private RankedType[] rankTypes(final Set<Type> typesToRank,
			final int bestOf, final int column) {

		final RankedType[] result = new RankedType[bestOf];
		final List<RankedType> l = new LinkedList<ColumnDisAlgorithm.RankedType>();
		for (final Type type : typesToRank) {
			if (type.isType() && type.isRelevant()) {
				final double[] featureVals = this.rankType(type, column);
				final RankedType rt = new RankedType(type, featureVals);
				l.add(rt);
			}
		}
		Collections.sort(l);
		for (final RankedType rankedType : l) {
			if ((this.gt != null) && (this.gt.size() != 0)) {
				if (!this.gt.get(column).equalsIgnoreCase("")) {
					final LearntoRankOutputObject obj = new LearntoRankOutputObject(
							rankedType.getType().isCorrect(), this.qryId,
							rankedType.getFeatureVals());
					this.ltroutput.writeQueryResult(obj);
				}
			}
		}

		for (int i = 0; i < bestOf; i++) {
			if (l.size() > i) {
				result[i] = l.get(i);
			}
		}
		return result;
	}

	private boolean typeFilter(final Type type) {
		final String uri = type.getUri();
		if (uri.contains("Living_people")
				|| uri.contains("English-language_films")) {
			return false;
		}
		final Pattern p = Pattern.compile("[+-]?[0-9]+");
		final Matcher m = p.matcher(uri);
		if (m.find()) {
			return false;
		}
		return true;
	}
}
