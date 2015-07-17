package com.sap.sailing.gwt.home.client.place.event.partials.racelist;

import java.util.Collection;
import java.util.Date;

import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.wind.AbstractWindDTO;
import com.sap.sse.common.Duration;

public class RaceListDataUtil {
    
    private interface ValueProvider<T, V> {
        V getValue(T object);
    }
    
    private static <T extends RaceMetadataDTO<?>> boolean hasValues(Collection<T> races, ValueProvider<T, ?> valueProvider) {
        for (T race : races) {
            if (valueProvider.getValue(race) != null) {
                return true;
            }
        }
        return false;
    }
    
    private static <T extends RaceMetadataDTO<?>> boolean hasNumberValues(Collection<T> races, ValueProvider<T, Number> valueProvider) {
        for (T race : races) {
            Number value = valueProvider.getValue(race);
            if (value != null && value.longValue() > 0) {
                return true;
            }
        }
        return false;
    }
    
    private static <T extends RaceMetadataDTO<?>> boolean hasDifferentValues(Collection<T> races, ValueProvider<T, ?> valueProvider) {
        Object compareValue = null;
        for (T race : races) {
            Object currentValue = valueProvider.getValue(race);
            if (currentValue == null) {
                continue;
            } else if (compareValue == null) {
                compareValue = currentValue;
            } else if (!compareValue.equals(currentValue)) {
                return true;
            }
        }
        return false;
    }
    
    public static <T extends RaceMetadataDTO<?>> boolean hasFleets(Collection<T> data) {
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
    
    public static <T extends RaceMetadataDTO<?>> boolean hasWind(Collection<T> data) {
        return hasValues(data, new ValueProvider<T, AbstractWindDTO>() {
            @Override
            public AbstractWindDTO getValue(T object) {
                return object.getWind();
            }
        });
    }
    
    public static <T extends RaceListRaceDTO> boolean hasWindSources(Collection<T> data) {
        return hasNumberValues(data, new ValueProvider<T, Number>() {
            @Override
            public Number getValue(T object) {
                return object.getWindSourcesCount();
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
    
    public static <T extends RaceMetadataDTO<?>> boolean hasCourses(Collection<T> data) {
        return hasValues(data, new ValueProvider<T, String>() {
            @Override
            public String getValue(T object) {
                return object.getCourse();
            }
        });
    }
    
    public static <T extends RaceMetadataDTO<?>> boolean hasCourseAreas(Collection<T> data) {
        return hasValues(data, new ValueProvider<T, String>() {
            @Override
            public String getValue(T object) {
                return object.getCourseArea();
            }
        });
    }
    
    public static <T extends RaceListRaceDTO> boolean hasWinner(Collection<T> data) {
        return hasValues(data, new ValueProvider<T, SimpleCompetitorDTO>() {
            @Override
            public SimpleCompetitorDTO getValue(T object) {
                return object.getWinner();
            }
        });
    }
    
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    public static <T extends RaceMetadataDTO<?>> boolean hasDifferentStartDates(Collection<T> data) {
        return hasDifferentValues(data, new ValueProvider<T, Long>() {
            @Override
            public Long getValue(T object) {
                Date start = object.getStart();
                if(start == null) {
                    return null;
                }
                return start.getTime() / DAY_IN_MILLIS;
            }
        });
    }
}
