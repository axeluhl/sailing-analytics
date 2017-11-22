package com.sap.sailing.gwt.home.shared.partials.statistics;

import com.sap.sailing.gwt.home.communication.eventlist.EventListYearDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class YearStatisticsBoxPresenter extends AbstractStatisticsBoxPresenter {
    
    private static final StringMessages MSG = StringMessages.INSTANCE;
    
    public YearStatisticsBoxPresenter(StatisticsBoxView view) {
        super(view);
    }

    public void setData(EventListYearDTO statistics) {
        clear();
        addItem(StatisticsBoxResources.INSTANCE.regattasFought(), MSG.regattas(), statistics.getNumberOfRegattas());
        addItemIfNotNull(StatisticsBoxResources.INSTANCE.averageSpeed(), MSG.competitors(), statistics.getNumberOfCompetitors());
        addItem(StatisticsBoxResources.INSTANCE.racesCount(), MSG.races(), statistics.getNumberOfRaces());
        addItem(StatisticsBoxResources.INSTANCE.trackedCount(), MSG.trackedRaces(), statistics.getNumberOfTrackedRaces());
        addItemWithCompactFormat(StatisticsBoxResources.INSTANCE.gpsFixes(), MSG.numberOfGPSFixes(), statistics.getNumberOfGPSFixes());
        addItemWithCompactFormat(StatisticsBoxResources.INSTANCE.strongestWind(), MSG.numberWindFixes(), statistics.getNumberOfWindFixes());
        addItemWithCompactFormat(StatisticsBoxResources.INSTANCE.sumMiles(), MSG.sailedMiles(), statistics.getDistanceTraveled().getNauticalMiles());
        addCompetitorItem(StatisticsBoxResources.INSTANCE.fastestSailor(), MSG.fastestSailor(), statistics.getFastestCompetitor());
        addKnotsItem(StatisticsBoxResources.INSTANCE.maxSpeed(), MSG.highestSpeed(), statistics.getFastestCompetitorSpeedInKnots());
    }
}
