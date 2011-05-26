package com.sap.sailing.domain.tractracadapter.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.EventTracker;
import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.data.DataController;
import com.tractrac.clientmodule.data.DataController.Listener;
import com.tractrac.clientmodule.setup.KeyValue;

public class EventTrackerImpl implements Listener, EventTracker {
    private final Event tractracEvent;
    private final com.sap.sailing.domain.base.Event domainEvent;
    private final Thread ioThread;
    private final DataController controller;

    protected EventTrackerImpl(DomainFactory domainFactory, URL paramURL, URI liveURI, URI storedURI)
            throws URISyntaxException, MalformedURLException, FileNotFoundException {
        // Read event data from configuration file
        tractracEvent = KeyValue.setup(paramURL);
        // Initialize data controller using live and stored data sources
        controller = new DataController(liveURI, storedURI, this);
        // Start live and stored data streams
        ioThread = new Thread(controller, "io");
        domainEvent = domainFactory.createEvent(tractracEvent);
        DynamicTrackedEvent trackedEvent = domainFactory.trackEvent(domainEvent);
        addListenersForStoredDataAndStartController(domainFactory.getUpdateReceivers(trackedEvent));
    }
    
    protected void addListenersForStoredDataAndStartController(Iterable<TypeController> listenersForStoredData) {
        for (TypeController listener : listenersForStoredData) {
            getController().add(listener);
        }
        startController();
    }
    
    @Override
    public com.sap.sailing.domain.base.Event getEvent() {
        return domainEvent;
    }
    
    /**
     * Called when the {@link #storedDataEnd()} event was received. Adds the listeners
     * returned to the {@link #getController() controller}, presumably for live data.
     * This default implementation returns an empty iterable. Subclasses may override
     * to return more.
     */
    protected Iterable<TypeController> getListenersForLiveData() {
        return Collections.emptySet();
    }

    protected void startController() {
        ioThread.start();
    }
    
    @Override
    public void stop() throws MalformedURLException, IOException, InterruptedException {
        controller.stop(/* abortStored */ true);
        ioThread.join();
    }

    protected DataController getController() {
        return controller;
    }

    @Override
    public void liveDataConnected() {
        System.out.println("Live data connected");
    }

    @Override
    public void liveDataDisconnected() {
        System.out.println("Live data disconnected");
    }

    @Override
    public void stopped() {
        System.out.println("stopped");
    }

    @Override
    public void storedDataBegin() {
        System.out.println("Stored data begin");
    }

    @Override
    public void storedDataEnd() {
        System.out.println("Stored data end");
    }

    @Override
    public void storedDataProgress(float progress) {
        System.out.println("Stored data progress: "+progress);
        
    }

    @Override
    public void storedDataError(String arg0) {
        System.err.println("Error with stored data "+arg0);
    }

    @Override
    public void liveDataConnectError(String arg0) {
        System.err.println("Error with live data "+arg0);
    }
}
