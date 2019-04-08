package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.statistic;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class PreRaceStatisticsBox extends Composite {

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
    
    public void addItem(DataResource icon, String name, Object payload) {
        addItem(icon.getSafeUri().asString(), name, payload);
    }
    
    public void addItem(String iconUrl, String name, Object payload) {
        itemContainerUi.appendChild(new PreRaceStatisticsBoxItem(iconUrl, name, payload).getElement());
    }

    public void addQRItem(DataResource icon, String name, String url) {
        itemContainerUi.appendChild(new PreRaceStatisticsBoxQR(icon.getSafeUri().asString(), name, url).getElement());
    }
}
