package com.sap.sailing.domain.swisstimingreplayadapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import static org.junit.Assert.assertNull;

public class SwissTimingRaceConfigurationTest {
    
    private static final String JSON_URL = "/2012_OSG.json";
    
    @Test
    public void testLoadConfiguartions() throws IOException, ParseException, org.json.simple.parser.ParseException {
        
        InetSocketAddress proxyAddress = new InetSocketAddress("proxy", 8080);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
        
        InputStream inputStream = getClass().getResourceAsStream(JSON_URL);
        List<SwissTimingReplayRace> races = SwissTimingReplayService.parseJSONObject(inputStream , JSON_URL);
        
        Map<String, SwissTimingRaceConfig> configsById = new HashMap<String, SwissTimingRaceConfig>();
        for (SwissTimingReplayRace race : races) {
            URL configFileURL = new URL(MessageFormat.format(SwissTimingReplayService.RACE_CONFIG_URL_TEMPLATE, race.race_id));
            URLConnection connection = configFileURL.openConnection(proxy);
            InputStream configDataStream = connection.getInputStream();
            SwissTimingRaceConfig raceConfig = SwissTimingReplayService.loadRaceConfig(configDataStream);
            configsById.put(race.race_id, raceConfig);
        }
    }
    
    @Test
    public void testRaceConfig_446483_no_detail_config() throws Exception {

        InetSocketAddress proxyAddress = new InetSocketAddress("proxy", 8080);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);

        URL configFileURL = new URL(MessageFormat.format(SwissTimingReplayService.RACE_CONFIG_URL_TEMPLATE, "446483"));
        URLConnection connection = configFileURL.openConnection(proxy);
        InputStream configDataStream = connection.getInputStream();

        SwissTimingRaceConfig config_446483 = SwissTimingReplayService.loadRaceConfig(configDataStream);
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

        InetSocketAddress proxyAddress = new InetSocketAddress("proxy", 8080);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);

        URL configFileURL = new URL(MessageFormat.format(SwissTimingReplayService.RACE_CONFIG_URL_TEMPLATE, "6260"));
        URLConnection connection = configFileURL.openConnection(proxy);
        InputStream configDataStream = connection.getInputStream();

        SwissTimingRaceConfig config_446483 = SwissTimingReplayService.loadRaceConfig(configDataStream);
        assertNotNull(config_446483);
        assertEquals("GB", config_446483.country_code);
        assertEquals("London 2012 Olympic Games", config_446483.event_name);
        assertEquals("60", config_446483.gmt_offset);
        assertEquals("50,603424", config_446483.latitude);
        assertEquals("Nothe", config_446483.location);
        assertEquals("-2,442963", config_446483.longitude);
        assertEquals("1343914800000", config_446483.race_start_ts);

    }

}
