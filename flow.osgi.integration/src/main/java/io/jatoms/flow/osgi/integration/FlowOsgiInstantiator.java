package io.jatoms.flow.osgi.integration;

import org.osgi.annotation.bundle.Capability;
import org.osgi.annotation.bundle.Requirement;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.di.DefaultInstantiator;
import com.vaadin.flow.router.NavigationEvent;
import com.vaadin.flow.server.VaadinService;

// TODO: Instantiators can be set via ServiceLoader mechanisms... so if we register this Instantiator somehow as an eligible 
// Instantiator Service for Flow, then everything might work magically?
// 
// will need to have a look at https://blog.osgi.org/2013/02/javautilserviceloader-in-osgi.html and the corresponding specification
//
// UPDATE: As I get it, the Flow bundle that uses ServiceLoader to load the Instantiator needs to define something like this in its Manifest:
//In the META-INF/MANIFEST.MF, the consumer bundle has:
//	  Require-Capability: osgi.extender;
//	      filter:="(osgi.extender=osgi.serviceloader.processor)",
//	    osgi.serviceloader;
//	      filter:="(osgi.serviceloader=com.vaadin.flow.di.Instantiator)";
//	      cardinality:=multiple
//
// Whereas we need to define the Capability and Requirement as below?
@Capability(namespace="osgi.serviceloader", attribute= {"osgi.serviceloader=com.vaadin.flow.di.Instantiator"})
@Requirement(namespace="osgi.extender", filter="(osgi.extender=osgi.serviceloader.registrar)")
public class FlowOsgiInstantiator extends DefaultInstantiator {

	public FlowOsgiInstantiator(VaadinService service) {
		super(service);
	}
	
	@Override
	public <T extends HasElement> T createRouteTarget(Class<T> routeTargetType, NavigationEvent event) {
		// TODO: need Access to OSGiRouteRegistry (via RouteRegistry.getInstance()?) then use the class to create a new ComponentInstance via ConfigAdmin
		// this instance is then an OSGi service/component and is able to take part in service lifecycles
		return null;
	}

}
