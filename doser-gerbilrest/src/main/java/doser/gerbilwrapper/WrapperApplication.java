package doser.gerbilwrapper;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class WrapperApplication extends Application {

	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */
	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/doserwrapper", DoserResource.class);
//		router.attach("/aidawrapper", AidaWrapper.class);
//		router.attach("/illinoiswrapper", IllinoisWrapper.class);
		return router;
	}
}
