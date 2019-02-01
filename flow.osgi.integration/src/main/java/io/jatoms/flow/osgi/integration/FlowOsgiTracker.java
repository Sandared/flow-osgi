package io.jatoms.flow.osgi.integration;

import java.lang.annotation.Annotation;

import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.osgi.annotation.bundle.Capability;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.BundleTracker;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.Route;

// This tracker adds services under the Class interface
@Capability(namespace="osgi.service", attribute= {"objectClass:List<String>=\"java.lang.Class\""})
public class FlowOsgiTracker extends BundleTracker<List<ServiceRegistration<Class>>> {
	private Bundle trackerBundle;

	public FlowOsgiTracker(BundleContext context) {
		super(context, Bundle.ACTIVE, null);
		trackerBundle = context.getBundle();
	}

	@Override
	public List<ServiceRegistration<Class>> addingBundle(Bundle bundle, BundleEvent event) {
//		if(bundle == trackerBundle)
//			return null;
		// maybe skip vaadin bundles hardcoded?

		BundleWiring wiring = bundle.adapt(BundleWiring.class);

		// should get us all wires that are created from import/export package headers
		List<BundleWire> wires = wiring.getRequiredWires(PackageNamespace.PACKAGE_NAMESPACE);

		List<ServiceRegistration<Class>> registrations = new ArrayList<ServiceRegistration<Class>>();
		for (BundleWire wire : wires) {
			
			// we are only interested in bundles that use a specific annotation,
			// so we only use the requirements
			BundleRequirement requirement = wire.getRequirement();
			String filter = requirement.getDirectives().get("filter");
			
			if (filter != null) {
				List<String> classNames = getBundleClasses(bundle);
				
				// if this requirement points to the package where @Routes annotations are,
				// then this bundle is scanned for @Route annotated classes
				if (filter.contains("com.vaadin.flow.router")) {
					
					// scan for @Route annotated classes
					List<Class<?>> routes = scanClassesForAnnotation(bundle, classNames, Route.class, Component.class);
					
					// if there are any classes with an annotation
					if (!routes.isEmpty()) {
						for (Class<?> route : routes) {
							
							// create additional props to make this service better targetable by other @Components
							Hashtable<String, String> props = new Hashtable<String, String>();
							props.put(FlowOsgiConstants.Annotation, FlowOsgiConstants.Route);
							
							// register the found class as service so other @Components can find them
							ServiceRegistration<Class> registration = bundle.getBundleContext()
									.registerService(Class.class, route, props);
							registrations.add(registration);
						}
					}
				}
			}
		}
		return registrations;
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
	public void modifiedBundle(Bundle bundle, BundleEvent event, List<ServiceRegistration<Class>> registrations) {
		if (!registrations.isEmpty()) {
			// TODO?
		}
	}

	private List<String> getBundleClasses(Bundle bundle) {
		BundleWiring wiring = bundle.adapt(BundleWiring.class);

		// get all resources in the specific bundle and in its fragment bundles
		List<URL> resources = wiring.findEntries("/", "*.class", BundleWiring.FINDENTRIES_RECURSE);
		List<String> classNames = new ArrayList<String>();
		for (URL resource : resources) {
			String className = resource.getPath()
					// remove leading slash
					.substring(1, resource.getPath().length())
					// replace trailing .class
					.replace(".class", "")
					// replace all slashes
					.replace('/', '.');
			classNames.add(className);
		}
		return classNames;
	}

	private List<Class<?>> scanClassesForAnnotation(Bundle bundle, List<String> classNames, Class<? extends Annotation> annotation, Class<?> type) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (String className :classNames) {
			try {
				Class<?> clazz = bundle.loadClass(className);
				if (clazz.isAnnotationPresent(annotation)) {
					if(type != null) {
						if (Component.class.isAssignableFrom(clazz)) {
							classes.add(clazz);
						} else {
							throw new IllegalArgumentException("Class annotated with " + annotation.getSimpleName() + " is not a " + type.getSimpleName());
						}
					} else {
						classes.add(clazz);
					}
				}
			} catch (ClassNotFoundException | NoClassDefFoundError exception) {
				exception.printStackTrace();
			}
		}
		return classes;
	}
}
