package io.jatoms.flow.osgi.integration.v2;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import com.vaadin.flow.router.Route;

@Component(property= {HttpWhiteboardConstants.HTTP_WHITEBOARD_LISTENER + "=true"})
public class RouteComponentWhiteboard{
	
	@Reference(cardinality=ReferenceCardinality.MULTIPLE, policy=ReferencePolicy.DYNAMIC, policyOption=ReferencePolicyOption.GREEDY)
	void addInstance(com.vaadin.flow.component.Component route) {
		Class<? extends com.vaadin.flow.component.Component> routeClazz = route.getClass();
		
		if(routeClazz.isAnnotationPresent(Route.class)) {
			// add route to RouteRegistry
		}
	}
}
