package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.junit.Before;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;
import com.tractrac.clientmodule.Race;

/**
 * Connects to the Kieler Woche 2011 TracTrac data of the 505 Race 2. Subclasses should implement a @Before method which
 * calls {@link #setUp(String, ReceiverType[])} with a useful set of receiver types and the race they want to observe /
 * load, or they should call {@link #setUp(String, ReceiverType[])} at the beginning of each respective test in case
 * they want to select/load different races for different tests. When all stored data has been received, the
 * {@link #getSemaphor() semaphor} is notified. Therefore, a typical pattern for subclasses should be to invoke
 * {@link #setUp(ReceiverType[])}, then wait on the semaphor before starting with test processing.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public abstract class KielerWoche2011BasedTest extends AbstractTracTracLiveTest {
    private DomainFactoryImpl domainFactory;
    private Event domainEvent;
    private DynamicTrackedEvent trackedEvent;
    private RaceDefinition race;
    private DynamicTrackedRace trackedRace;

    private final Object semaphor = new Object();
    
    /**
     * When the {@link #semaphor} is notified, this flag indicates whether {@link #storedDataEnd()} has already
     * been called.
     */
    private boolean storedDataLoaded;

    protected KielerWoche2011BasedTest() throws MalformedURLException, URISyntaxException {
        super();
    }
    
    
    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException, URISyntaxException {
        // keep superclass implementation from automatically setting up for a Weymouth event and force subclasses
        // to select a race
    }

    protected void setUp(String raceId, ReceiverType... receiverTypes) throws MalformedURLException, IOException, InterruptedException, URISyntaxException {
        setUpWithoutLaunchingController(raceId);
        completeSetupLaunchingControllerAndWaitForRaceDefinition(receiverTypes);
    }


    protected void completeSetupLaunchingControllerAndWaitForRaceDefinition(ReceiverType... receiverTypes)
            throws InterruptedException {
        setStoredDataLoaded(false);
        ArrayList<Receiver> receivers = new ArrayList<Receiver>();
        for (Receiver r : domainFactory.getUpdateReceivers(trackedEvent, getEvent(), EmptyWindStore.INSTANCE,
                /* tokenToRetrieveAssociatedRace */ this, receiverTypes)) {
            receivers.add(r);
        }
        addListenersForStoredDataAndStartController(receivers);
        Race tractracRace = getEvent().getRaceList().iterator().next();
        // now we expect that there is no 
        assertNull(domainFactory.getExistingRaceDefinitionForRace(tractracRace));
        race = getDomainFactory().getRaceDefinition(tractracRace);
        assertNotNull(race);
        synchronized (getSemaphor()) {
            while (!isStoredDataLoaded()) {
                getSemaphor().wait();
            }
        }
        for (Receiver receiver : receivers) {
            receiver.stopAfterProcessingQueuedEvents();
            receiver.join();
        }
        trackedRace = getTrackedEvent().getTrackedRace(race);
    }


    private void setStoredDataLoaded(boolean storedDataLoaded) {
        this.storedDataLoaded = storedDataLoaded;
    }


    protected void setUpWithoutLaunchingController(String raceId) throws FileNotFoundException, MalformedURLException,
            URISyntaxException {
        super.setUp(new URL("http://germanmaster.traclive.dk/events/event_20110609_KielerWoch/clientparams.php?event=event_20110609_KielerWoch&race="+raceId),
                tractracTunnel ? new URI("tcp://"+tractracTunnelHost+":4412") : new URI("tcp://germanmaster.traclive.dk:4400"),
                        tractracTunnel ? new URI("tcp://"+tractracTunnelHost+":4413") : new URI("tcp://germanmaster.traclive.dk:4401"));
        domainFactory = new DomainFactoryImpl();
        domainEvent = domainFactory.createEvent(getEvent());
        trackedEvent = domainFactory.getOrCreateTrackedEvent(domainEvent);
    }
    
    protected Competitor getCompetitorByName(String nameRegexp) {
        Pattern p = Pattern.compile(nameRegexp);
        for (Competitor c : getTrackedRace().getRace().getCompetitors()) {
            if (p.matcher(c.getName()).matches()) {
                return c;
            }
        }
        return null;
    }

    protected RaceDefinition getRace() {
        return race;
    }

    protected DynamicTrackedRace getTrackedRace() {
        return trackedRace;
    }

    /**
     * When all stored data has been transmitted, notify the semaphor for tests to start processing
     */
    @Override
    public void storedDataEnd() {
        super.storedDataEnd();
        synchronized (semaphor) {
            storedDataLoaded = true;
            semaphor.notifyAll();
        }
    }

    protected String getExpectedEventName() {
        return "Kieler Woche";
    }

    protected DomainFactoryImpl getDomainFactory() {
        return domainFactory;
    }

    protected Event getDomainEvent() {
        return domainEvent;
    }

    protected DynamicTrackedEvent getTrackedEvent() {
        return trackedEvent;
    }

    protected Object getSemaphor() {
        return semaphor;
    }

    protected boolean isStoredDataLoaded() {
        return storedDataLoaded;
    }
    
}
