package com.sap.sailing.gwt.home.mobile.partials.stage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.databylogo.DataByLogo;
import com.sap.sailing.gwt.home.communication.event.EventLinkAndMetadataDTO;
import com.sap.sailing.gwt.home.communication.start.EventStageDTO;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigator;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;

public abstract class StageTeaserBand extends Composite {

    interface StageTeaserBandUiBinder extends UiBinder<Widget, StageTeaserBand> {
    }
    
    private static StageTeaserBandUiBinder uiBinder = GWT.create(StageTeaserBandUiBinder.class);

    @UiField
    DivElement bandTitle;
    @UiField
    DivElement bandSubtitle;
    @UiField
    Anchor actionLink;
    @UiField
    DataByLogo dataByLogo;

    private final MobilePlacesNavigator placeNavigator;
    private final EventLinkAndMetadataDTO event;
    private final PlaceNavigation<EventDefaultPlace> eventNavigation;
    
    public StageTeaserBand(EventLinkAndMetadataDTO event, MobilePlacesNavigator placeNavigator) {
        this.event = event;
        this.placeNavigator = placeNavigator;
        
        StageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        if (event instanceof EventStageDTO) {
            dataByLogo.setUp(((EventStageDTO) event).getTrackingConnectorInfos(), false);
        } else {
            dataByLogo.setVisible(false);
        }
        
        // TODO currently hidden by default
        dataByLogo.setVisible(false);
        
        eventNavigation = placeNavigator.getEventNavigation(event.getId().toString(), event.getBaseURL(), event.isOnRemoteServer());
    }

    @UiHandler("actionLink")
    public void stageActionClicked(ClickEvent e) {
        actionLinkClicked(e);
    }

    public PlaceNavigator getPlaceNavigator() {
        return placeNavigator;
    }

    public EventLinkAndMetadataDTO getEvent() {
        return event;
    }

    public PlaceNavigation<EventDefaultPlace> getEventNavigation() {
        return eventNavigation;
    }

    protected void actionLinkClicked(ClickEvent e) {
    }
}
