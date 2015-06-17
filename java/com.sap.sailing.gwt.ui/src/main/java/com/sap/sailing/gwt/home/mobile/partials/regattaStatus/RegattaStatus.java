package com.sap.sailing.gwt.home.mobile.partials.regattaStatus;

import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RegattasAndLiveRacesDTO;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;

public class RegattaStatus extends Composite implements RefreshableWidget<RegattasAndLiveRacesDTO> {

    private static RegattaStatusUiBinder uiBinder = GWT.create(RegattaStatusUiBinder.class);

    interface RegattaStatusUiBinder extends UiBinder<Widget, RegattaStatus> {
    }

    @UiField MobileSection itemContainerUi;
    @UiField DivElement regattaContainerUi;
    
    public RegattaStatus() {
        RegattaStatusResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setData(RegattasAndLiveRacesDTO data, long nextUpdate, int updateNo) {
        itemContainerUi.clearContent();
        for (Entry<RegattaMetadataDTO, Set<LiveRaceDTO>> pair : data.getRegattasWithRaces().entrySet()) {
            addRegatta(pair.getKey(), pair.getValue());
        }
    }
    
    public void addRegatta(RegattaMetadataDTO regatta, Set<LiveRaceDTO> liveRaces) {
        RegattaStatusRegatta regattaWidget = new RegattaStatusRegatta(regatta);
        itemContainerUi.addContent(regattaWidget);
        for (LiveRaceDTO race : liveRaces) {
            regattaWidget.addRace(race);
        }
    }

}
