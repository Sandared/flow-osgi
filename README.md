# FLOW OSGi Integration

## WHAT
Trying to enhance current Flow OSGi integration, so that `@Route`/`@RouteAlias` annotated classes can be components (`@Component` annotated classes) too in an OSGi container

## WHY?
Currently the efforts of Vaadin to make Flow (Vaadin 10+) OSGi compatible center around being able to run an existing Flow Application in an OSGi container. This is a great step forward, but might not be sufficient for people who do not want to port an existing Vaadin application into an OSGi container, but develop a Vaadin application as an OSGi  enabled application, e.g., want to make use of OSGi's dependency injection mechanisms, i.e., Declarative Services.

Built on the premise, that one wants to develop a Flow application in OSGi I assume that this developer then wants to be able to define his `@Route`/`@RouteAlias`/(other?) annotated classes also to be a `@Component` in OSGi.

## Contribute
You want to help making this work?
* PRs are highly welcome, just [![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io#https://github.com/Sandared/flow-osgi/blob/master/flow.osgi.integration/src/main/java/io/jatoms/flow/osgi/integration/FlowOsgiInitializer.java) and start coding/browsing/whatever ;)
* Any question/suggestion? -> Submit an issue
* I've also opened an [issue at Flow](https://github.com/vaadin/flow/issues/5017) in order to get feedback from Vaadin directly on this integration.
* Any other stuff? Write me on [Twitter](https://twitter.com/SanfteSchorle)

## HOW?
The idea is as follows:
* Replace `ServletContainerInitializers` that are used in Flow to initialize a `RouteRegistry` with a corresponding `BundleTracker` that is able to track all the classes with `@Route`/`RouteAlias`/etc.. see [FlowOsgiRouteTracker](https://github.com/Sandared/flow-osgi/blob/master/flow.osgi.integration/src/main/java/io/jatoms/flow/osgi/integration/FlowOsgiRouteTracker.java) and [FlowOsgiInitializer](https://github.com/Sandared/flow-osgi/blob/master/flow.osgi.integration/src/main/java/io/jatoms/flow/osgi/integration/FlowOsgiInitializer.java)
* As we want to use `@Route` annotated classes also as `@Components` in OSGi we need to somehow bridge the Flow and OSGi world. This is done via two connection points, i.e., `RouteRegistry` and `Instantiator`
  * [FlowOsgiRouteRegistry](https://github.com/Sandared/flow-osgi/blob/master/flow.osgi.integration/src/main/java/io/jatoms/flow/osgi/integration/FlowOsgiRouteRegistry.java) is used to dynamically add and remove `Routes` if used within an OSGi container. This is done via [FlowOsgiRouteTracker](https://github.com/Sandared/flow-osgi/blob/master/flow.osgi.integration/src/main/java/io/jatoms/flow/osgi/integration/FlowOsgiRouteTracker.java) scanning all classes for a `@Route` annotation and if present registering these classes as a services in OSGi so that they are dynamically injected to [FlowOsgiRouteRegistry](https://github.com/Sandared/flow-osgi/blob/master/flow.osgi.integration/src/main/java/io/jatoms/flow/osgi/integration/FlowOsgiRouteRegistry.java) at runtime. 
  * [FlowOsgiInstantiator](https://github.com/Sandared/flow-osgi/blob/master/flow.osgi.integration/src/main/java/io/jatoms/flow/osgi/integration/FlowOsgiInstantiator.java) is used to create the route targets as OSGi `@Components` so that they can participate in OSGi' service lifecycle and reference other services that are registered in an Gi runtime. The instantiator is created within Flow via `ServiceLoader` which can be used by us to inject our `Instantiator` into Flow
* A developer that wants to create a `Route` for an OSGi application then only has to do something like the following:

```java
@Route("")
@Component(factory="io.jatoms.example.MainView")
public class MainView extends VerticalLayout {

    @Reference
    GreeterService greeter;

    public MainView() {
        Button button = new Button("Click me",
                event -> Notification.show(greeter.greet()));
        add(button);
    }
}
```

This is an abstract view of the the logic flow of the solution:

![Logic Flow](https://github.com/Sandared/flow-osgi/blob/master/Unbenannt.PNG)
*Created with [plantuml](http://plantuml.com/de/) in [Markdown Preview Enhanced](https://shd101wyy.github.io/markdown-preview-enhanced/#/diagrams)*
