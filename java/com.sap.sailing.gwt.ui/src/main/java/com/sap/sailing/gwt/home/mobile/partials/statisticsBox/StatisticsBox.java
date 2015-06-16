package com.sap.sailing.gwt.home.mobile.partials.statisticsBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class StatisticsBox extends Composite {
    
    public static final String ICON_REGATTAS_FOUGHT = "images/mobile/icon_regattasFought.svg";
    public static final String ICON_COMPATITORS_COUNT = "images/mobile/icon_averageSpeed.svg";
    public static final String ICON_RACES_COUNT = "images/mobile/icon_racesCount.svg";
    public static final String ICON_TRACKED_COUNT = "images/mobile/icon_trackedCount.svg";
    
    private static StatisticsBoxUiBinder uiBinder = GWT.create(StatisticsBoxUiBinder.class);

    interface StatisticsBoxUiBinder extends UiBinder<Widget, StatisticsBox> {
    }

    @UiField FlowPanel itemContainerUi;
    
    public StatisticsBox() {
        StatisticsBoxResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void addItem(String iconUrl, String name, Integer count) {
        itemContainerUi.add(new StatisticsBoxItem(iconUrl, name, count));
    }

}
