package com.sap.sailing.gwt.home.client.place.event.partials.raceListLive;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRacesDTO;

public class RacesListLive extends Composite implements RefreshableWidget<LiveRacesDTO> {

    private static RacesListLiveUiBinder uiBinder = GWT.create(RacesListLiveUiBinder.class);

    interface RacesListLiveUiBinder extends UiBinder<Widget, RacesListLive> {
    }

    @UiField TableElement table;
    @UiField RacesListLiveHead listHead;
    
    public RacesListLive() {
        RacesListLiveResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setData(LiveRacesDTO data, long nextUpdate, int updateNo) {
        table.removeAllChildren();
        table.appendChild(listHead.getElement());
        for (LiveRaceDTO race : data.getRaces()) {
            table.appendChild(new RacesListLiveRow(race).getElement());
        }
    }

}
