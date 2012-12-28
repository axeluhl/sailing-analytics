package com.sap.sailing.domain.tractracadapter.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;

/**
 * Parses and then represents the contents of a clientparams.php TracTrac document describing one race with
 * competitors, control point and route / course information. The general file format supports comments
 * starting with "//", empty lines and property lines with format <code>PropertyName:PropertyValue</code> with
 * one property per line. Some properties form groups, following a naming convention using common prefixes and
 * numbering schemes. For example, consider the following lines:<pre>
 * 
 * ControlPoint1UUID:161203dc-ffe3-11e0-9787-406186cbf87c
 * ControlPoint1Name:Luvtonne
 * ControlPoint1HasTwoPoints:0
 * ControlPoint1ShortName:W
 * ControlPoint1Mark1Lon:-122.144668
 * ControlPoint1Mark1Lat:37.396782
 * 
 * ControlPoint2UUID:1b8ec652-7874-11e0-8236-406186cbf87c
 * ControlPoint2Name:start/finish
 * ControlPoint2HasTwoPoints:1
 * ControlPoint2ShortName:s/
 * ControlPoint2Mark1Lon:10.168493
 * ControlPoint2Mark1Lat:54.430050
 * ControlPoint2Mark2Lon:10.168492
 * ControlPoint2Mark2Lat:54.430012
 * </pre>
 *
 * This describes two control points of which the second has two marks. 
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ClientParamsPHP {
    private final Map<String, String> properties;
    
    /**
     * Keys are UUIDs, values are property names for which this UUID was set. The values of this map can be used as
     * keys for {@link #properties}.
     */
    private final Map<UUID, String> propertiesByID;
    
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss Z");
    
    public static enum HandicapSystem {
        NONE,
        
        /**
         * Time on Time
         */
        ToT,
        
        /**
         * Time on Distance
         */
        ToD,
        
        /**
         * Performance Line / Performance Curve
         */
        PL
    }
    
    private class ObjectWithUUID {
        private final Pattern propertyNamePattern = Pattern.compile("([^0-9]*)(([0-9][0-9]*)(.*))?");
        private final String propertyNamePrefix;
        private final Integer number;

        public ObjectWithUUID(UUID uuid) {
            String idPropertyName = propertiesByID.get(uuid);
            Matcher m = propertyNamePattern.matcher(idPropertyName);
            if (m.matches()) {
                propertyNamePrefix = m.group(1);
                if (m.groupCount() > 1) {
                    number = Integer.valueOf(m.group(3));
                } else {
                    number = null;
                }
            } else {
                throw new RuntimeException("Unexpected ID property name "+idPropertyName+" that cannot be analyzed");
            }
        }
        
        protected String getProperty(String propertyName) {
            return properties.get(propertyNamePrefix+number+propertyName);
        }
    }
    
    public class Route extends ObjectWithUUID {
        public Route(UUID uuid) {
            super(uuid);
        }

        public Object getDescription() {
            return getProperty("Description");
        }
    }
    
    public class Image extends ObjectWithUUID {
        public Image(UUID uuid) {
            super(uuid);
        }
    }
    
    public class Team extends ObjectWithUUID {
        public Team(UUID uuid) {
            super(uuid);
        }
    }
    
    public class Competitor extends ObjectWithUUID {
        public Competitor(UUID uuid) {
            super(uuid);
        }
    }
    
    public class ControlPoint extends ObjectWithUUID {
        public ControlPoint(UUID uuid) {
            super(uuid);
        }
    }
    
    public ClientParamsPHP(Reader r) throws IOException {
        BufferedReader br = new BufferedReader(r);
        properties = new HashMap<>();
        propertiesByID = new HashMap<>();
        String line;
        while ((line = br.readLine()) != null) {
            // ignore comment lines
            if (!line.trim().startsWith("//")) {
                int colonIndex = line.indexOf(':');
                if (colonIndex >= 0) {
                    String propertyName = line.substring(0, colonIndex).trim();
                    String propertyValue = line.substring(colonIndex + 1).trim();
                    properties.put(propertyName, propertyValue);
                    if (propertyName.endsWith("ID")) {
                        try {
                            final UUID uuid = UUID.fromString(propertyValue);
                            propertiesByID.put(uuid, propertyName);
                        } catch (IllegalArgumentException e) {
                            // UUID was not parsable; can't enter it into propertiesByID
                        }
                    }
                }
            }
        }
    }
    
    public URI getLiveUri() throws URISyntaxException {
        return new URI(properties.get("live-uri"));
    }

    public URI getStoredUri() throws URISyntaxException {
        return new URI(properties.get("stored-uri"));
    }
    
    public long getLiveDelayInMillis() {
        return new Long(properties.get("LiveDelaySecs"))*1000l;
    }
    
    public String getEventName() {
        return properties.get("EventName");
    }

    /**
     * @return the event's UUID
     */
    public String getEventID() {
        return properties.get("EventID");
    }
    
    /**
     * Returns the string to be used in TracTrac URLs to identify the event
     */
    public String getEventDB() {
        return properties.get("EventDB");
    }
    
    public TimePoint getEventStartTime() throws ParseException {
        return getTimePoint("EventStartTime");
    }

    public TimePoint getEventEndTime() throws ParseException {
        return getTimePoint("EventEndTime");
    }

    private TimePoint getTimePoint(final String key) throws ParseException {
        final TimePoint result;
        final String eventEndTimeAsString = properties.get(key);
        if (eventEndTimeAsString != null && eventEndTimeAsString.length()>0) {
            result = new MillisecondsTimePoint(dateFormat.parse(eventEndTimeAsString+" UTC"));
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * @return the race UUID
     */
    public UUID getRaceID() {
        return UUID.fromString(properties.get("RaceID"));
    }
    
    public String getRaceName() {
        return properties.get("RaceName");
    }

    public TimePoint getRaceStartTime() throws ParseException {
        return getTimePoint("RaceStartTime");
    }
    
    public TimePoint getRaceEndTime() throws ParseException {
        return getTimePoint("RaceEndTime");
    }
    
    public TimePoint getRaceTrackingStartTime() throws ParseException {
        return getTimePoint("RaceTrackingStartTime");
    }
    
    public TimePoint getRaceTrackingEndTime() throws ParseException {
        return getTimePoint("RaceTrackingEndTime");
    }
    
    public Image getRaceDefaultRoute() {
        return new Image(UUID.fromString(properties.get("RaceDefaultRouteUUID")));
    }
    
    public HandicapSystem getRaceHandicapSystem() {
        return HandicapSystem.valueOf(properties.get("RaceHandicapSystem"));
    }
    
}
