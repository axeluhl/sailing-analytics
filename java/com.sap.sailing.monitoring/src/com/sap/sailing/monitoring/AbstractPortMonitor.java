package com.sap.sailing.monitoring;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Simple post monitoring application. Checks given ports in an regular interval. 
 * If there is a problem it calls the registered handler.
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Nov 25, 2012
 */
public abstract class AbstractPortMonitor extends Thread {

    private final int TIMEOUT = 1000;
    private final int GRACEFUL = 5000;
    private final int PAUSE = 1000;
    
    private InetSocketAddress[] endpoints = null;
    private int interval;

    boolean started = false;
    private long lastmillis;
    private InetSocketAddress currentendpoint;

    public AbstractPortMonitor(InetSocketAddress[] endpoints, int interval) {
        this.endpoints = endpoints;
        
        if (interval < 10000) {
            throw new RuntimeException("Interval can not be lower than 10 seconds!");
        }
        
        this.interval = interval;
        this.started = true;
    }

    public void startMonitoring() {
        this.start();
    }
    
    @Override
    public void run() {
        
        try {
            /* graceful start - give services some time */
            Thread.sleep(GRACEFUL);
            
            while (started) {
                long currentmillis = System.currentTimeMillis();
                try {
                    if (currentmillis >= (lastmillis + interval + (100*endpoints.length))) {
                        for (int i = 0; i < endpoints.length; i++) {
                            currentendpoint = endpoints[i];
                            
                            Socket sn = new Socket();
                            sn.connect(currentendpoint, TIMEOUT);
                            
                            handleConnection(currentendpoint);
                            lastmillis = System.currentTimeMillis();
                        }
                    }
    
                    Thread.sleep(PAUSE);
                    
                } catch (SocketTimeoutException ex) {
                    handleFailure(currentendpoint);
                    
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        } catch (InterruptedException ex) {
            System.out.println("Interrupted");
        }
            
    }
    
    public void stopMonitoring() {
        this.started = false;        
    }
    
    public abstract void handleFailure(InetSocketAddress endpoint);
    
    public abstract void handleConnection(InetSocketAddress endpoint);

}
