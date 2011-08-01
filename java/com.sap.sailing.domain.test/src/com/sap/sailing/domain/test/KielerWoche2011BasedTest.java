package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

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
 * Connects to the Kieler Woche 2011 TracTrac data of the 505 Race 2. Subclasses have to implement a @Before method
 * which calls {@link #setUp(ReceiverType[])} with a useful set of receiver types. When all stored data has been
 * received, the {@link #getSemaphor() semaphor} is notified. Therefore, a typical pattern for subclasses should be
 * to invoke {@link #setUp(ReceiverType[])}, then wait on the semaphor before starting with test processing.
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

    protected KielerWoche2011BasedTest(String raceId) throws MalformedURLException, URISyntaxException {
        super(new URL("http://germanmaster.traclive.dk/events/event_20110609_KielerWoch/clientparams.php?event=event_20110609_KielerWoch&race="+raceId),
                tractracTunnel ? new URI("tcp://"+tractracTunnelHost+":4412") : new URI("tcp://germanmaster.traclive.dk:4400"),
                        tractracTunnel ? new URI("tcp://"+tractracTunnelHost+":4413") : new URI("tcp://germanmaster.traclive.dk:4401"));
    }

    protected void setUp(ReceiverType[] receiverTypes) throws MalformedURLException, IOException, InterruptedException {
        super.setUp();
        domainFactory = new DomainFactoryImpl();
        domainEvent = domainFactory.createEvent(getEvent());
        trackedEvent = domainFactory.getOrCreateTrackedEvent(domainEvent);
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
        trackedRace = getTrackedEvent().getTrackedRace(race);
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
