package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.statistik;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.statistics.EventStatisticsDTO;
import com.sap.sailing.gwt.home.shared.partials.statistics.AbstractStatisticsBox;

public class PreRaceStatisticsBox extends AbstractStatisticsBox {

    private static PreRaceStatisticsBoxUiBinder uiBinder = GWT.create(PreRaceStatisticsBoxUiBinder.class);

    interface PreRaceStatisticsBoxUiBinder extends UiBinder<Widget, PreRaceStatisticsBox> {
    }
    
    @UiField UListElement itemContainerUi;

    public PreRaceStatisticsBox() {
        this(true);
    }
    
    public PreRaceStatisticsBox(boolean showRegattaInformation) {
        super(showRegattaInformation);
        PreRaceStatisticsBoxResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setData(EventStatisticsDTO statistics) {
        super.setData(statistics);
        setVisible(true);
    }

    @Override
    public void clear() {
        itemContainerUi.removeAllChildren();
    }
    
    @Override
    public void addItem(String iconUrl, String name, Object payload) {
        itemContainerUi.appendChild(new PreRaceStatisticsBoxItem(iconUrl, name, payload).getElement());
    }

}
