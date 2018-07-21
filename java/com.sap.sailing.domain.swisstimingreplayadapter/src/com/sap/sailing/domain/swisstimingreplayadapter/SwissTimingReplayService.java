package com.sap.sailing.domain.swisstimingreplayadapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.List;

import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.SwissTimingRaceConfig;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.TrackerManager;

public interface SwissTimingReplayService {

    List<SwissTimingReplayRace> parseJSONObject(InputStream inputStream, String swissTimingUrlText) throws IOException,
            ParseException, org.json.simple.parser.ParseException;

    List<SwissTimingReplayRace> listReplayRaces(String swissTimingUrlText);

    SwissTimingRaceConfig loadRaceConfig(InputStream configDataStream) throws IOException,
            org.json.simple.parser.ParseException;

    DateFormat getStartTimeFormat();

    /**
     * @param regattaToAddTo
     *            the regatta to which the race shall be added; if <code>null</code>, a default regatta will be
     *            created/used based on the {@code regattaName} and {@code boatClassName} parameter values
     * @param link
     *            the URL without the implicit "http://" prefix, as obtained, e.g., from
     *            {@link SwissTimingReplayRace#getLink()}.
     * @param raceID
     *            the SwissTiming ID for the race
     * @param useInternalMarkPassingAlgorithm
     *            use our own instead of the SwissTiming-provided mark rounding / split times
     * @param boatClassNameName
     *            only required if {@code regattaToAddTo} is {@code null}; used for the retrieval/creation of a default
     *            regatta
     */
    void loadRaceData(RegattaIdentifier regattaToAddTo, String link, String raceName, String raceID,
            String boatClassName, TrackerManager trackerManager, TrackedRegattaRegistry trackedRegattaRegistry,
            boolean useInternalMarkPassingAlgorithm, RaceLogStore raceLogStore, RegattaLogStore regattaLogStore)
            throws MalformedURLException, FileNotFoundException, URISyntaxException, Exception;

    /**
     * @param link
     *            the URL without the implicit "http://" prefix, as obtained, e.g., from
     *            {@link SwissTimingReplayRace#getLink()}, or with an explicit protocol specification
     *            as in "file:///..."
     * @param replayListener
     *            the listener to receive all persing events
     */
    void loadRaceData(String link, SwissTimingReplayListener listener);

}
