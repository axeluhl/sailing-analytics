package com.sap.sailing.domain.yellowbrickadapter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.yellowbrickadapter.impl.PositionsDocument;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.TimePoint;

public interface YellowBrickTrackingAdapter {
    static String NAME = "YellowBrick";
    static String YELLOWBRICK_PREFIX = "YB-";
    
    static String getBoatId(String ybBoatName) {
        return YELLOWBRICK_PREFIX + ybBoatName;
    }
    
    static String getCompetitorId(String ybBoatName, String raceUrl) {
        return YELLOWBRICK_PREFIX+raceUrl+"-"+ybBoatName;
    }

    Iterable<YellowBrickConfiguration> getYellowBrickConfigurations();
    
    YellowBrickConfiguration createYellowBrickConfiguration(String name, String raceUrl, String username,
            String password, String creatorName);
    
    void addYellowBrickConfiguration(YellowBrickConfiguration yellowBrickConfig);
    
    void removeYellowBrickConfiguration(String raceUrl, String creatorName);

    /**
     * @param password
     *            if {@code null} and the configuration already exists within this adapter (as identified by the
     *            {@code creatorName} and {@code raceUrl}), the existing password is left unchanged.
     */
    void updateYellowBrickConfiguration(String name, String raceUrl, String username,
            String password, String creatorName);
    
    void addYellowBrickConfigurationListener(YellowBrickConfigurationListener listener);

    void removeYellowBrickConfigurationListener(YellowBrickConfigurationListener listener);
    
    void addYellowBrickRace(RacingEventService service, RegattaIdentifier regattaToAddTo, String yellowBrickRaceUrl,
            RaceLogStore raceLogStore, RegattaLogStore regattaLogStore, String yellowBrickUsername,
            String yellowBrickPassword, boolean trackWind, boolean correctWindByDeclination) throws Exception;

    /**
     * Obtains the meta-data for the YellowBrick race identified by URL {@code raceUrl}
     */
    YellowBrickRace getRaceMetadata(String raceUrl, Optional<String> username, Optional<String> password) throws IOException, ParseException;

    /**
     * Retrieves all data stored for the race with {@code raceUrl} so far.
     */
    PositionsDocument getStoredData(String raceUrl, Optional<String> username, Optional<String> password) throws MalformedURLException, IOException, ParseException;

    PositionsDocument getPositionsSince(String raceUrl, TimePoint since, Optional<String> username,
            Optional<String> password) throws MalformedURLException, IOException, ParseException;
}
