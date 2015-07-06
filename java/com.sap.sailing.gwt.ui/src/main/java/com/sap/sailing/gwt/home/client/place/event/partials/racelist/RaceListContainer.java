package com.sap.sailing.gwt.home.client.place.event.partials.racelist;


import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;

public class RaceListContainer<T extends RaceMetadataDTO> extends Composite {

    private static RaceListContainerUiBinder uiBinder = GWT.create(RaceListContainerUiBinder.class);

    interface RaceListContainerUiBinder extends UiBinder<Widget, RaceListContainer<?>> {
    }
    
    @UiField HeadingElement titleUi;
    @UiField DivElement infoUi;
    @UiField(provided = true) AbstractRaceList<T> raceListUi;

    public RaceListContainer(String title, AbstractRaceList<T> raceList) {
        RacesListLiveResources.INSTANCE.css().ensureInjected();
        this.raceListUi = raceList;
        initWidget(uiBinder.createAndBindUi(this));
        this.titleUi.setInnerText(title);
    }
    
    public void setRaceListData(Collection<T> data) {
        this.raceListUi.setTableData(data);
    }
    
    public void setInfoText(String infoText) {
        this.infoUi.setInnerText(infoText);
        this.infoUi.getStyle().clearDisplay();
    }

}
