package com.sap.sse.gwt.dispatch.client.transport;

import com.sap.sse.gwt.dispatch.client.system.DispatchContext;
import com.sap.sse.gwt.dispatch.client.transport.gwtrpc.DispatchRPCImpl;

/**
 * Current default dispatch transport.
 *
 * @param <CTX>
 */
public class DefaultTransport<CTX extends DispatchContext> extends DispatchRPCImpl<CTX> {
    public DefaultTransport(String dispatchRPCPath) {
        super(dispatchRPCPath);
    }
}
