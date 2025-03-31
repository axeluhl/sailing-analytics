package com.sap.sailing.monitoring;

import java.util.Properties;

/**
 * Implementation of port monitor that prints out messages if
 * given endpoint is not connected.
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Nov 25, 2012
 */
public class SystemPrinterPortMonitor extends AbstractPortMonitor {

    public SystemPrinterPortMonitor(Properties prop) {
        super(prop);
    }

    @Override
    public void handleFailure(Endpoint endpoint) {
        System.out.println("ERROR: Could not connect to endpoint " + endpoint.toString());
    }

    @Override
    public void handleConnection(Endpoint endpoint) {
        System.out.println("INFO: Connection to " + endpoint.toString() + " succeeded.");
    }

}
