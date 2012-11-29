package com.sap.sailing.monitoring;

import java.net.InetSocketAddress;


/**
 * @see Endpoint
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Nov 28, 2012
 */
public class EndpointImpl implements Endpoint {

    private InetSocketAddress address;
    private String name = "";
    private long last_success = 0;
    private long last_fail = 0;
    private boolean already_checked = false;
    
    public EndpointImpl(InetSocketAddress address) {
        this.address = address;
    }
    
    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public long lastSucceeded() {
        return last_success;
    }

    @Override
    public long lastFailed() {
        return last_fail;
    }

    @Override
    public boolean alreadyChecked() {
        return already_checked;
    }
    
    @Override
    public boolean hasFailed() {
        return (last_fail > last_success);
    }
    
    public void setSuccess(long millis) {
        last_success = millis;
        already_checked = true;
    }
    
    public void setFailure(long millis) {
        last_fail = millis;
    }
    
    public String toString() {
        return getAddress().toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}
