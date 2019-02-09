package io.jatoms.flow.osgi.integration;

import java.lang.annotation.Annotation;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.vaadin.flow.router.RouteAlias;

import static io.jatoms.flow.osgi.integration.FlowOsgiConstants.*;

public abstract class FlowOsgiTypeTracker<T> extends BundleTracker<T> {
	private Bundle trackerBundle;
	private Map<String, List<Class<?>>> namespaceToClasses;

	public FlowOsgiTypeTracker(BundleContext context, Class<?>  ... handledTypes) {
		super(context, Bundle.ACTIVE, null);
		trackerBundle = context.getBundle();
		namespaceToClasses = getNamespaceToClassMapping(handledTypes);
	}
	
	private Map<String, List<Class<?>>> getNamespaceToClassMapping (Class<?>  ... handledTypes){
		Map<String, List<Class<?>>> namespaceToClasses = new HashMap<String, List<Class<?>>>();
		for (Class<?> clazz : handledTypes) {
			String namespace = clazz.getPackage().getName();
			List<Class<?>> clazzes = namespaceToClasses.get(namespace);
			
			if(clazzes == null) {
				clazzes = new ArrayList<Class<?>>();
				namespaceToClasses.put(namespace, clazzes);
			}
			
			clazzes.add(clazz);
		}
		
		return namespaceToClasses;
	}
	

	@Override
	public T addingBundle(Bundle bundle, BundleEvent event) {
		// not interested in ourselves
		if(bundle == trackerBundle)
			return getTrackedDefault();
		
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
				
				List<Class<?>> typesToHandle = checkForHandledNamespace(filter);
				
				// if this requirement points to a package we track,
				// then this bundle is scanned for the handled types
				if (!typesToHandle.isEmpty()) {
					
					List<String> classNames = getBundleClasses(bundle);
					
					// scan for types we handle
					List<Class<?>> handledClasses = getHandledClasses(bundle, classNames, typesToHandle);
					
					if(!handledClasses.isEmpty()) {
						// delegate to concrete type to decide what to do with the found classes
						addHandledClasses(bundle, handledClasses);
					}
				}
			}
		}
		return getTrackedDefault();
	}

	// checks if a given filter contains one fo the namespaces that we track
	private List<Class<?>> checkForHandledNamespace(String filter) {
		List<Class<?>> classesToHandle = new ArrayList<Class<?>>();
		for (String namespace : namespaceToClasses.keySet()) {
			if(filter.contains(namespace)) {
				classesToHandle.addAll(namespaceToClasses.get(namespace));
			}
		}
		return classesToHandle;
	}
	
	// gets all classes within a specific bundle and its fragment bundles
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

	// used to get all classes within a bundle that either implement/extend or are annotated with typesToHandle
	private List<Class<?>> getHandledClasses(Bundle bundle, List<String> classNames, List<Class<?>> typesToHandle) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (String className :classNames) {
			try {
				Class<?> clazz = bundle.loadClass(className);
				
				for (Class<?> toHandle : typesToHandle) {
					if(toHandle.isAnnotation()) {
						if (clazz.isAnnotationPresent((Class<? extends Annotation>) toHandle)) {
							classes.add(clazz);
						}
					}
					// It's either an interface or a class
					else {
						if(toHandle.isAssignableFrom(clazz)) {
							classes.add(clazz);
						}
					}
				}
				
			} catch (ClassNotFoundException | NoClassDefFoundError exception) {
				exception.printStackTrace();
			}
		}
		return classes;
	}
	
	
	protected abstract T addHandledClasses(Bundle bundle, List<Class<?>> handledClasses);
	protected abstract T getTrackedDefault();
}
