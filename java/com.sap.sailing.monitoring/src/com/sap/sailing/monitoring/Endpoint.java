package com.sap.sailing.monitoring;

import java.net.InetSocketAddress;

/**
 * Definition of an endpoint that submits to checks by 
 * the monitoring service. It consists of an address, a port
 * and additional status information.
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Nov 28, 2012
 */
public interface Endpoint {
    
    InetSocketAddress getAddress();
    
    String getName();
    
    long lastSucceeded();
    
    long lastFailed();
    
    boolean alreadyChecked();
    
    boolean hasFailed();
    
    void setSuccess(long millis);
    
    void setFailure(long millis);
    
    void setName(String name);
    
    String toString();

}
