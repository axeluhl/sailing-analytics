package com.sap.sailing.monitoring;

import java.net.InetSocketAddress;

/**
 * Implementation of port monitor that prints out messages if
 * given endpoint is not connected.
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Nov 25, 2012
 */
public class SystemPrinterPortMonitor extends AbstractPortMonitor {

    public SystemPrinterPortMonitor(InetSocketAddress[] endpoints, int interval) {
        super(endpoints, interval);
    }

    @Override
    public void handleFailure(InetSocketAddress endpoint) {
        System.out.println("ERROR: Could not connect to endpoint " + endpoint.toString());
    }

    @Override
    public void handleConnection(InetSocketAddress endpoint) {
        System.out.println("INFO: Connection to " + endpoint.toString() + " succeeded.");
    }

}
