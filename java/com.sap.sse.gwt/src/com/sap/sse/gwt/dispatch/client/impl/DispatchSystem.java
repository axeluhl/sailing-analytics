package com.sap.sse.gwt.dispatch.client.impl;

import java.util.Date;

import com.sap.sse.gwt.dispatch.client.DispatchAsync;
import com.sap.sse.gwt.dispatch.client.DispatchContext;

public interface DispatchSystem<CTX extends DispatchContext> extends DispatchAsync<CTX> {
    Date getCurrentServerTime();
}
