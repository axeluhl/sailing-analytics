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
public interface IEndpoint {
    
    public InetSocketAddress getAddress();
    
    public String getName();
    
    public long lastSucceeded();
    
    public long lastFailed();
    
    public boolean alreadyChecked();
    
    public boolean hasFailed();
    
    public void setSuccess(long millis);
    
    public void setFailure(long millis);
    
    public void setName(String name);
    
    public String toString();

}
