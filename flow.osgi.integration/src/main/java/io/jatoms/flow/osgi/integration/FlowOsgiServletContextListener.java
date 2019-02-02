package io.jatoms.flow.osgi.integration;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardListener;

import com.vaadin.flow.server.startup.RouteRegistry;

@Component(immediate=true)
@HttpWhiteboardListener
public class FlowOsgiServletContextListener implements ServletContextListener {
	
	@Reference
	RouteRegistry routeRegistry;
	
	private CopyOnWriteArrayList<ServletContext> contexts = new CopyOnWriteArrayList<ServletContext>();
	
	@Deactivate
	void deactivate() {
		contexts.stream()
			.forEach(context -> context.removeAttribute(RouteRegistry.class.getName()));
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		context.setAttribute(RouteRegistry.class.getName(), routeRegistry);
		contexts.add(context);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		contexts.remove(sce.getServletContext());
	}
}
