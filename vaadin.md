```puml
package "Vaadin" {
    class Router
    class PwaRegistry
    class DefaultRouteResolver
    class SessionRouteRegistry
    class ApplicationRouteRegistry
    class RouteConfiguration
    class BeforeEvent
    class WebComponentRegistry
'    ServletContainerInitializer <|-- WebComponentRegistryInitializer
'    ServletContainerInitializer <|-- ServletVerifier
    ServletContainerInitializer <|-- RouteRegistryInitializer
'    ServletContainerInitializer <|-- NativeWebSocketServletContainerInitializer
'    ServletContainerInitializer <|-- JasperInitializer
    ServletContainerInitializer <|-- ErrorNavigationTargetInitializer
    ServletContainerInitializer <|-- ContainerInitializer
'    ServletContainerInitializer <|-- AnnotationValidator
    ServletContainerInitializer <|-- AnnotationScanningServletContainerInitializer
'    JasperInitializer <|-- JettyJasperInitializer
'    WebComponentRegistryInitializer --> WebComponentRegistry
    ApplicationRouteRegistry --> NavigationTargetFilter
}

package "OSGi" {
    class OSGiRouteRegistry
    class OSGiAccess
    class VaadinBundleTracker
    class OSGiDataCollector
'    class OSGiWebComponentRegistry
}


RouteRegistryInitializer --> ApplicationRouteRegistry


PwaRegistry --> OSGiRouteRegistry
DefaultRouteResolver --> OSGiRouteRegistry
SessionRouteRegistry --> OSGiRouteRegistry
ApplicationRouteRegistry --> OSGiRouteRegistry
RouteConfiguration --> OSGiRouteRegistry
BeforeEvent --> OSGiRouteRegistry
Router --> OSGiRouteRegistry
RouteRegistryInitializer --> RouteConfiguration

VaadinBundleTracker --> OSGiAccess
ApplicationRouteRegistry --> OSGiAccess
WebComponentRegistry --> OSGiAccess
OSGiRouteRegistry --> OSGiAccess

ApplicationRouteRegistry <|-- OSGiRouteRegistry
ApplicationRouteRegistry <|-- OSGiDataCollector
'WebComponentRegistry <|-- OSGiWebComponentRegistry
```

```puml
ServletContainer --> WebComponentRegistryInitializer : onStartup(Set<WebComponent> wc, ServletContext ctx)
WebComponentRegistryInitializer --> WebComponentRegistry : getInstance(ctx)

alt wc == null || wc.isEmpty()
    WebComponentRegistryInitializer --> WebComponentRegistry : setWebComponents(empty)
else wc contains elements
    WebComponentRegistryInitializer --> WebComponentRegistry : setWebComponents(wc.toMap())
end
note right
    Problem für OSGi:
    WebComponentRegistry kann
    nur einmal gesetzt werden.
    -> OSGiWebComponentRegistry
end note
```

```puml
ServletContainer --> RouteRegistryInitializer : onStartup(Set<Route> routes, ServletContext ctx)
RouteRegistryInitializer --> ApplicationRouteRegistry : registry = getInstance(ctx)
ApplicationRouteRegistry --> ServletContext : registry = getAttribute(RouteRegistry.class.getName())

alt registry == null
    ApplicationRouteRegistry --> ApplicationRouteRegistry : createRegistry(ctx)
    ApplicationRouteRegistry --> ServletContext : setAttribute(RouteRegistry.class.getName())
end

ApplicationRouteRegistry --> RouteRegistryInitializer : return registry

alt routes == null
    RouteRegistryInitializer --> ApplicationRouteRegistry : clean()
else routes != null
    RouteRegistryInitializer --> RouteConfiguration : forRegistry(registry)
    RouteRegistryInitializer --> RouteConfiguration : setRoutes(routes)
    RouteConfiguration --> RouteConfiguration : setAnnotatedRoute(route)
    RouteConfiguration --> ApplicationRouteRegistry : setRoute(route)
    RouteRegistryInitializer --> ApplicationRouteRegistry : setPwaConfigurationClass(...)
end
```

```puml
VaadinBundleTracker -> OSGiAccess : addScannedClasses
OSGiAccess -> OSGiAccess : resetContextInitializers
OSGiAccess -> OSGiAccess : handleTypes
OSGiAccess -> OSGiAccess : filterClasses
```

```puml
class VaadinService
interface Instantiator
class DefaultInstantiator

VaadinService --> Instantiator
Instantiator <|-- DefaultInstantiator
```


```puml
class UI
class Router
interface RouteRegistry
abstract class AbstractRouteRegistry
class ApplicationRouteRegistry
class OSGiRouteRegistry
class OSGiDataCollector
class BootstrapHandler
interface NavigationHandler
class NavigationStateRenderer
class InternalRedirectHandler
class ErrorStateRenderer
class NavigationEvent
interface RouteResolver
class DefaultRouteResolver
class VaadinService
abstract class AbstractNavigationStateRenderer {
    <T extends HasElement> T getRouteTaget(Class<T> routeTargetType, NavigationEvent event)
}
interface Instantiator {
    <T extends HasElement> T createRouteTarget(Class<T> routeTargetType, NavigationEvent event)
}
class DefaultInstantiator

VaadinService --> Instantiator
Instantiator <|-- DefaultInstantiator
NavigationStateRenderer ..> Instantiator

VaadinService --> Router
UI --> Router
Router --> RouteRegistry
Router ..> NavigationHandler: creates
RouteRegistry <|-- AbstractRouteRegistry
AbstractRouteRegistry <|-- ApplicationRouteRegistry
ApplicationRouteRegistry <|-- OSGiDataCollector
ApplicationRouteRegistry <|-- OSGiRouteRegistry
BootstrapHandler --> Router
RouteResolver <|-- DefaultRouteResolver

NavigationHandler <|-- AbstractNavigationStateRenderer
AbstractNavigationStateRenderer <|-- ErrorStateRenderer
AbstractNavigationStateRenderer <|-- NavigationStateRenderer
NavigationHandler <|-- InternalRedirectHandler
NavigationEvent --> Router
NavigationEvent --> UI
Router ..> NavigationEvent : creates

Router --> RouteResolver

```

# Eigene OSGi Implementierung

## Bisheriger Ablauf

### Setzen der RouteRegistry
```puml
ServletContainer --> RouteRegistryInitializer : onStartup(routes)
RouteRegistryInitializer --> RouteRegistry : getInstance(context)
RouteRegistry --> ServletContext : getAttribute(RouteRegistry.class.getName())
ServletContext --> RouteRegistry : RouteRegistry routeRegistry
RouteRegistry --> RouteRegistryInitializer : RouteRegistry routeRegistry
RouteRegistry --> RouteRegistry : createRegistry(context)
note left
	Hier wird entschieden
    welche Art von
    Registry verwendet
    wird
end note
RouteRegistry --> RouteRegistry : RouteRegistry routeRegistry
RouteRegistry --> ServletContext : setAttribute(ReouteRegistry.class.getName, routeRegistry)
RouteRegistryInitializer --> RouteRegistry : setNavigationTargets(routes)
RouteRegistryInitializer --> RouteRegistry : setPwaConfigurationClass(pwaClass)

... Other initialization stuff ...
```
### VaadinServlet Erstellung
```puml
ServletContainer --> ServletContextInitializers
ServletContextInitializers --> ServletDeployer : contextInitialized(sce)
ServletDeployer --> ServletDeployer : createAppServlet(context)
ServletDeployer --> RouteRegistry : getInstance(context)
RouteRegistry --> ServletContext : getAttribute(RouteRegistry.class.getName())
ServletContext --> RouteRegistry : RouteRegistry routeRegistry
RouteRegistry --> ServletDeployer : RouteRegistry routeRegistry
ServletDeployer --> RouteRegistry : hasNavigationTargets()
ServletDeployer --> ServletContext : addServlet(name, VaadinServlet.class)
ServletContext --> ServletDeployer : ServletRegistration.Dynamic registration
ServletDeployer --> ServletRegistration.Dynamic : setAsyncSupported(true)
ServletDeployer --> ServletRegistration.Dynamic : addMapping(path)
alt developmentMode
    ServletDeployer --> ServletDeployer : createServletIfNotExists(context, "frontendFilesServlet", "/frontend/")
end
```

### VaadinServlet Initialisierung
```puml
ServletContainer --> VaadinServlet : init(config)
VaadinServlet --> VaadinSerlet : createServletService(deploymentConfig)
VaadinServlet --> VaadinServletService : new (servlet, deploymentConfig)
VaadinServletService --> VaadinService : super (deploymentConfig)
VaadinService --> VaadinService : checkAtmosphereSupport()
VaadinService --> DeploymentConfiguration : getClassLoaderName()
VaadinService --> VaadinService : init()
VaadinService --> VaadinService : createInstantiator()
note left
	Hier wird entschieden
    welche Art von
    Instantiator verwendet
    wird: Erstellt Components
end note
... noch mehr Initialisierungsgedöns, aber erst mal uninteressant ...

```

### VaadinSerlet Request
```puml
VaadinServlet --> BootstrapHandler : synchronizedHandleRequest(session, request, response)
BootstrapHandler --> BootstrapHandler : getUIClass(request)
BootstrapHandler --> VaadinService : getDeploymentConfiguration()
VaadinService --> BootstrapHandler : deploymentConfiguration
BootstrapHandler --> DeploymentConfiguration : getUIClassName()
DeploymentConfiguration --> BootstrapHandler : uiClassName = UI.class.getName()
BootstrapHandler --> BootstrapHandler : createAndInitUI(uiClass, request, response, session)
... viel Initialisierungsgedöns aber wsl. erst mal uninteressant ...
BootstrapHandler --> UI : getRouter()
UI --> BootstrapHandler : router
BootstrapHandler --> Router : initializeUI(ui, request) 
```

## OSGiServletDeployer

* Kümmert sich um das registrieren des/der Servlets beim Container. Implementiert das `ServletContextListener` interface.
* Methode `contextInitialized` müsste überschrieben werden um eine OSGiRouteRegistry anmelden zu können

## OSGiRouteRegistry

## OSGiInstantiator
