//package io.jatoms.flow.osgi.integration;
//
//import java.util.List;
//
//import org.osgi.framework.Bundle;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.ServiceObjects;
//import org.osgi.framework.ServiceRegistration;
//
//import com.vaadin.flow.component.Component;
////import com.vaadin.flow.component.WebComponent;
//
//// TODO: add Capability to tell osgi what services this tracker adds to the runtime, if any
//public class FlowOsgiWebComponentTracker extends FlowOsgiTypeTracker<List<ServiceRegistration<Class>>> {
//
//	public FlowOsgiWebComponentTracker(BundleContext context) {
//		super(context, WebComponent.class);
//		ServiceObjects<Component> so = context.getServiceObjects(context.getServiceReference(Component.class));
//	}
//
//	@Override
//	protected List<ServiceRegistration<Class>> addHandledClasses(Bundle bundle, List<Class<?>> handledClasses) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected List<ServiceRegistration<Class>> getTrackedDefault() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
