package com.sap.sailing.monitoring;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
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
    
    protected Endpoint[] endpoints = null;
    protected int interval;

    boolean started = false;
    boolean paused = false;
    
    private long lastmillis;
    private Endpoint currentendpoint;
    
    protected Properties properties;
    
    public AbstractPortMonitor(Properties properties) {
        String[] prop_endpoints = properties.getProperty("monitor.endpoints").split(",");
        
        this.endpoints = new Endpoint[prop_endpoints.length];
        for (int i=0; i<prop_endpoints.length;i++) {
            
            if (prop_endpoints[i].trim().startsWith("http")) {
                try {
                    URL url = new URL(prop_endpoints[i].trim());
                    EndpointImpl e = new EndpointImpl(url);
                    this.endpoints[i] = e;
                } catch (MalformedURLException ex) {
                    log.severe("Could not parse endpoint definition " + prop_endpoints[i]);
                }
            } else {
                String[] data = prop_endpoints[i].split(":");
                
                try {
                    EndpointImpl e = new EndpointImpl(new InetSocketAddress(InetAddress.getByName(data[0].trim()), Integer.parseInt(data[1].trim())));
                    this.endpoints[i] = e;
                } catch (Exception ex) {
                    log.severe("Could not parse endpoint definition " + prop_endpoints[i]);
                }
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
                                try {
                                    if (!currentendpoint.isURL()) {
                                        Socket sn = new Socket();
                                        sn.connect(currentendpoint.getAddress(), timeout);
                                        sn.close();
                                    } else {
                                        /* Avoid pooling http connections leading to TIME_WAIT */
                                        System.setProperty("http.keepAlive", "false");
                                        
                                        /* despite its name this does NOT open a real TCP connection */
                                        HttpURLConnection conn = (HttpURLConnection)currentendpoint.getURL().openConnection();
                                        conn.setConnectTimeout(timeout);
                                        conn.setRequestMethod("GET");
                                        conn.connect();
                                        int code = conn.getResponseCode();
                                        
                                        /* Make sure to also close the stream */
                                        try {
                                            conn.getInputStream().close();
                                        } catch (Exception ex) {
                                            /* ignore if there is no input stream */
                                        }
                                        
                                        conn.disconnect();
                                        if (code != 200) {
                                            throw new ConnectException("Could not successfully connect to endpoint " + currentendpoint.toString());
                                        }
                                    }

                                    log.finest("Connection succeeded to " + currentendpoint.toString());
                                    handleConnection(currentendpoint);
                                    
                                    lastmillis = System.currentTimeMillis();
                                    currentendpoint.setSuccess(System.currentTimeMillis());
                                    
                                } catch (SocketTimeoutException|ConnectException ex) {
                                    /* if service has failed before then wait at least some time before checking it again */
                                    boolean handle = true;
                                    if (currentendpoint.hasFailed() /* failed before */) {
                                        if ( (currentendpoint.lastFailed()+Integer.parseInt(properties.getProperty("monitor.wait_after_failure", "60000"))) > System.currentTimeMillis()) {                
                                            handle = false;
                                        } else {
                                            log.info("Service has failed (" + currentendpoint.toString() + ") before and gracetime is over. Calling failure handler again.");
                                        }
                                    } else {
                                        log.info("Connection FAILED to " + currentendpoint.toString());                            
                                    }
                                    
                                    if (handle) {
                                        handleFailure(currentendpoint);
                                        lastmillis = System.currentTimeMillis();
                                        currentendpoint.setFailure(System.currentTimeMillis());
                                    }
                                }
                            }
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
    
    public abstract void handleFailure(Endpoint endpoint);
    
    public abstract void handleConnection(Endpoint endpoint);

}
