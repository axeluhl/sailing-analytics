package com.sap.sailing.monitoring;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Simple post monitoring application. Checks given ports in an regular interval. 
 * If there is a problem it calls the registered handler.
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Nov 25, 2012
 */
public abstract class AbstractPortMonitor extends Thread {

    Logger log = Logger.getLogger(AbstractPortMonitor.class.getName());
    
    private int timeout = 1000;
    private int graceful = 5000;
    private int pause = 1000;
    
    protected IEndpoint[] endpoints = null;
    protected int interval;

    boolean started = false;
    boolean paused = false;
    
    private long lastmillis;
    private IEndpoint currentendpoint;
    
    protected Properties properties;
    
    public AbstractPortMonitor(Properties properties) {
        String[] prop_endpoints = properties.getProperty("monitor.endpoints").split(",");
        
        this.endpoints = new IEndpoint[prop_endpoints.length];
        for (int i=0; i<prop_endpoints.length;i++) {
            String[] data = prop_endpoints[i].split(":");
            
            try {
                Endpoint e = new Endpoint(new InetSocketAddress(InetAddress.getByName(data[0].trim()), Integer.parseInt(data[1].trim())));
                this.endpoints[i] = e;
            } catch (Exception ex) {
                log.severe("Could not parse endpoint definition " + prop_endpoints[i]);
            }
        }
        
        this.interval = Integer.parseInt(properties.getProperty("monitor.interval", ""+interval).trim());
        this.timeout = Integer.parseInt(properties.getProperty("monitor.timeout", ""+timeout).trim());
        this.graceful = Integer.parseInt(properties.getProperty("monitor.gracetime", ""+graceful).trim());
        this.pause = Integer.parseInt(properties.getProperty("monitor.pause", ""+pause).trim());
        this.started = true;
        this.properties = properties;
        log.info("Initialized monitoring!");
    }

    public void startMonitoring() {
        this.start();
    }
    
    @Override
    public void run() {
        try {
            /* graceful start - give bundles some time */
            Thread.sleep(graceful);
            while (started) {
                if (!paused) {
                    long currentmillis = System.currentTimeMillis();
                    try {
                        if (currentmillis >= (lastmillis + interval + (100*endpoints.length))) {
                            for (int i = 0; i < endpoints.length; i++) {
                                currentendpoint = endpoints[i];
                                
                                Socket sn = new Socket();
                                sn.connect(currentendpoint.getAddress(), timeout);
                                sn.close();
                                
                                log.info("Connection succeeded to " + currentendpoint.toString());
                                handleConnection(currentendpoint);
                                
                                lastmillis = System.currentTimeMillis();
                                currentendpoint.setSuccess(System.currentTimeMillis());
                            }
                        }
                    } catch (SocketTimeoutException|ConnectException ex) {
                        log.info("Connection FAILED to " + currentendpoint.toString());

                        /* if service has failed before then wait at least some time before checking it again */
                        boolean handle = true;
                        if (currentendpoint.hasFailed() /* failed before */) {
                            if ( (currentendpoint.lastFailed()+Integer.parseInt(properties.getProperty("monitor.wait_after_failure", "60000"))) > System.currentTimeMillis()) {                
                                log.info("Not calling handler because failure has been shortly before this call");                                
                                handle = false;
                            }
                        }
                        
                        if (handle) {
                            handleFailure(currentendpoint);
                            lastmillis = System.currentTimeMillis();
                            currentendpoint.setFailure(System.currentTimeMillis());
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    log.info("Pausing...");
                }
                Thread.sleep(pause);
            }
        } catch (InterruptedException ex) {
            System.err.println("Interrupted");
        }
    }
    
    public void stopMonitoring() {
        this.started = false;        
    }
    
    public abstract void handleFailure(IEndpoint endpoint);
    
    public abstract void handleConnection(IEndpoint endpoint);

}
