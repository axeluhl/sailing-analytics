package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public abstract class StageTeaser extends Composite {
    @UiField DivElement bandCount;
    @UiField SpanElement subtitle;
    @UiField SpanElement title;
    @UiField DivElement countDown;
    @UiField HTMLPanel stageTeaserBandsPanel;
    @UiField DivElement teaserImage;

    private final static String DEFAULT_STAGE_IMAGE_URL = "http://static.sapsailing.com/ubilabsimages/default/default_stage_event_teaser.jpg"; 
    
    interface StageTeaserUiBinder extends UiBinder<Widget, StageTeaser> {
    }
    
    private static StageTeaserUiBinder uiBinder = GWT.create(StageTeaserUiBinder.class);

    public StageTeaser(EventDTO event) {
        StageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        String stageImageUrl = event.getStageImageURL() != null ? event.getStageImageURL() : DEFAULT_STAGE_IMAGE_URL;

        String backgroundImage = "url(" + stageImageUrl + ")";
        teaserImage.getStyle().setBackgroundImage(backgroundImage);
    }
}
