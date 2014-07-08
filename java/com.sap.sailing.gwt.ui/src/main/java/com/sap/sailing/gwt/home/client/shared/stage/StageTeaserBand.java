package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class StageTeaserBand extends Composite {

    interface StageTeaserBandUiBinder extends UiBinder<Widget, StageTeaserBand> {
    }
    
    private static StageTeaserBandUiBinder uiBinder = GWT.create(StageTeaserBandUiBinder.class);

    @UiField SpanElement bandTitle;
    @UiField SpanElement bandSubtitle;
    @UiField Anchor actionLink;

    private final PlaceNavigator placeNavigator;
    private final EventDTO event;
    
    public StageTeaserBand(EventDTO event, PlaceNavigator placeNavigator) {
        this.event = event;
        this.placeNavigator = placeNavigator;
        
        StageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        bandTitle.setInnerText("bandTitle");
        bandSubtitle.setInnerText("bandSubtitle");
        
        actionLink.setText("Show event");
    }
    
    @UiHandler("actionLink")
    public void actionLinkClicked(ClickEvent e) {
        placeNavigator.goToEvent(event.id.toString());
    }
}
