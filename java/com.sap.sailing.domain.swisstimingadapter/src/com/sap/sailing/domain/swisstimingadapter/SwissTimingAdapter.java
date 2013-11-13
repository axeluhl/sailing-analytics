package com.sap.sailing.domain.swisstimingadapter;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.List;

import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackerManager;

public interface SwissTimingAdapter {
    List<com.sap.sailing.domain.swisstimingadapter.RaceRecord> getSwissTimingRaceRecords(String hostname, int port,
            boolean canSendRequests) throws InterruptedException, UnknownHostException, IOException, ParseException;

    /**
     * @param trackerManager TODO
     * @param regattaToAddTo
     *            if <code>null</code>, an existing regatta by the name of the TracTrac event with the boat class name
     *            appended in parentheses will be looked up; if not found, a default regatta with that name will be
     *            created, with a single default series and a single default fleet. If a valid {@link RegattaIdentifier}
     *            is specified, a regatta lookup is performed with that identifier; if the regatta is found, it is used
     *            to add the races to. Otherwise, a default regatta as described above will be created and used.
     */
    RacesHandle addSwissTimingRace(TrackerManager trackerManager, RegattaIdentifier regattaToAddTo, String raceID, String hostname,
            int port, boolean canSendRequests, RaceLogStore logStore, long timeoutInMilliseconds)
            throws InterruptedException, UnknownHostException, IOException, ParseException, Exception;

    SwissTimingFactory getSwissTimingFactory();

    void storeSwissTimingDummyRace(String racMessage, String stlMesssage, String ccgMessage) throws IllegalArgumentException;

    com.sap.sailing.domain.swisstimingadapter.DomainFactory getSwissTimingDomainFactory();

}
