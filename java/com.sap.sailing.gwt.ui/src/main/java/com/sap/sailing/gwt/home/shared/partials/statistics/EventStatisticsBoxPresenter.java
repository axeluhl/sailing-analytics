package com.sap.sailing.gwt.home.shared.partials.statistics;

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
            addItem(StatisticsBoxResources.INSTANCE.regattasFought(), MSG.regattas(), statistics.getRegattasFoughtCount());
        }
        addItemIfNotNull(StatisticsBoxResources.INSTANCE.averageSpeed(), MSG.competitors(), statistics.getCompetitorsCount());
        addItem(StatisticsBoxResources.INSTANCE.racesCount(), MSG.races(), statistics.getRacesRunCount());
        addItem(StatisticsBoxResources.INSTANCE.trackedCount(), MSG.trackedRaces(), statistics.getTrackedRacesCount());
        addItemWithCompactFormat(StatisticsBoxResources.INSTANCE.gpsFixes(), MSG.numberOfGPSFixes(), statistics.getNumberOfGPSFixes());
        addItemWithCompactFormat(StatisticsBoxResources.INSTANCE.strongestWind(), MSG.numberWindFixes(), statistics.getNumberOfWindFixes());
        addItemWithCompactFormat(StatisticsBoxResources.INSTANCE.sumMiles(), MSG.sailedMiles(), statistics.getTotalDistanceTraveled() == null ? null : statistics.getTotalDistanceTraveled().getSeaMiles());
        addCompetitorItem(StatisticsBoxResources.INSTANCE.fastestSailor(), MSG.fastestSailor(), statistics.getFastestCompetitor());
        addKnotsItem(StatisticsBoxResources.INSTANCE.maxSpeed(), MSG.highestSpeed(), statistics.getFastestCompetitorSpeedInKnots());
    }
}
