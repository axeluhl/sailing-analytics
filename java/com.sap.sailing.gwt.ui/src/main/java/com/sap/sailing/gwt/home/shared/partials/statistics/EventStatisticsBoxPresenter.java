package com.sap.sailing.gwt.home.shared.partials.statistics;

import static com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxConstants.ICON_COMPATITORS_COUNT;
import static com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxConstants.ICON_FASTEST_SAILOR;
import static com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxConstants.ICON_MAX_SPEED;
import static com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxConstants.ICON_RACES_COUNT;
import static com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxConstants.ICON_RAW_GPS_FIX;
import static com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxConstants.ICON_REGATTAS_FOUGHT;
import static com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxConstants.ICON_SUM_MILES;
import static com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxConstants.ICON_TRACKED_COUNT;
import static com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxConstants.ICON_WIND_FIX;

import com.sap.sailing.gwt.home.communication.event.statistics.EventStatisticsDTO;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EventStatisticsBoxPresenter extends AbstractStatisticsBoxPresenter implements RefreshableWidget<EventStatisticsDTO> {
    
    private static final StringMessages MSG = StringMessages.INSTANCE;
    
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
        addItemWithCompactFormat(ICON_SUM_MILES, MSG.sailedMiles(), statistics.getTotalDistanceTraveled() == null ? null : statistics.getTotalDistanceTraveled().getSeaMiles());
        addCompetitorItem(ICON_FASTEST_SAILOR, MSG.fastestSailor(), statistics.getFastestCompetitor());
        addKnotsItem(ICON_MAX_SPEED, MSG.highestSpeed(), statistics.getFastestCompetitorSpeedInKnots());
    }
}
