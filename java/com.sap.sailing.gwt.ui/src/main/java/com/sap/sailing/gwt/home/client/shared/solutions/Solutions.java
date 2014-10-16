package com.sap.sailing.gwt.home.client.shared.solutions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Solutions extends Composite {

    @UiField Anchor sailingAnalyticsAnchor;
    @UiField Anchor raceAnchor;
    @UiField Anchor postRaceAnchor;
    @UiField Anchor trainingDiaryAnchor;
    @UiField Anchor simulatorAnchor;

    @UiField DivElement sailingAnalyticsDiv;
    @UiField DivElement raceDiv;
    @UiField DivElement postRaceDiv;
    @UiField DivElement trainingDiaryDiv;
    @UiField DivElement simulatorDiv;

    interface SolutionsUiBinder extends UiBinder<Widget, Solutions> {
    }
    
    private static SolutionsUiBinder uiBinder = GWT.create(SolutionsUiBinder.class);

    public Solutions() {
        SolutionsResources.INSTANCE.css().ensureInjected();
        
        StyleInjector.injectAtEnd("@media (min-width: 25em) { "+SolutionsResources.INSTANCE.mediumCss().getText()+"}");
        StyleInjector.injectAtEnd("@media (min-width: 50em) { "+SolutionsResources.INSTANCE.largeCss().getText()+"}");

        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("sailingAnalyticsAnchor")
    public void scrollToSailingAnalytics(ClickEvent e) {
        sailingAnalyticsDiv.scrollIntoView();
    }
    
    @UiHandler("raceAnchor")
    public void scrollToRace(ClickEvent e) {
        raceDiv.scrollIntoView();
    }
    @UiHandler("postRaceAnchor")
    public void scrollToPostRace(ClickEvent e) {
        postRaceDiv.scrollIntoView();
    }
    @UiHandler("trainingDiaryAnchor")
    public void scrollToTrainingDiary(ClickEvent e) {
        trainingDiaryDiv.scrollIntoView();
    }

    @UiHandler("simulatorAnchor")
    public void scrollToSimulator(ClickEvent e) {
        simulatorDiv.scrollIntoView();
    }

}
