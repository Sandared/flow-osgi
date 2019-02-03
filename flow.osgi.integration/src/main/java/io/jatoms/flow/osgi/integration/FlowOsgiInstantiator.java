package io.jatoms.flow.osgi.integration;

import java.util.Collection;

import org.osgi.annotation.bundle.Capability;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.di.DefaultInstantiator;
import com.vaadin.flow.router.NavigationEvent;
import com.vaadin.flow.server.VaadinService;

// Instantiators can be set via ServiceLoader mechanisms... so if we register this Instantiator somehow as an eligible 
// Instantiator Service for Flow, then everything might work magically?
// 
// For more infos on ServiceLoaders in OSGi have a look at https://blog.osgi.org/2013/02/javautilserviceloader-in-osgi.html and the corresponding specification
//
// As I get it, the Flow bundle that uses ServiceLoader to load the Instantiator needs to define something like this in its Manifest:
// In the META-INF/MANIFEST.MF, the consumer bundle has:
//	  Require-Capability: osgi.extender;
//	      filter:="(osgi.extender=osgi.serviceloader.processor)",
//	    osgi.serviceloader;
//	      filter:="(osgi.serviceloader=com.vaadin.flow.di.Instantiator)";
//	      cardinality:=multiple
//
// Whereas we need to define the Capability and Requirement as below:
@Capability(namespace="osgi.serviceloader", attribute= {"osgi.serviceloader=com.vaadin.flow.di.Instantiator"})
@Requirement(namespace="osgi.extender", filter="(osgi.extender=osgi.serviceloader.registrar)")
public class FlowOsgiInstantiator extends DefaultInstantiator {

	public FlowOsgiInstantiator(VaadinService service) {
		super(service);
	}
	
	@Override
	public <T extends HasElement> T createRouteTarget(Class<T> routeTargetType, NavigationEvent event) {
		// Get the bundle context of the given class
		BundleContext context = FrameworkUtil.getBundle(routeTargetType).getBundleContext();
		
		// filter string used to get the ComponentFactory for this specific class
		// This assumes, that the @Route and @Component annotated class followed our recommendation to 
		// add the attribute "factory=<full qualified class name>"
		// Otherwise we will not find any ComponentFactory
		String filter = "(" + ComponentConstants.COMPONENT_FACTORY + "=" + routeTargetType.getName() + ")";
		
		try {
			// find all service references to the ComponentFactory of this class. 
			// If there is more than one, then something went wrong
			Collection<ServiceReference<ComponentFactory>> componentFactories = context.getServiceReferences(ComponentFactory.class, filter);
			
			if(componentFactories.size() != 1) {
				// there is something wrong with the factory name given for this class, e.g., typo?
				System.err.println("Factory name of " + routeTargetType.getSimpleName() + " does not match it full qualified class name");
			}
			
			// Now create a new component with no additional properties
			// This is not really typesafe, but we can assume that this class is extending HasElement
			// as this is checked in the FlowOsgiTracker
			ComponentFactory<T> factory = context.getService(componentFactories.iterator().next());
			ComponentInstance<T> componentInstance = factory.newInstance(null);
			
			// Add a DetachListener to the respective Element, so that this component cleans itself up 
			// after it has been detached
			T component = componentInstance.getInstance();
			component.getElement().addDetachListener(detachEvent -> componentInstance.dispose());
			
			return component;
			
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException("Filter to search for ComponentFactory for " + routeTargetType.getSimpleName() + " was invalid! Filter was " + filter);
		}
		
	}

}
