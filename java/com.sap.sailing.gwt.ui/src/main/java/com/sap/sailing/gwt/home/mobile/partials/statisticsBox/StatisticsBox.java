package com.sap.sailing.gwt.home.mobile.partials.statisticsBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventStatisticsDTO;

public class StatisticsBox extends Composite implements RefreshableWidget<EventStatisticsDTO> {
    private static final StringMessages MSG = StringMessages.INSTANCE;
    public static final String ICON_REGATTAS_FOUGHT = "images/mobile/icon_regattasFought.svg";
    public static final String ICON_COMPATITORS_COUNT = "images/mobile/icon_averageSpeed.svg";
    public static final String ICON_RACES_COUNT = "images/mobile/icon_racesCount.svg";
    public static final String ICON_TRACKED_COUNT = "images/mobile/icon_trackedCount.svg";
    public static final String ICON_FASTEST_SAILOR = "images/mobile/fastest_sailor.svg";
    public static final String ICON_RAW_GPS_FIX = "images/mobile/raw_gps_fixes.svg";
    public static final String ICON_WIND_FIX = "images/mobile/strongest_wind.svg";
    public static final String ICON_SUM_MILES = "images/mobile/sum_miles.svg";
    private static StatisticsBoxUiBinder uiBinder = GWT.create(StatisticsBoxUiBinder.class);

    interface StatisticsBoxUiBinder extends UiBinder<Widget, StatisticsBox> {
    }

    @UiField
    MobileSection itemContainerUi;

    public StatisticsBox() {
        StatisticsBoxResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void addItem(String iconUrl, String name, Object payload) {
        itemContainerUi.addContent(new StatisticsBoxItem(iconUrl, name, payload));
    }

    public void addItemIfNotNull(String iconUrl, String name, Object payload) {
        if (payload != null) {
            itemContainerUi.addContent(new StatisticsBoxItem(iconUrl, name, payload));
        }
    }

    @Override
    public void setData(EventStatisticsDTO statistics, long nextUpdate, int updateNo) {
        itemContainerUi.clearContent();
        addItem(StatisticsBox.ICON_REGATTAS_FOUGHT, MSG.regattas(), statistics.getRegattasFoughtCount());
        addItem(StatisticsBox.ICON_COMPATITORS_COUNT, MSG.competitors(), statistics.getCompetitorsCount());
        addItem(StatisticsBox.ICON_RACES_COUNT, MSG.races(), statistics.getRacesRunCount());
        addItem(StatisticsBox.ICON_TRACKED_COUNT, MSG.trackedRaces(), statistics.getTrackedRacesCount());
        addItemIfNotNull(StatisticsBox.ICON_RAW_GPS_FIX, MSG.numberOfGPSFixes(), statistics.getNumberOfGPSFixes());
        addItemIfNotNull(StatisticsBox.ICON_FASTEST_SAILOR, MSG.fastestSailor() + ": "
 + statistics.getCompetitorInfo(), statistics.getCompetitorSpeed());
        addItemIfNotNull(StatisticsBox.ICON_WIND_FIX, MSG.numberWindFixes(), statistics.getNumberOfWindFixes());
        addItemIfNotNull(StatisticsBox.ICON_SUM_MILES, MSG.sailedMiles(), statistics.getTotalDistanceTraveled());
    }
}
