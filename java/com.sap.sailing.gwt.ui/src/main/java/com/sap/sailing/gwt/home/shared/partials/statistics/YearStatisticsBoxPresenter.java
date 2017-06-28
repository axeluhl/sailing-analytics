package com.sap.sailing.gwt.home.shared.partials.statistics;

import com.sap.sailing.gwt.home.communication.eventlist.EventListYearDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class YearStatisticsBoxPresenter extends AbstractStatisticsBoxPresenter {
    
    private static final StringMessages MSG = StringMessages.INSTANCE;
    public static final String ICON_REGATTAS_FOUGHT = "images/mobile/icon_regattasFought.svg";
    public static final String ICON_COMPATITORS_COUNT = "images/mobile/icon_averageSpeed.svg";
    public static final String ICON_RACES_COUNT = "images/mobile/icon_racesCount.svg";
    public static final String ICON_TRACKED_COUNT = "images/mobile/icon_trackedCount.svg";
    public static final String ICON_FASTEST_SAILOR = "images/mobile/fastest_sailor.svg";
    public static final String ICON_RAW_GPS_FIX = "images/mobile/raw_gps_fixes.svg";
    public static final String ICON_WIND_FIX = "images/mobile/strongest_wind.svg";
    public static final String ICON_SUM_MILES = "images/mobile/sum_miles.svg";
    
    public YearStatisticsBoxPresenter(StatisticsBoxView view) {
        super(view);
    }

    public void setData(EventListYearDTO statistics) {
        clear();
        addItem(ICON_REGATTAS_FOUGHT, MSG.regattas(), statistics.getNumberOfRegattas());
        addItem(ICON_COMPATITORS_COUNT, MSG.competitors(), statistics.getNumberOfCompetitors());
        addItem(ICON_RACES_COUNT, MSG.races(), statistics.getNumberOfRaces());
        addItem(ICON_TRACKED_COUNT, MSG.trackedRaces(), statistics.getNumberOfTrackedRaces());
        addItemWithCompactFormat(ICON_RAW_GPS_FIX, MSG.numberOfGPSFixes(), statistics.getNumberOfGPSFixes());
        addItemWithCompactFormat(ICON_WIND_FIX, MSG.numberWindFixes(), statistics.getNumberOfWindFixes());
        addItemWithCompactFormat(ICON_SUM_MILES, MSG.sailedMiles(), statistics.getSailedMiles());
    }
}
