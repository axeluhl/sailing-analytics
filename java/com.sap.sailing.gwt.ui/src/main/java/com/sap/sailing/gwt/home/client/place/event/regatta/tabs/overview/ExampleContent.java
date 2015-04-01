package com.sap.sailing.gwt.home.client.place.event.regatta.tabs.overview;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRacesDTO;

public class ExampleContent extends Widget implements RefreshableWidget<LiveRacesDTO> {

    private static final DateTimeFormat FORMAT = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL);
    private static ExampleContentUiBinder uiBinder = GWT.create(ExampleContentUiBinder.class);

    interface ExampleContentUiBinder extends UiBinder<Element, ExampleContent> {
    }
    
    @UiField SpanElement lastUpdate;
    @UiField SpanElement nextUpdate;
    @UiField SpanElement updateNo;
    @UiField DivElement liveRaces;

    public ExampleContent() {
        setElement(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setData(LiveRacesDTO data, long nextUpdate, int updateNo) {
        this.lastUpdate.setInnerText(FORMAT.format(new Date()));
        this.nextUpdate.setInnerText(FORMAT.format(new Date(nextUpdate)));
        this.updateNo.setInnerText("" + updateNo);
        
        for(LiveRaceDTO race : data.getRaces()) {
            DivElement raceDiv = Document.get().createDivElement();
            raceDiv.setInnerText(race.getName());
            liveRaces.appendChild(raceDiv);
        }
    }

}
