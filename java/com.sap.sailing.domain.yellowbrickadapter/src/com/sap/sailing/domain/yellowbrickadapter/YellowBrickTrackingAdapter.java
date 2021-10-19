package com.sap.sailing.domain.yellowbrickadapter;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.yellowbrickadapter.impl.PositionsDocument;
import com.sap.sailing.server.interfaces.RacingEventService;

public interface YellowBrickTrackingAdapter {
    Iterable<YellowBrickConfiguration> getYellowBrickConfigurations();
    
    YellowBrickConfiguration createYellowBrickConfiguration(String name, String raceUrl, String username,
            String password, String creatorName);
    
    void addYellowBrickConfiguration(YellowBrickConfiguration yellowBrickConfig);
    
    void removeYellowBrickConfiguration(String raceUrl, String creatorName);

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
    YellowBrickRace getRaceMetadata(String raceUrl) throws IOException, ParseException;

    /**
     * Retrieves all data stored for the race with {@code raceUrl} so far.
     */
    PositionsDocument getStoredData(String raceUrl) throws MalformedURLException, IOException, ParseException;
}
