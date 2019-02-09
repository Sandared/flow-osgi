package io.jatoms.flow.osgi.integration;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import com.vaadin.flow.server.RouteRegistry;


// TODO: if registered this way this SCL SHOULD work and use the default ServletContext
// Somehow this does not work and the SCL is never called by the HTTP Whiteboard Service. 
// I've already asked on the OSGi mailing list, but not received an answer yet 

// UPDATE: This seems to be a bug in felix which should soon be resolved
// Affects Version/s: http.jetty-4.0.6, http.base-4.0.4, http.bridge-4.0.4
// Fix Version/s: http.base-4.0.6, http.jetty-4.0.8, http.bridge-4.0.6
// https://issues.apache.org/jira/browse/FELIX-6049
// As a workaround for now: don't use @HttpWhiteboardListener but
// @Component(property= {HttpWhiteboardConstants.HTTP_WHITEBOARD_LISTENER + "=true"})

@Component(property= {HttpWhiteboardConstants.HTTP_WHITEBOARD_LISTENER + "=true"})
@ServiceRanking(Integer.MAX_VALUE) // makes sure this is called before ServletDeployer and others
public class FlowOsgiRouteRegistryInitializer implements ServletContextListener {
	
	@Reference
	RouteRegistry routeRegistry;
	
	private CopyOnWriteArrayList<ServletContext> contexts = new CopyOnWriteArrayList<ServletContext>();
	
	@Deactivate
	void deactivate() {
		contexts.stream()
			.forEach(context -> context.removeAttribute(RouteRegistry.class.getName()));
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		context.setAttribute(RouteRegistry.class.getName(), routeRegistry);
		contexts.add(context);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		contexts.remove(sce.getServletContext());
	}
}
