package com.sap.sailing.gwt.ui.server.dispatch;

import com.sap.sailing.gwt.ui.server.ProxiedRemoteServiceServlet;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContextImpl;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;
import com.sap.sailing.gwt.ui.shared.dispatch.rpc.DispatchRPC;

public class DispatchRPCImpl extends ProxiedRemoteServiceServlet implements DispatchRPC {

    private static final long serialVersionUID = -245230476512348999L;

    private final DispatchContext ctx;

    public DispatchRPCImpl() {
        ctx = new DispatchContextImpl();
    }

    @Override
    public <R extends Result, A extends Action<R>> R execute(A action) throws DispatchException {
        return action.execute(ctx);
    }

}
