package com.sap.sailing.gwt.home.shared.partials.statistics;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

public interface StatisticsBoxResources extends ClientBundle {
    
    public static final StatisticsBoxResources INSTANCE = GWT.create(StatisticsBoxResources.class);

    @Source("statistics.svg")
    @MimeType("image/svg+xml")
    DataResource statistics();

    @Source("fastest_sailor.svg")
    @MimeType("image/svg+xml")
    DataResource fastestSailor();
    
    @Source("fastest_sailor_white.svg")
    @MimeType("image/svg+xml")
    DataResource fastestSailorWhite();

    @Source("icon_averageSpeed.svg")
    @MimeType("image/svg+xml")
    DataResource averageSpeed();
    
    @Source("icon_racesCount.svg")
    @MimeType("image/svg+xml")
    DataResource racesCount();
    
    @Source("icon_regattasFought.svg")
    @MimeType("image/svg+xml")
    DataResource regattasFought();
    
    @Source("icon_trackedCount.svg")
    @MimeType("image/svg+xml")
    DataResource trackedCount();

    @Source("max_speed.svg")
    @MimeType("image/svg+xml")
    DataResource maxSpeed();
    
    @Source("max_speed_white.svg")
    @MimeType("image/svg+xml")
    DataResource maxSpeedWhite();

    @Source("raw_gps_fixes.svg")
    @MimeType("image/svg+xml")
    DataResource gpsFixes();
    
    @Source("strongest_wind.svg")
    @MimeType("image/svg+xml")
    DataResource strongestWind();
    
    @Source("sum_miles.svg")
    @MimeType("image/svg+xml")
    DataResource sumMiles();

}
