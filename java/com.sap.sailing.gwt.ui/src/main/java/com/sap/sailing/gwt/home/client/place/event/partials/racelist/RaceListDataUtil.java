package com.sap.sailing.gwt.home.client.place.event.partials.racelist;

import java.util.Collection;

import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListRaceDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleWindDTO;
import com.sap.sse.common.Duration;

public class RaceListDataUtil {
    
    private interface ValueProvider<T, V> {
        V getValue(T object);
    }
    
    private static <T extends RaceMetadataDTO> boolean hasValues(Collection<T> races, ValueProvider<T, ?> valueProvider) {
        for (T race : races) {
            if (valueProvider.getValue(race) != null) {
                return true;
            }
        }
        return false;
    }
    
    private static <T extends RaceMetadataDTO> boolean hasNumberValues(Collection<T> races, ValueProvider<T, Number> valueProvider) {
        for (T race : races) {
            Number value = valueProvider.getValue(race);
            if (value != null && value.longValue() > 0) {
                return true;
            }
        }
        return false;
    }
    
    public static <T extends RaceMetadataDTO> boolean hasFleets(Collection<T> data) {
        return hasValues(data, new ValueProvider<T, FleetMetadataDTO>() {
            @Override
            public FleetMetadataDTO getValue(T object) {
                return object.getFleet();
            }
        });
    }
    
    public static <T extends RaceListRaceDTO> boolean hasDurations(Collection<T> data) {
        return hasValues(data, new ValueProvider<T, Duration>() {
            @Override
            public Duration getValue(T object) {
                return object.getDuration();
            }
        });
    }
    
    public static <T extends RaceMetadataDTO> boolean hasWind(Collection<T> data) {
        return hasValues(data, new ValueProvider<T, SimpleWindDTO>() {
            @Override
            public SimpleWindDTO getValue(T object) {
                return object.getWind();
            }
        });
    }
    
    public static <T extends RaceListRaceDTO> boolean hasWindFixes(Collection<T> data) {
        return hasNumberValues(data, new ValueProvider<T, Number>() {
            @Override
            public Number getValue(T object) {
                return object.getWindFixesCount();
            }
        });
    }
    
    public static <T extends RaceListRaceDTO> boolean hasVideos(Collection<T> data) {
        return hasNumberValues(data, new ValueProvider<T, Number>() {
            @Override
            public Number getValue(T object) {
                return object.getVideoCount();
            }
        });
    }
    
    public static <T extends RaceListRaceDTO> boolean hasAudios(Collection<T> data) {
        return hasNumberValues(data, new ValueProvider<T, Number>() {
            @Override
            public Number getValue(T object) {
                return object.getAudioCount();
            }
        });
    }
    
    public static <T extends RaceMetadataDTO> boolean hasCourses(Collection<T> data) {
        return hasValues(data, new ValueProvider<T, String>() {
            @Override
            public String getValue(T object) {
                return object.getCourse();
            }
        });
    }
    
    public static <T extends RaceMetadataDTO> boolean hasCourseAreas(Collection<T> data) {
        return hasValues(data, new ValueProvider<T, String>() {
            @Override
            public String getValue(T object) {
                return object.getCourseArea();
            }
        });
    }
}
