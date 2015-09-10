package lda.properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public final class LDAProperties {
	private static LDAProperties instance;
	private static final String RESOURCE_NAME = "lda.properties";

	public synchronized static LDAProperties getInstance() {
		if (instance == null) {
			instance = new LDAProperties();
		}

		return instance;
	}

	/**
	 * Provides easy access to property files (e.g. config.getInt())
	 */
	PropertiesConfiguration config;

	private LDAProperties() {
		try {
			this.config = new PropertiesConfiguration(RESOURCE_NAME);
		} catch (final ConfigurationException e) {
			Logger.getRootLogger().error(
					"Failed to load properties file: " + RESOURCE_NAME, e);
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

	public String getLDAClientApp() {
		return this.config.getString("LDAClientApp");
	}

	public String getLDACThreadWorkingDir() {
		return this.config.getString("LDAThreadWorkingDir");
	}

	public int getNRLDAThreads() {
		return Integer.parseInt(this.config.getString("LDANrThreads"));
	}

}