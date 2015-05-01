package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventDefaultPlace;
import com.sap.sailing.gwt.ui.shared.start.EventStageDTO;

public abstract class StageTeaserBand extends Composite {

    interface StageTeaserBandUiBinder extends UiBinder<Widget, StageTeaserBand> {
    }
    
    private static StageTeaserBandUiBinder uiBinder = GWT.create(StageTeaserBandUiBinder.class);

    @UiField SpanElement bandTitle;
    @UiField SpanElement bandSubtitle;
    @UiField Anchor actionLink;
    @UiField DivElement isLiveDiv;

    private final HomePlacesNavigator placeNavigator;
    private final EventStageDTO event;
    private final PlaceNavigation<EventDefaultPlace> eventNavigation;
    
    public StageTeaserBand(EventStageDTO event, HomePlacesNavigator placeNavigator) {
        this.event = event;
        this.placeNavigator = placeNavigator;
        
        StageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        isLiveDiv.getStyle().setDisplay(Display.NONE);

        eventNavigation = placeNavigator.getEventNavigation(event.getId().toString(), event.getBaseURL(), event.isOnRemoteServer());
    }

    @UiHandler("actionLink")
    public void stageActionClicked(ClickEvent e) {
        actionLinkClicked(e);
    }

    public PlaceNavigator getPlaceNavigator() {
        return placeNavigator;
    }

    public EventStageDTO getEvent() {
        return event;
    }

    public PlaceNavigation<EventDefaultPlace> getEventNavigation() {
        return eventNavigation;
    }

    protected void actionLinkClicked(ClickEvent e) {
    }
}
