package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.Countdown;
import com.sap.sailing.gwt.home.client.shared.Countdown.CountdownListener;
import com.sap.sailing.gwt.home.client.shared.Countdown.RemainingTime;
import com.sap.sailing.gwt.ui.shared.start.EventStageDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.controls.carousel.LazyLoadable;

public abstract class StageTeaser extends Composite implements LazyLoadable {
    @UiField
    DivElement bandCount;
    @UiField
    SpanElement subtitle;
    @UiField
    SpanElement title;
    @UiField
    DivElement countdown;
    @UiField
    DivElement countdownMajor;
    @UiField
    DivElement countdownMajorValue;
    @UiField
    DivElement countdownMajorUnit;
    @UiField
    DivElement countdownMinor;
    @UiField
    DivElement countdownMinorValue;
    @UiField
    DivElement countdownMinorUnit;
    @UiField
    HTMLPanel stageTeaserBandsPanel;
    @UiField
    DivElement teaserImage;

    interface StageTeaserUiBinder extends UiBinder<Widget, StageTeaser> {
    }

    private static StageTeaserUiBinder uiBinder = GWT.create(StageTeaserUiBinder.class);
    private final EventStageDTO event;

    @Override
    public void doInitializeLazyComponents() {
        String stageImageUrl = event.getStageImageURL() != null ? event.getStageImageURL() : StageResources.INSTANCE
                .defaultStageEventTeaserImage().getSafeUri().asString();
        String backgroundImage = "url(" + stageImageUrl + ")";
        teaserImage.getStyle().setBackgroundImage(backgroundImage);

        teaserImage.getStyle().setOpacity(0);
        new Animation() {

            @Override
            protected void onUpdate(double progress) {
                teaserImage.getStyle().setOpacity(progress);

            }
        }.run(500);


    }

    protected void handleUserAction() {

    }

    public StageTeaser(EventStageDTO event) {
        this.event = event;
        StageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        TimePoint eventStart = new MillisecondsTimePoint(event.getStartDate());
        CountdownListener countdownListener = new CountdownListener() {

            @Override
            public void changed(RemainingTime major, RemainingTime minor) {
                updateCountdown(major, minor);
            }

        };
        new Countdown(eventStart, countdownListener);
       
        addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                handleUserAction();
            }
        }, ClickEvent.getType());
        
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
                countdownMajorUnit.setInnerHTML(String.valueOf(major.unitI18n()));
            } else {
                countdownMajorValue.setInnerHTML(null);
                countdownMajorUnit.setInnerHTML(null);
                countdownMajor.getStyle().setDisplay(Display.NONE);
                countdownMajor.getStyle().setVisibility(Visibility.HIDDEN);
            }
            if (minor != null) {
                countdownMinorValue.setInnerHTML(String.valueOf(minor.value));
                countdownMinorUnit.setInnerHTML(String.valueOf(minor.unitI18n()));
            } else {
                countdownMinorValue.setInnerHTML(null);
                countdownMinorUnit.setInnerHTML(null);
                countdownMinor.getStyle().setDisplay(Display.NONE);
                countdownMinor.getStyle().setVisibility(Visibility.HIDDEN);
            }
        }
    }
}
