package com.sap.sse.gwt.client;

import com.sap.sse.gwt.dispatch.client.system.DispatchSystemDefaultImpl;

/**
 * Interface used to append a routing suffix to target url 
 * in GWT RPC service instances and in {@link DispatchSystemDefaultImpl} 
 */
public interface ServiceRoutingProvider {
    
    String routingSuffixPath();
}
