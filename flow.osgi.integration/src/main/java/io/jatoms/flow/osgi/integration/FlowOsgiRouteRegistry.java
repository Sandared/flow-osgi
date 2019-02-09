package io.jatoms.flow.osgi.integration;

import static io.jatoms.flow.osgi.integration.FlowOsgiConstants.Annotation;
import static io.jatoms.flow.osgi.integration.FlowOsgiConstants.Route;
import static io.jatoms.flow.osgi.integration.FlowOsgiConstants.RouteAlias;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;

@org.osgi.service.component.annotations.Component(service = RouteRegistry.class)
public class FlowOsgiRouteRegistry extends ApplicationRouteRegistry {
	
	@Reference(
			target="(" + Annotation + "=" + Route + ")", 
			cardinality=ReferenceCardinality.MULTIPLE, 
			policy=ReferencePolicy.DYNAMIC,
			policyOption=ReferencePolicyOption.GREEDY)
	void addRouteClass(Class<? extends Component> route) {
		// TODO: add route to navigation targets, maybe switch to vaadin 13 or snapshot version as 12 does not support dynamic routes?
	}
	
	void removeRouteClass(Class<? extends Component> route) {
	}
	
	@Reference(target="(" + Annotation + "=" + RouteAlias + ")", 
			cardinality=ReferenceCardinality.MULTIPLE,
			policy=ReferencePolicy.DYNAMIC,
			policyOption=ReferencePolicyOption.GREEDY)
	void addRouteAliasClass(Class<? extends Component> routeAlias) {
		// TODO: add route to navigation targets, maybe switch to vaadin 13 or snapshot version as 12 does not support dynamic routes?
	}
	
	void removeRouteAliasClass(Class<? extends Component> routeAlias) {
	}
	
	// we can add here anything else that needs to be tracked... e.g., ErrorPages ? Just need a tracker that adds the classes

}
