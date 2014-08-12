package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public abstract class StageTeaserBand extends UIObject {

    interface StageTeaserBandUiBinder extends UiBinder<DivElement, StageTeaserBand> {
    }
    
    private static StageTeaserBandUiBinder uiBinder = GWT.create(StageTeaserBandUiBinder.class);

    @UiField SpanElement bandTitle;
    @UiField SpanElement bandSubtitle;
    @UiField AnchorElement actionLink;
    @UiField DivElement isLiveDiv;

    private final PlaceNavigator placeNavigator;
    private final EventBaseDTO event;
    
    public StageTeaserBand(EventBaseDTO event, PlaceNavigator placeNavigator) {
        this.event = event;
        this.placeNavigator = placeNavigator;
        
        StageResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        
        isLiveDiv.getStyle().setDisplay(Display.NONE);

        Event.sinkEvents(actionLink, Event.ONCLICK);
        Event.setEventListener(actionLink, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                switch (DOM.eventGetType(event)) {
                case Event.ONCLICK:
                    actionLinkClicked();
                    break;
                }
            }
        });

        actionLink.getStyle().setDisplay(Display.NONE);
    }

    public PlaceNavigator getPlaceNavigator() {
        return placeNavigator;
    }

    public EventBaseDTO getEvent() {
        return event;
    }
    
    protected void actionLinkClicked() {
    }
}
