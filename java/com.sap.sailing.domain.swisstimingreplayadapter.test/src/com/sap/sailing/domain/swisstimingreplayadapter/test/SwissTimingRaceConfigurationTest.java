package com.sap.sailing.domain.swisstimingreplayadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayRace;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.SwissTimingRaceConfig;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.SwissTimingReplayServiceImpl;

public class SwissTimingRaceConfigurationTest {
    
    private static final String JSON_URL = "/2012_OSG.json";
    
    @Ignore("Takes a very long time; only used to see if we can parse all configurations")
    @Test
    public void testLoadConfigurations() throws IOException, ParseException, org.json.simple.parser.ParseException {
        InputStream inputStream = getClass().getResourceAsStream(JSON_URL);
        List<SwissTimingReplayRace> races = new SwissTimingReplayServiceImpl(DomainFactory.INSTANCE).parseJSONObject(inputStream , JSON_URL);
        Map<String, SwissTimingRaceConfig> configsById = new HashMap<String, SwissTimingRaceConfig>();
        for (SwissTimingReplayRace race : races) {
            URL configFileURL = new URL(MessageFormat.format(SwissTimingReplayServiceImpl.RACE_CONFIG_URL_TEMPLATE, race.getRaceId()));
            URLConnection connection = configFileURL.openConnection();
            InputStream configDataStream = connection.getInputStream();
            SwissTimingRaceConfig raceConfig = new SwissTimingReplayServiceImpl(DomainFactory.INSTANCE).loadRaceConfig(configDataStream);
            configsById.put(race.getRaceId(), raceConfig);
        }
    }
    
    @Test
    public void testRaceConfig_446483_no_detail_config() throws Exception {
        URL configFileURL = new URL(MessageFormat.format(SwissTimingReplayServiceImpl.RACE_CONFIG_URL_TEMPLATE, "446483"));
        URLConnection connection = configFileURL.openConnection();
        InputStream configDataStream = connection.getInputStream();
        SwissTimingRaceConfig config_446483 = new SwissTimingReplayServiceImpl(DomainFactory.INSTANCE).loadRaceConfig(configDataStream);
        assertNotNull(config_446483);
        assertEquals("GB", config_446483.country_code);
        assertNull(config_446483.event_name);
        assertEquals("60", config_446483.gmt_offset);
        assertEquals("50,603424", config_446483.latitude);
        assertEquals("Nothe", config_446483.location);
        assertEquals("-2,442963", config_446483.longitude);
        assertNull(config_446483.race_start_ts);

    }

    @Test
    public void testRaceConfig_6260_with_detail_config() throws Exception {
        URL configFileURL = new URL(MessageFormat.format(SwissTimingReplayServiceImpl.RACE_CONFIG_URL_TEMPLATE, "6260"));
        URLConnection connection = configFileURL.openConnection();
        InputStream configDataStream = connection.getInputStream();
        SwissTimingRaceConfig config_446483 = new SwissTimingReplayServiceImpl(DomainFactory.INSTANCE).loadRaceConfig(configDataStream);
        assertNotNull(config_446483);
        assertEquals("GB", config_446483.country_code);
        // TODO There used to be an attribute event_name in the "config" sub-element; around 2017-07-25 these things seem to have vanished;
        // Asked Radek Masnica <masnica.r@st-software.com> and Christian Sgodzay <Sgodzay.C@st-sportservice.com> about the changes.
        // Hopefully this can be re-enabled soon...
        // assertEquals("London 2012 Olympic Games", config_446483.event_name);
        // assertEquals("1343914800000", config_446483.race_start_ts);
        assertEquals("60", config_446483.gmt_offset);
        assertEquals("50,603424", config_446483.latitude);
        assertEquals("Nothe", config_446483.location);
        assertEquals("-2,442963", config_446483.longitude);
    }

}
