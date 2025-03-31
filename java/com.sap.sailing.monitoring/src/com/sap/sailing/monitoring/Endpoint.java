package com.sap.sailing.monitoring;

import java.net.InetSocketAddress;
import java.net.URL;

/**
 * Definition of an endpoint that submits to checks by 
 * the monitoring service. It consists of an address, a port
 * and additional status information.
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Nov 28, 2012
 */
public interface Endpoint {
    
    /**
     * @return the address of this endpoint
     */
    InetSocketAddress getAddress();
    
    /**
     * @return the name of this endpoint. This information can be used
     * to reference other services.
     */
    String getBundleName();
    
    /**
     * @return true if this endpoint represents an url
     */
    boolean isURL();
    
    /**
     * @return the associated URL or null if there is none
     */
    URL getURL();
    
    long lastSucceeded();
    
    long lastFailed();
    
    boolean alreadyChecked();
    
    boolean hasFailed();
    
    void setSuccess(long millis);
    
    void setFailure(long millis);
    
    void setBundleName(String name);
    
    String toString();

}
