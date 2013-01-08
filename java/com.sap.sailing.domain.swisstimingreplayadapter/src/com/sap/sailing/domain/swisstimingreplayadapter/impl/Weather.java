package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import java.util.HashMap;
import java.util.Map;

public enum Weather {

    blizzard((short) 209),
    cloudy((short) 186),
    sky_clear((short) 57),
    fog((short) 127),
    hail((short) 21),
    haze((short) 0),
    heavy_rain((short) 122),
    lightning((short) 160),
    light_rain((short)191),
    light_snow((short)38),
    limited_visibility((short)61),
    mostly_cloudy((short)219),
    moderate_rain((short)112),
    overcast((short)180),
    partly_cloudy((short)210),
    rain((short)27),
    rain_snow((short)33),
    showers((short)240),
    snow((short)68),
    sunny((short)128),
    torrential_rain((short)177),
    tropical_cyclone((short)165),
    thunderstorm((short)247),
    unstable((short)153),
    windy((short)244),
    wind_and_rain((short)28),
    wind_and_snow((short)4);

    private static final Map<Short, Weather> byCode;

    static {
        byCode = new HashMap<Short, Weather>();
        for (Weather weather : Weather.values()) {
            byCode.put(weather.code, weather);
        }
    }

    private final short code;
    
    private Weather(short code) {
        this.code = code;
    }

    public static Weather byCode(short code) {
        return byCode.get(code);
    }

}
