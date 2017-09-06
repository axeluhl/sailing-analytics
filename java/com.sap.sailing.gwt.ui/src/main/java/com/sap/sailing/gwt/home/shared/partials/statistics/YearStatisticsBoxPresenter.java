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

import com.sap.sailing.gwt.home.communication.eventlist.EventListYearDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class YearStatisticsBoxPresenter extends AbstractStatisticsBoxPresenter {
    
    private static final StringMessages MSG = StringMessages.INSTANCE;
    
    public YearStatisticsBoxPresenter(StatisticsBoxView view) {
        super(view);
    }

    public void setData(EventListYearDTO statistics) {
        clear();
        addItem(ICON_REGATTAS_FOUGHT, MSG.regattas(), statistics.getNumberOfRegattas());
        addItemIfNotNull(ICON_COMPATITORS_COUNT, MSG.competitors(), statistics.getNumberOfCompetitors());
        addItem(ICON_RACES_COUNT, MSG.races(), statistics.getNumberOfRaces());
        addItem(ICON_TRACKED_COUNT, MSG.trackedRaces(), statistics.getNumberOfTrackedRaces());
        addItemWithCompactFormat(ICON_RAW_GPS_FIX, MSG.numberOfGPSFixes(), statistics.getNumberOfGPSFixes());
        addItemWithCompactFormat(ICON_WIND_FIX, MSG.numberWindFixes(), statistics.getNumberOfWindFixes());
        addItemWithCompactFormat(ICON_SUM_MILES, MSG.sailedMiles(), statistics.getDistanceTraveled().getNauticalMiles());
        addCompetitorItem(ICON_FASTEST_SAILOR, MSG.fastestSailor(), statistics.getFastestCompetitor());
        addKnotsItem(ICON_MAX_SPEED, MSG.highestSpeed(), statistics.getFastestCompetitorSpeedInKnots());
    }
}
