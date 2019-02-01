package io.jatoms.flow.osgi.integration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardListener;

import com.vaadin.flow.server.startup.RouteRegistry;

@Component(immediate=true)
@HttpWhiteboardListener
public class FlowOsgiServletContextListener implements ServletContextListener {
	
	@Reference
	RouteRegistry routeRegistry;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		sce.getServletContext().setAttribute(RouteRegistry.class.getName(), routeRegistry);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}
}
