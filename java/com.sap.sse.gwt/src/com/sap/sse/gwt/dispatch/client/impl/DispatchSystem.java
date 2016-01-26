package com.sap.sse.gwt.dispatch.client.impl;

import java.util.Date;

import com.sap.sse.gwt.dispatch.client.DispatchSystemAsync;
import com.sap.sse.gwt.dispatch.client.DispatchContext;

/**
 * The dispatch system is the client side implementation of the dispatch communication pattern.
 *
 * @param <CTX>
 *            the context that will be provided to the actions during execution
 */
public interface DispatchSystem<CTX extends DispatchContext> extends DispatchSystemAsync<CTX> {
    Date getCurrentServerTime();
}
