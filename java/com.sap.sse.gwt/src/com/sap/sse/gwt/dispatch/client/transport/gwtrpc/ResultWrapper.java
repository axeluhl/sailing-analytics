package com.sap.sse.gwt.dispatch.client.transport.gwtrpc;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

/**
 * Result wrapper that encapsulates the curent server time.
 *
 * @param <R>
 */
public class ResultWrapper<R extends Result> implements IsSerializable {
    
    private Date currentServerTime = new Date();
    
    private R result;
    
    @SuppressWarnings("unused")
    private ResultWrapper() {
    }

    public ResultWrapper(R result) {
        super();
        this.result = result;
    }
    
    public Date getCurrentServerTime() {
        return currentServerTime;
    }
    
    public R getResult() {
        return result;
    }
}
