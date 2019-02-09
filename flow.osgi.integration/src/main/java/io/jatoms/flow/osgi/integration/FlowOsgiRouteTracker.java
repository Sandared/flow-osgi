package io.jatoms.flow.osgi.integration;

import static io.jatoms.flow.osgi.integration.FlowOsgiConstants.Annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.osgi.annotation.bundle.Capability;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import static io.jatoms.flow.osgi.integration.FlowOsgiConstants.*;

// tracks bundles that are annotated with @Route/@RouteAlias and registers them as services under the Class type so that a RouteRegistry can find them
// as this tracker registers the found classes under java.langClass we have to tell osgi so that bundle resolutions does not fail
@Capability(namespace="osgi.service", attribute= {"objectClass:List<String>=\"java.lang.Class\""})
public class FlowOsgiRouteTracker extends FlowOsgiTypeTracker<List<ServiceRegistration<Class>>> {

	public FlowOsgiRouteTracker(BundleContext context) {
		super(context, Route.class, RouteAlias.class);
	}

	@Override
	protected List<ServiceRegistration<Class>> addHandledClasses(Bundle bundle, List<Class<?>> routes) {
		List<ServiceRegistration<Class>> registrations = new ArrayList<ServiceRegistration<Class>>();
		for (Class<?> clazz : routes) {
			// make sure its a component
			if (Component.class.isAssignableFrom(clazz)) {
				if (clazz.isAnnotationPresent(Route.class))
					registrations.add(registerService(bundle, clazz, Route));
				else
					registrations.add(registerService(bundle, clazz, RouteAlias));
			}
		}
		return registrations;
	}

	private ServiceRegistration<Class> registerService(Bundle bundle, Class<?> service, String annotationType) {
		// create additional props to make this service better targetable by other
		// @Components
		Hashtable<String, String> props = new Hashtable<String, String>();
		props.put(Annotation, annotationType);

		// register the found class as service so other @Components can find them
		return bundle.getBundleContext().registerService(Class.class, service, props);
	}

	@Override
	public void removedBundle(Bundle bundle, BundleEvent event, List<ServiceRegistration<Class>> registrations) {
		if (!registrations.isEmpty()) {
			for (ServiceRegistration<Class> registration : registrations) {
				registration.unregister();
			}
		}
	}
	
	@Override
	public void close() {
		// unregister all classes that this tracker added. Otherwise there will be multiple registrations per class when this tracker is restarted again.
		getTracked().entrySet().stream()
			.forEach(entry -> entry.getValue().stream()
					.forEach(registration -> registration.unregister()));
		super.close();
	}

	@Override
	protected List<ServiceRegistration<Class>> getTrackedDefault() {
		return Collections.emptyList();
	}

}
