package io.jatoms.flow.osgi.simpleui;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteResolver;

@Route("")
// This prevents SCR from instantiating this component which should be done by vaadin osgi intgration via config admin
@Component(factory="io.jatoms.flow.osgi.simpleui.MainView")
public class MainView extends VerticalLayout {
	
    public MainView() {
        Button button = new Button("Click me",
                event -> Notification.show("Clicked!"));
        add(button);
    }
}


