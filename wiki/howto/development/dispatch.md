# Dispatch

[[_TOC_]]

## Traditional dispatch

Dispatch is a communication pattern based on the command pattern. 
 
The picture below overviews the tradional dispatch approach:
  
![Traditional Dispatch](/wiki/images/dispatch/traditional_dispatch.png)

The main working parts of a traditional dispatch system are:

* action: the action is the instance sent to the server through the dispatch system
* result: the result is the answer for a given action sent to the server
* client dispatch: client side dispatch implementation
* server dispatch: server side dispatch implementation
* action handler: server side command execution 

The big picture is: instead of calling services, 

* the client sends actions to to server. 
* The server answers result objects to the client. 

The dispatch system does not change the asynchrounous nature of the client server communication imposed by GWT.

The actions and the result in a dispatch system are usually defined by the following wti interfaces, 
so that each action must implement the action interface, and each result must implement the result interface.

 
    public interface Action<R extends Result> extends IsSerializable {
    
    }
    
    public interface Result extends IsSerializable {
    
    }

  
The dispatch interface basically provides one method for the command execution:

    <R extends Result, A extends Action<R>> void execute(A action, AsyncCallback<R> callback);

The traditional dispatch approach clearly separates client from server code: the server side execution is performed by ActionHandlers. 
Those can be typed against the Action and Result further ensuring type safety.
 
 
## SSE dispatch

The dispatch implementation does slightly differ from the traditional dispatch implementation. 
The code that is executed on the server side is not located in an ActionHandler, it is placed direclty the action class:

    public interface Action<R extends Result, CTX extends DispatchContext> extends IsSerializable {
    
        @GwtIncompatible
        R execute(CTX ctx) throws DispatchException;
    
    }

The GwtIncompatible annotation is required and makes it possible to place backend code in a class that will also be available on the client side.  

By putting the code that will be executed on the server side directly in the action class, we need to provide access to the server side services. 
The execution is triggered directly by the receiving dispatch servlet, eliminating the need for an ActionHandler.

![SSE Dispatch](/wiki/images/dispatch/sse_dispatch.png)

## Server side setup

Each Web-Fragment must provide an Dispatch/ RPC endpoint. 
The com.sap.sse.gwt GWT module provides an abstract base that implements the action execution. 

Each web fragment is required to

* define a custom DispatchContext
* implement the servlet
* register the servlet in the web.xml
 
Example DispatchContext defined by Sailing:

    public interface SailingDispatchContext extends DispatchContext {
        @GwtIncompatible
        RacingEventService getRacingEventService();
    
        @GwtIncompatible
        EventNewsService getEventNewsService();
    
        @GwtIncompatible
        String getClientLocaleName();
    
        @GwtIncompatible
        Locale getClientLocale();
    
        @GwtIncompatible
        Date getCurrentClientTime();
        
    }

Please note that all methods must be declared GwtIncompatible, since those methods will only be available on the server side.
The interface is used on the client side to ensure typesafety.

The Servlet provided by Sailing does simply extend the provided AbstractDispatchServlet and create the required context for each incoming request:

 
    public class SailingDispatchServlet extends AbstractDispatchServlet<SailingDispatchContext> {
        private static final long serialVersionUID = -245230476512348999L;
    
        private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
        private final ServiceTracker<EventNewsService, EventNewsService> eventNewsServiceTracker;
    
        public SailingDispatchServlet() {
            final BundleContext context = Activator.getDefault();
            racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
            eventNewsServiceTracker = ServiceTrackerFactory.createAndOpen(context, EventNewsService.class);
        }
    
        @Override
        protected <R extends Result, A extends Action<R, SailingDispatchContext>> SailingDispatchContext createDispatchContextFor(
                RequestWrapper<R, A, SailingDispatchContext> request) {
            return new SailingDispatchContextImpl(request.getCurrentClientTime(),
            racingEventServiceTracker.getService(), eventNewsServiceTracker.getService(), request
                    .getClientLocaleName(), getThreadLocalRequest());
        }
    }
 
Finally, the SailingDispatchServlet is registered in the module WEB-INF/web.xml, here the fragment:
 
    <servlet>
        <display-name>Sailing Dispatch Service</display-name>
        <servlet-name>sailingDispatch</servlet-name>
        	<servlet-class>com.sap.sailing.gwt.home.server.servlets.SailingDispatchServlet</servlet-class>
    </servlet>
    <servlet-mapping>
    	<servlet-name>sailingDispatch</servlet-name>
    	<url-pattern>/service/dispatch</url-pattern>
    </servlet-mapping>
    

## Client side setup
 
The client code needs a reference to the dispatch system in order to send commands to the server.

The com.sap.sse.gwt module provides a base class for easy setup: 

com.sap.sse.gwt.dispatch.client.system.DispatchSystemDefaultImpl<SailingDispatchContext>
 
All a module needs to to is to subclass the default dispatch implementation:

    public class SailingDispatchSystemImpl extends DispatchSystemDefaultImpl<SailingDispatchContext> implements
            SailingDispatchSystem {
        
        public SailingDispatchSystemImpl() {
            super(RemoteServiceMappingConstants.dispatchServiceRemotePath);
        }
    }

The SailingDispatchContext is the same interface used by the server side implementation.

## Communicating with the server

Once the client and server side of the dispatch system are properly setup, the client can start sending commands to the server.

The action must implement the Action interface and be typed against the Required result and the custom DispatchContext defined in the module.

As an example, here is how Sailing implemented the Actions.

First, we introduced a SailingAction interface that implements the typing against the SailingDispatchContext:


    public interface SailingAction<R extends Result> extends Action<R, SailingDispatchContext> {
    
        @GwtIncompatible
        R execute(SailingDispatchContext ctx) throws DispatchException;
    
    }

This way, all SailingActions are automatically strongly typed against the SailingDispatchContext.

The next step is to implement an Action, here is the simple GetSearchServerNamesAction example:


    public class GetSearchServerNamesAction implements SailingAction<StringsResult> {
    
        @Override
        @GwtIncompatible
        public StringsResult execute(SailingDispatchContext ctx) throws DispatchException {
            StringsResult result = new StringsResult();
            for (RemoteSailingServerReference remoteServerRef : ctx.getRacingEventService().getLiveRemoteServerReferences()) {
                result.addValue(remoteServerRef.getName());
            }
            return result;
        }
        
    }

This action is typed against the StringsResult, a base result class provided by the dispatch system:
                                              
    public class StringsResult implements DTO, Result {
    
        private ArrayList<String> values = new ArrayList<>();
    
        public StringsResult() {
        }
    
        public StringsResult(Collection<String> values) {
            this.values.addAll(values);
        }
    
        public void addValue(String value) {
            if (value != null) {
                this.values.add(value);
            }
        }
    
        public List<String> getValues() {
            return values;
        }
        
        public boolean isEmpty() {
            return this.values.isEmpty();
        }
    
    }
    
There are a few more default result implemetations provided by the com.sap.sse.gwt in the com.sap.sse.gwt.dispatch.shared.commands package.

To finally make the server side call, just send the action instance through the dispatch system:
 
    clientFactory.getDispatch().execute(
        
        new GetSearchServerNamesAction(), 
        
        new AsyncCallback<StringsResult>() {
            @Override
            public void onSuccess(StringsResult result) {
                for (String serverName : result.getValues()) {
                    //...
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                //...
            }
        }
        
    );
 
 
## Exception handling
 
The Action interface explicetely declares that the server side execution may throw a DispatchException: 

    @GwtIncompatible
    R execute(CTX ctx) throws DispatchException;

Custom exceptions that should be delivered to the client should always extend the DispatchException. 

![SSE Exception](/wiki/images/dispatch/sse_exceptions.png)

The DispatchException creates an unique UUID String that is logged on the server side and sent to the client. 
This way it is possible to find the server exception catched in the client code in the server logs (if logging on both ends is properly configured).

## The default transport
 
The default (and currently only one) transport mechanism uses GWT RPC to communicate with the server. The implementations can be found in the com.sap.sse.gwt.dispatch.client.transport package.

## The dispatch decorators

The current dispatch implementation DispatchSystemDefaultImpl uses the decorator pattern 
to add cross cutting concern functionality to the core dispatch system.
 
There are currently two decorators provided by the com.sap.sse.gwt module: CachingDispatch and AutomaticBatchingDispatch.

![SSE Dispatch Decorators](/wiki/images/dispatch/sse_dispatch_decorators.png)

### CachingDispatch

The caching dispatch decorator uses th IsClientCacheable contract interface to identify actions that may be cached on 
the client side of the dispatch system.

The results are stored in a HashMap under an instance specific key. The key consists two parts: the class 
name and an custom instance key created by calling the IsClientCacheable.cacheInstanceKey(StringBuilder key) on the action.

The caching decorator delivers resuls from the local cache transparently to the caller. 

The motivation for the caching decorator is to significantly reduce the amount of data sent over the wire. 
This is especially important on mobile devices, where delivering the results 
for the back and forth navigation from local cache improves navigation speed.

### AutomaticBatchingDispatch

The AutomaticBatchingDispatch decorator provides transparent batching: actions fired in the same event loop are 
queued up and sent as one batch action to the server, where the actions are sequentially "unpacked" and "executed".

![SSE Batching](/wiki/images/dispatch/sse_batching.png)

This means that the order in which the action get fired is the order in which they get enqueued/ executed. 

The batching reduces the amount of data sent over the wire and keeps the number of the connections 
low – both improvements regarding user latency experience. 

By ensuring order of execution, the application must not wait until one request/ action finishes before starting 
the next, eliminating the need for extra roundtrips otherwise required due to the asynchronous nature of AJAX calls. 

![AJAX Async](/wiki/images/dispatch/ajax-async.png)

