package com.sap.sailing.domain.tractracadapter.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceHandle;
import com.sap.sailing.domain.tractracadapter.RaceTracker;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.data.DataController;
import com.tractrac.clientmodule.data.DataController.Listener;
import com.tractrac.clientmodule.setup.KeyValue;

public class RaceTrackerImpl implements Listener, RaceTracker {
    private final Event tractracEvent;
    private final com.sap.sailing.domain.base.Event domainEvent;
    private final Thread ioThread;
    private final DataController controller;
    private final Set<Receiver> receivers;
    private final DomainFactory domainFactory;
    private final DynamicTrackedEvent trackedEvent;

    /**
     * Creates a race tracked for the specified URL/URIs and starts receiving all available existing and future push
     * data from there. Receiving continues until {@link #stop()} is called.
     * <p>
     * 
     * A race tracker uses the <code>paramURL</code> for the TracTrac Java client to register for push data about one
     * race. The {@link RaceDefinition} for that race, however, isn't created until the {@link Course} has been
     * received. Therefore, the {@link RaceCourseReceiver} will create the {@link RaceDefinition} and will add it to the
     * {@link com.sap.sailing.domain.base.Event}.
     * <p>
     * 
     * The link to the {@link RaceDefinition} is created in the {@link DomainFactory} when the
     * {@link RaceCourseReceiver} creates the {@link TrackedRace} object. Starting then, the {@link DomainFactory} will
     * respond with the {@link RaceDefinition} when its {@link DomainFactory#getRace(Event)} is called with the TracTrac
     * {@link Event} as argument that is used for its tracking.
     * <p>
     * 
     * @param windStore
     *            Provides the capability to obtain the {@link WindTrack}s for the different wind sources. A trivial
     *            implementation is {@link EmptyWindStore} which simply provides new, empty tracks. This is always
     *            available but loses track of the wind, e.g., during server restarts.
     */
    protected RaceTrackerImpl(DomainFactory domainFactory, URL paramURL, URI liveURI, URI storedURI, WindStore windStore)
            throws URISyntaxException, MalformedURLException, FileNotFoundException {
        this.domainFactory = domainFactory;
        // Read event data from configuration file
        tractracEvent = KeyValue.setup(paramURL);
        
        // can happen that tractrac event is null (occurs when there is no internet connection)
        // so lets raise some meaningful exception
        if (tractracEvent == null) {
        	throw new RuntimeException("Connection failed. Could not connect to " + paramURL);
        }
        
        // Initialize data controller using live and stored data sources
        controller = new DataController(liveURI, storedURI, this);
        // Start live and stored data streams
        ioThread = new Thread(controller, "io");
        domainEvent = domainFactory.createEvent(tractracEvent);
        trackedEvent = domainFactory.getOrCreateTrackedEvent(domainEvent);
        receivers = new HashSet<Receiver>();
        Set<TypeController> typeControllers = new HashSet<TypeController>();
        for (Receiver receiver : domainFactory.getUpdateReceivers(trackedEvent, tractracEvent, windStore)) {
            receivers.add(receiver);
            for (TypeController typeController : receiver.getTypeControllers()) {
                typeControllers.add(typeController);
            }
        }
        addListenersForStoredDataAndStartController(typeControllers);
    }
    
    @Override
    public DynamicTrackedEvent getTrackedEvent() {
        return trackedEvent;
    }
    
    @Override
    public RaceHandle getRaceHandle() {
        return new RaceHandleImpl(domainFactory, tractracEvent, getTrackedEvent());
    }
    
    @Override
    public RaceDefinition getRace() {
        return domainFactory.getRace(tractracEvent);
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
        for (Receiver receiver : receivers) {
            receiver.stop();
        }
        ioThread.join(3000); // wait no more than three seconds
        trackedEvent.removedTrackedRace(trackedEvent.getTrackedRace(getRace()));
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
