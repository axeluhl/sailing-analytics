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
    
    private static StatisticsBoxUiBinder uiBinder = GWT.create(StatisticsBoxUiBinder.class);

    interface StatisticsBoxUiBinder extends UiBinder<Widget, StatisticsBox> {
    }

    @UiField MobileSection itemContainerUi;
    
    public StatisticsBox() {
        StatisticsBoxResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void addItem(String iconUrl, String name, Integer count) {
        itemContainerUi.addContent(new StatisticsBoxItem(iconUrl, name, count));
    }

    @Override
    public void setData(EventStatisticsDTO statistics, long nextUpdate, int updateNo) {
        itemContainerUi.clearContent();
        addItem(StatisticsBox.ICON_REGATTAS_FOUGHT, MSG.regattas(), statistics.getRegattasFoughtCount());
        addItem(StatisticsBox.ICON_COMPATITORS_COUNT, MSG.competitors(), statistics.getCompetitorsCount());
        addItem(StatisticsBox.ICON_RACES_COUNT, MSG.races(), statistics.getRacesRunCount());
        addItem(StatisticsBox.ICON_TRACKED_COUNT, MSG.trackedRaces(), statistics.getTrackedRacesCount());
    }

}
