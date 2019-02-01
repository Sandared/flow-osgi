package io.jatoms.flow.osgi.integration;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import com.vaadin.flow.server.startup.RouteRegistry;
import static io.jatoms.flow.osgi.integration.FlowOsgiConstants.*;

@Component(service = RouteRegistry.class)
public class FlowOsgiRouteRegistry extends RouteRegistry {
	
	@Reference(target="(" + Annotation + "=" + Route + ")", cardinality=ReferenceCardinality.MULTIPLE)
	void addRoute(Class route) {
		String test = "";
		// TODO: add route to navigation targets, maybe switch to vaadin 13 or snapshot version as 12 does not support dynamic routes?
	}
	
	void removeRoute(Class route) {
		String test = "";
	}
	
	@Reference(target="(" + Annotation + "=" + RouteAlias + ")", cardinality=ReferenceCardinality.MULTIPLE)
	void addRouteAlias(Class route) {
		String test = "";
		// TODO: add route to navigation targets, maybe switch to vaadin 13 or snapshot version as 12 does not support dynamic routes?
	}
	
	void removeRouteAlias(Class route) {
		String test = "";
	}

}
