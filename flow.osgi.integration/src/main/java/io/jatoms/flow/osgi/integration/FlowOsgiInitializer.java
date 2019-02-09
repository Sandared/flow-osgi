package io.jatoms.flow.osgi.integration;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletContextListener;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import com.vaadin.flow.server.communication.JSR356WebsocketInitializer;
import com.vaadin.flow.server.startup.ServletDeployer;

// This is the entrypoint for OSGi
@Component
public class FlowOsgiInitializer{
	
	private FlowOsgiRouteTracker tracker;
	private List<ServiceRegistration<ServletContextListener>> vaadinSCLs = new ArrayList<ServiceRegistration<ServletContextListener>>();
	
	@Activate
	void activate (BundleContext context) {
		tracker = new FlowOsgiRouteTracker(context);
		tracker.open();
		registerFlowServletContextListeners(context);
	}
	
	private void registerFlowServletContextListeners(BundleContext context) {
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_LISTENER, true);
		// make sure ServletDeployer is called before JSR356WebsocketInitializer
		props.put(Constants.SERVICE_RANKING, 200);
		vaadinSCLs.add(context.registerService(ServletContextListener.class, new ServletDeployer(), props));

		props.put(Constants.SERVICE_RANKING, 100);
		vaadinSCLs.add(context.registerService(ServletContextListener.class, new JSR356WebsocketInitializer(), props));
	}

	@Deactivate
	void deactivate () {
		vaadinSCLs.stream().forEach(registration -> registration.unregister());
		tracker.close();
	}
}
