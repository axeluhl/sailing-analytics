package com.sap.sailing.gwt.ui.server.dispatch;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.gwt.ui.server.ProxiedRemoteServiceServlet;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContextImpl;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.RequestWrapper;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWrapper;
import com.sap.sailing.gwt.ui.shared.dispatch.rpc.DispatchRPC;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.util.ServiceTrackerFactory;

public class DispatchRPCImpl extends ProxiedRemoteServiceServlet implements DispatchRPC {

    private static final long serialVersionUID = -245230476512348999L;
    
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    public DispatchRPCImpl() {
        final BundleContext context = Activator.getDefault();
//      final Activator activator = Activator.getInstance();

      racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
    }

    @Override
    public <R extends Result, A extends Action<R>> ResultWrapper<R> execute(RequestWrapper<R, A> request) throws DispatchException {
        return new ResultWrapper<R>(request.getAction().execute(new DispatchContextImpl(request.getCurrentClientTime(), racingEventServiceTracker)));
    }

}
