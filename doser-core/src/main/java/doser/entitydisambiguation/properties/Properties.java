package doser.entitydisambiguation.properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public final class Properties {
	private static Properties instance;
	private static final String RESOURCE_NAME = "disambiguation.properties";
	
	public synchronized static Properties getInstance() {
		if (instance == null) {
			instance = new Properties();
		}

		return instance;
	}

	/**
	 * Provides easy access to property files (e.g. config.getInt())
	 */
	PropertiesConfiguration config;

	private Properties() {
		try {
			this.config = new PropertiesConfiguration(RESOURCE_NAME);
		} catch (final ConfigurationException e) {
			Logger.getRootLogger().error("Failed to load properties file: "	+ RESOURCE_NAME, e);
		}
	}

	/**
	 * ArtifactId of the application (from maven pom.xml)
	 * 
	 * @return artifact id
	 */
	public String getApplicationArtifactId() {
		return this.config.getString("application.artifactId");
	}

	/**
	 * Name of the application (from maven pom.xml)
	 * 
	 * @return application name
	 */
	public String getApplicationName() {
		return this.config.getString("application.name");
	}

	/**
	 * Version of the application (from maven pom.xml)
	 * 
	 * @return application version
	 */
	public String getApplicationVersion() {
		return this.config.getString("application.version");
	}

	public String getCategorySuggestionIndex() {
		return this.config.getString("categorySuggestionIndex");
	}

	public String getDBPediaArticleCategories() {
		return this.config.getString("dbpedia.articlescategorie");
	}

	public String getDBPediaCategoryLabels() {
		return this.config.getString("dbpedia.categorylabels");
	}

	public String getDBPediaDescriptions() {
		return this.config.getString("dbpedia.descriptions");
	}

	public String getDBPediaIndex() {
		return this.config.getString("stanbol.dbPediaSolr");
	}

	public String getDBPediaLabels() {
		return this.config.getString("dbpedia.labels");
	}
	
	public String getDBPediaFacts() {
		return this.config.getString("dbpedia.facts");
	}

	public String getDBpediaSkosCategories() {
		return this.config.getString("dbpedia.skos_categories");
	}
	
	public String getDBpediaInstanceTypes() {
		return this.config.getString("dbpedia.mappingbasedtypes");
	}
	
	public String getDBpediaRedirects() {
		return this.config.getString("dbpedia.redirects");
	}
	
	public int getDisambiguationResultSize() {
		final String size = this.config.getString("disambiguation.returnSize");
		return Integer.valueOf(size);
	}

	/**
	 * Get location of entity-centric knowledge base
	 */

	public String getEntityCentricKBWikipedia() {
		return this.config.getString("disambiguation.entityCentricKBWikipedia");
	}
	
	/**
	 * Get location of CSTableIndex
	 */
	public String getCSTableIndex() {
		return this.config.getString("disambiguation.tableIndexCSDomain");
	}

	public String getLearnToRankOutputService() {
		return this.config.getString("disambiguation.LTROutputService");
	}
	
	public String getWord2VecService() {
		return this.config.getString("disambiguation.Word2VecService");
	}


	public String getTypeLuceneIndex() {
		return this.config.getString("yago.typeIndex");
	}

	public String getYagoCategoryLabels() {
		return this.config.getString("yago.categoryLabels");
	}

	public String getYagoTaxonomy() {
		return this.config.getString("yago.taxonomy");
	}

	public String getYagoTransitiveTypes() {
		return this.config.getString("yago.transitiveTypes");
	}
	
	public String getDbPediaBiomedCopyKB() {
		return this.config.getString("disambiguation.dbpediabiomedcopy");
	}
	
	public String getDocumentCentricKB() {
		return this.config.getString("disambiguation.documentcentric");
	}
	
	public String getWord2VecModel() {
		return this.config.getString("word2vecmodel");
	}
	
	public String getDBpediaSpotLight_En_Rest() {
		return this.config.getString("dbpediaSpotlight_eng");
	}
	
	public String getDBpediaSpotLight_Ger_Rest() {
		return this.config.getString("dbpediaSpotlight_ger");
	}
	
	public String getNounPhraseModel() {
		return this.config.getString("nounphrasemodel");
	}
	
	public boolean getCandidateExpansion() {
		boolean bool = false;
		String s = this.config.getString("candidateExpansion");
		if(s.equalsIgnoreCase("true")) {
			bool = true;
		}
		return bool;
	}
	
	public boolean getHBaseStorage() {
		boolean bool = false;
		String s = this.config.getString("writeHBase");
		if(s.equalsIgnoreCase("true")) {
			bool = true;
		}
		return bool;
	}
}
