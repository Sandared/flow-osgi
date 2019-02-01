package io.jatoms.flow.osgi.integration;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.vaadin.flow.server.startup.RouteRegistry;

@Component
public class OSGiRouteRegistry extends RouteRegistry {
	
	@Reference(target="(" + FlowOsgi.Annotation + "=" + FlowOsgi.Route + ")")
	void addRoute(Class route) {
		// TODO: add route to navigation targets, mybe switch to vaadin 13 or snapshot version as 12 does not support dynamic routes?
	}
	
	void removeRoute(Class route) {
		
	}

}
