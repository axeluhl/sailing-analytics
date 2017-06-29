package com.sap.sailing.gwt.home.shared.partials.statistics;

import com.sap.sailing.gwt.home.communication.event.statistics.EventStatisticsDTO;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EventStatisticsBoxPresenter extends AbstractStatisticsBoxPresenter implements RefreshableWidget<EventStatisticsDTO> {
    
    private static final StringMessages MSG = StringMessages.INSTANCE;
    public static final String ICON_REGATTAS_FOUGHT = "images/mobile/icon_regattasFought.svg";
    public static final String ICON_COMPATITORS_COUNT = "images/mobile/icon_averageSpeed.svg";
    public static final String ICON_RACES_COUNT = "images/mobile/icon_racesCount.svg";
    public static final String ICON_TRACKED_COUNT = "images/mobile/icon_trackedCount.svg";
    public static final String ICON_FASTEST_SAILOR = "images/mobile/fastest_sailor.svg";
    public static final String ICON_RAW_GPS_FIX = "images/mobile/raw_gps_fixes.svg";
    public static final String ICON_WIND_FIX = "images/mobile/strongest_wind.svg";
    public static final String ICON_SUM_MILES = "images/mobile/sum_miles.svg";
    
    private final boolean showRegattaInformation;

    public EventStatisticsBoxPresenter(boolean showRegattaInformation, StatisticsBoxView view) {
        super(view);
        this.showRegattaInformation = showRegattaInformation;
    }

    @Override
    public void setData(EventStatisticsDTO statistics) {
        clear();
        if (showRegattaInformation) {
            addItem(ICON_REGATTAS_FOUGHT, MSG.regattas(), statistics.getRegattasFoughtCount());
        }
        addItemIfNotNull(ICON_COMPATITORS_COUNT, MSG.competitors(), statistics.getCompetitorsCount());
        addItem(ICON_RACES_COUNT, MSG.races(), statistics.getRacesRunCount());
        addItem(ICON_TRACKED_COUNT, MSG.trackedRaces(), statistics.getTrackedRacesCount());
        addItemWithCompactFormat(ICON_RAW_GPS_FIX, MSG.numberOfGPSFixes(), statistics.getNumberOfGPSFixes());
        addItemWithCompactFormat(ICON_WIND_FIX, MSG.numberWindFixes(), statistics.getNumberOfWindFixes());
        addItemWithCompactFormat(ICON_SUM_MILES, MSG.sailedMiles(), statistics.getTotalDistanceTraveled());
        addItemWithCompactFormat(ICON_FASTEST_SAILOR, MSG.fastestSailor() + ": "
                + statistics.getCompetitorInfo(), statistics.getCompetitorSpeed());
    }
}
