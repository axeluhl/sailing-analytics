package com.sap.sailing.gwt.autoplay.client.configs;

import com.sap.sailing.gwt.autoplay.client.configs.impl.AutoPlayClassicConfiguration;
import com.sap.sailing.gwt.autoplay.client.configs.impl.AutoPlaySixtyInchConfiguration;

public enum AutoPlayType {
    CLASSIC("Classic Autoplay", new AutoPlayClassicConfiguration()), SIXTYINCH("Sixty Inch Autoplay",
            new AutoPlaySixtyInchConfiguration());
    private final String name;
    private final AutoPlayConfiguration config;

    AutoPlayType(String name, AutoPlayConfiguration config) {
        this.name = name;
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public AutoPlayConfiguration getConfig() {
        return config;
    }
}
