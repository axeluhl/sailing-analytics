package com.sap.sailing.gwt.ui.server.dispatch;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.gwt.ui.server.ProxiedRemoteServiceServlet;
import com.sap.sailing.gwt.ui.server.dispatch.handlers.BatchActionHandler;
import com.sap.sailing.gwt.ui.server.dispatch.handlers.GetLiveRacesActionHandler;
import com.sap.sailing.gwt.ui.server.dispatch.handlers.OtherActionHandler;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;
import com.sap.sailing.gwt.ui.shared.dispatch.rpc.DispatchRPC;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.util.ServiceTrackerFactory;

public class DispatchRPCImpl extends ProxiedRemoteServiceServlet implements DispatchRPC {

    private static final long serialVersionUID = -245230476512348999L;
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    private final Map<Class<? extends Action<?>>, Handler<? extends Result, ? extends Action<?>>> handlers = new HashMap<>();

    public DispatchRPCImpl() {
        BundleContext context = Activator.getDefault();
        Activator activator = Activator.getInstance();
        if (context != null) {
            // TODO
            // activator.setSailingService(this); // register so this service is informed when the bundle shuts down
        }
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);

        addHandlers();
    }

    private void addHandlers() {
        addHandler(new BatchActionHandler());
        addHandler(new GetLiveRacesActionHandler(racingEventServiceTracker));
        addHandler(new OtherActionHandler(racingEventServiceTracker));

    }

    private <R extends Result, A extends Action<R>> void addHandler(Handler<R, A> handler) {
        Class<A> actionType = handler.getType();
        if (handlers.containsKey(actionType)) {
            String errorMessage = MessageFormat.format(
                    "A handler for Action {0} is already registered. Registered handler: {1}, new handler: {2}",
                    actionType.getName(), handlers.get(actionType).getClass().getName(), handler.getClass().getName());
            throw new IllegalStateException(errorMessage);
        }
        handlers.put(actionType, handler);
    }

    @Override
    public <R extends Result, A extends Action<R>> R execute(A action) throws DispatchException {
        return execute(action, new DispatchContextImpl(getThreadLocalRequest(), getThreadLocalResponse()));
    }
    
    @SuppressWarnings({ "unchecked" })
    public <R extends Result, A extends Action<R>> R execute(A action, DispatchContext context) throws DispatchException {
        Class<A> actionType = (Class<A>) action.getClass();
        
        Handler<R, A> handler = (Handler<R, A>) handlers.get(actionType);
        if (handler != null) {
            return handler.execute(action, context);
        }
        String errorMessage = MessageFormat.format("No handler found for Action {0}", actionType.getName());
        throw new IllegalStateException(errorMessage);
    }
    
    private class DispatchContextImpl implements DispatchContext {
        @SuppressWarnings("unused")
        private final HttpServletRequest req;
        @SuppressWarnings("unused")
        private final HttpServletResponse resp;

        public DispatchContextImpl(HttpServletRequest req, HttpServletResponse resp) {
            this.req = req;
            this.resp = resp;
        }
        
        public <R extends Result, A extends Action<R>> R execute(A action) throws DispatchException {
            return DispatchRPCImpl.this.execute(action, this);
        }
    }

}
