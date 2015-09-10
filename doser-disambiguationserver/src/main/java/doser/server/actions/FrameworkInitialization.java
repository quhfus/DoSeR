package doser.server.actions;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import doser.entitydisambiguation.backend.DisambiguationMainService;

public class FrameworkInitialization extends ContextLoader implements
		ServletContextListener {

	private ContextLoader contextLoader;

	public FrameworkInitialization() {
	}

	public FrameworkInitialization(WebApplicationContext context) {
		super(context);
	}

	/**
	 * Initialize the root web application context.
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		DisambiguationMainService.initialize();
		this.contextLoader = createContextLoader();
		if (this.contextLoader == null) {
			this.contextLoader = this;
		}
		this.contextLoader.initWebApplicationContext(event.getServletContext());
	}

	/**
	 * Create the ContextLoader to use. Can be overridden in subclasses.
	 * 
	 * @return the new ContextLoader
	 * @deprecated in favor of simply subclassing ContextLoaderListener itself
	 *             (which extends ContextLoader, as of Spring 3.0)
	 */
	@Deprecated
	protected ContextLoader createContextLoader() {
		return null;
	}

	/**
	 * Return the ContextLoader used by this listener.
	 * 
	 * @return the current ContextLoader
	 * @deprecated in favor of simply subclassing ContextLoaderListener itself
	 *             (which extends ContextLoader, as of Spring 3.0)
	 */
	@Deprecated
	public ContextLoader getContextLoader() {
		return this.contextLoader;
	}

	/**
	 * Close the root web application context.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		DisambiguationMainService.getInstance().shutDownDisambiguationService();
		if (this.contextLoader != null) {
			this.contextLoader.closeWebApplicationContext(event
					.getServletContext());
		}
		ServletContext sc = event.getServletContext();
		Enumeration<String> attrNames = sc.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String attrName = attrNames.nextElement();
			if (attrName.startsWith("org.springframework.")) {
				Object attrValue = sc.getAttribute(attrName);
				if (attrValue instanceof DisposableBean) {
					try {
						((DisposableBean) attrValue).destroy();
					} catch (Throwable ex) {
						Logger.getRootLogger().fatal(ex.getMessage());
					}
				}
			}
		}
	}

}
