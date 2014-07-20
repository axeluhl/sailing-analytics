package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.gwt.ui.common.client.Countdown;
import com.sap.sailing.gwt.ui.common.client.Countdown.CountdownListener;
import com.sap.sailing.gwt.ui.common.client.Countdown.RemainingTime;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public abstract class StageTeaser extends Composite {
    @UiField DivElement bandCount;
    @UiField SpanElement subtitle;
    @UiField SpanElement title;
    @UiField DivElement countdown;
    @UiField DivElement countdownMajor;
    @UiField DivElement countdownMajorValue;
    @UiField DivElement countdownMajorUnit;
    @UiField DivElement countdownMinor;
    @UiField DivElement countdownMinorValue;
    @UiField DivElement countdownMinorUnit;
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
        
        if (event.startDate != null) {
            TimePoint eventStart = new MillisecondsTimePoint(event.startDate);
            CountdownListener countdownListener = new CountdownListener() {
                
                @Override
                public void changed(RemainingTime major, RemainingTime minor) {
                    updateCountdown(major, minor);
                }

            };
            new Countdown(eventStart, countdownListener);
        }
    }

    private void updateCountdown(RemainingTime major, RemainingTime minor) {
        if (major == null && minor == null) {
            countdownMajorValue.setInnerHTML(null);
            countdownMajorUnit.setInnerHTML(null);
            countdownMinorValue.setInnerHTML(null);
            countdownMinorUnit.setInnerHTML(null);
            countdown.getStyle().setDisplay(Display.NONE);
            countdown.getStyle().setVisibility(Visibility.HIDDEN);
        } else {
            if (major != null) {
                countdownMajorValue.setInnerHTML(String.valueOf(major.value));
                countdownMajorUnit.setInnerHTML(String.valueOf(major.unit));
            } else {
                countdownMajorValue.setInnerHTML(null);
                countdownMajorUnit.setInnerHTML(null);
                countdownMajor.getStyle().setDisplay(Display.NONE);
                countdownMajor.getStyle().setVisibility(Visibility.HIDDEN);
            }
            if (minor != null) {
                countdownMinorValue.setInnerHTML(String.valueOf(minor.value));
                countdownMinorUnit.setInnerHTML(String.valueOf(minor.unit));
            } else {
                countdownMinorValue.setInnerHTML(null);
                countdownMinorUnit.setInnerHTML(null);
                countdownMinor.getStyle().setDisplay(Display.NONE);
                countdownMinor.getStyle().setVisibility(Visibility.HIDDEN);
            }
        }
    }
}
