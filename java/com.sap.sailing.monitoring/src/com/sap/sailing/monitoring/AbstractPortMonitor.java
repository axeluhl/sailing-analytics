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
    
    protected InetSocketAddress[] endpoints = null;
    protected int interval;

    boolean started = false;
    boolean paused = false;
    
    private long lastmillis;
    private InetSocketAddress currentendpoint;
    
    protected Properties properties;

    public AbstractPortMonitor(InetSocketAddress[] endpoints, int interval) {
        this.endpoints = endpoints;
        if (interval < 10000) {
            throw new RuntimeException("Interval can not be lower than 10 seconds!");
        }
        this.interval = interval;
        this.started = true;
        log.info("Initialized monitoring!");
    }
    
    public AbstractPortMonitor(Properties properties) {
        String[] prop_endpoints = properties.getProperty("monitor.endpoints").split(",");
        this.endpoints = new InetSocketAddress[prop_endpoints.length];
        try {
            for (int i=0;i<prop_endpoints.length;i++) {
                String[] data = prop_endpoints[i].split(":");
                this.endpoints[i] = new InetSocketAddress(InetAddress.getByName(data[0].trim()), Integer.parseInt(data[1].trim()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
            /* graceful start - give services some time */
            Thread.sleep(graceful);
            while (started) {
                if (!paused) {
                    long currentmillis = System.currentTimeMillis();
                    try {
                        if (currentmillis >= (lastmillis + interval + (100*endpoints.length))) {
                            for (int i = 0; i < endpoints.length; i++) {
                                currentendpoint = endpoints[i];
                                
                                Socket sn = new Socket();
                                sn.connect(currentendpoint, timeout);
                                
                                log.info("Connection succeeded to " + currentendpoint.toString());
                                handleConnection(currentendpoint);
                                lastmillis = System.currentTimeMillis();
                            }
                        }
                    } catch (SocketTimeoutException|ConnectException ex) {
                        log.info("Connection FAILED to " + currentendpoint.toString());
                        handleFailure(currentendpoint);
                        lastmillis = System.currentTimeMillis();
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
    
    protected void removeEndpoint(InetSocketAddress endpoint) {
        InetSocketAddress[] new_endpoints = new InetSocketAddress[endpoints.length-1];
        for (int i=0;i<endpoints.length;i++) {
            if (endpoints[i] != endpoint)
                new_endpoints[i] = endpoint;
        }
        this.endpoints = new_endpoints;
    }
    
    public abstract void handleFailure(InetSocketAddress endpoint);
    
    public abstract void handleConnection(InetSocketAddress endpoint);

}
