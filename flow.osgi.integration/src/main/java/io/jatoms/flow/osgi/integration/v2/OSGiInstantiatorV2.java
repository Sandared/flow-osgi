package io.jatoms.flow.osgi.integration.v2;

import java.util.Collection;

import org.osgi.annotation.bundle.Capability;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentConstants;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.di.DefaultInstantiator;
import com.vaadin.flow.router.NavigationEvent;
import com.vaadin.flow.server.VaadinService;

@Capability(namespace="osgi.serviceloader", attribute= {"osgi.serviceloader=com.vaadin.flow.di.Instantiator"})
@Requirement(namespace="osgi.extender", filter="(osgi.extender=osgi.serviceloader.registrar)")
public class OSGiInstantiatorV2 extends DefaultInstantiator{

	public OSGiInstantiatorV2(VaadinService service) {
		super(service);
	}
	
	@Override
	public <T extends HasElement> T createRouteTarget(Class<T> routeTargetType, NavigationEvent event) {
		// Get the bundle context of the given class
		BundleContext context = FrameworkUtil.getBundle(routeTargetType).getBundleContext();
		
		try {
			// We filter all possible references for the implementation class name.
			String filter = "(" + ComponentConstants.COMPONENT_NAME + "=" + routeTargetType.getName() + ")";
			Collection<ServiceReference<Component>> refs = context.getServiceReferences(Component.class, filter);
			
			if(refs != null && refs.size() == 1) {
				ServiceObjects<Component> so = context.getServiceObjects(refs.iterator().next());
				
				ServiceReference<Component> ref = so.getServiceReference();
				
			} else {
				System.err.println("There should be exactly one service reference for this implementation class");
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException("Filter to search for ComponentFactory for " + routeTargetType.getSimpleName() + " was invalid! Filter was " + filter);
		}
	}
}
