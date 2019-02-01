package io.jatoms.flow.osgi.integration;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component
public class FlowOsgiTrackerInitializer{
	
	private FlowOsgiTracker tracker;
	
	@Activate
	void activate (BundleContext context) {
		tracker = new FlowOsgiTracker(context);
		tracker.open();
	}
	
	@Deactivate
	void deactivate () {
		tracker.close();
	}
}
