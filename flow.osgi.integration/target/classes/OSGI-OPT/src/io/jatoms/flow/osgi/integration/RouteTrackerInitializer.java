package io.jatoms.flow.osgi.integration;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component
public class RouteTrackerInitializer{
	
	private RouteTracker routeTracker;
	
	@Activate
	void activate (BundleContext context) {
		routeTracker = new RouteTracker(context);
		routeTracker.open();
	}
	
	@Deactivate
	void deactivate () {
		routeTracker.close();
	}
}
