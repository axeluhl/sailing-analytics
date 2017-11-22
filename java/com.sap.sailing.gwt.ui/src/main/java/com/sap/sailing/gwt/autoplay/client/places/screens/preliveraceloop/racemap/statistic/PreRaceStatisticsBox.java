package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.statistic;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class PreRaceStatisticsBox extends Composite {
    
    public static final String ICON_COMPETITORS = "images/mobile/icon-competitors.svg";
    public static final String ICON_LEGS = "images/mobile/icon-legs.svg";
    public static final String ICON_LENGTH = "images/mobile/icon-length.svg";
    public static final String ICON_RACEVIEWER = "images/mobile/icon-raceviewer.svg";
    public static final String ICON_TIME = "images/mobile/icon-time.svg";
    public static final String ICON_WIND = "images/mobile/icon-wind.svg";

    private static StatisticsBoxUiBinder uiBinder = GWT.create(StatisticsBoxUiBinder.class);

    interface StatisticsBoxUiBinder extends UiBinder<Widget, PreRaceStatisticsBox> {
    }
    
    @UiField UListElement itemContainerUi;
    
    public PreRaceStatisticsBox() {
        PreRaceStatisticsBoxResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void clear() {
        setVisible(true);
        itemContainerUi.removeAllChildren();
    }
    
    public void addItem(String iconUrl, String name, Object payload) {
        itemContainerUi.appendChild(new PreRaceStatisticsBoxItem(iconUrl, name, payload).getElement());
    }

    public void addQRItem(String iconUrl, String name, String url) {
        itemContainerUi.appendChild(new PreRaceStatisticsBoxQR(iconUrl, name, url).getElement());
    }
}
