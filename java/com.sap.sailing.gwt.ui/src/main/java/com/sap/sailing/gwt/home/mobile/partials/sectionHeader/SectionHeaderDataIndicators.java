package com.sap.sailing.gwt.home.mobile.partials.sectionHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO.RaceDataInfo;

public class SectionHeaderDataIndicators extends Widget {

    private static SectionHeaderDataIndicatorsUiBinder uiBinder = GWT.create(SectionHeaderDataIndicatorsUiBinder.class);

    interface SectionHeaderDataIndicatorsUiBinder extends UiBinder<Element, SectionHeaderDataIndicators> {
    }

    @UiField
    DivElement gpsDataIndicatorUi;
    @UiField
    DivElement windDataIndicatorUi;
    @UiField
    DivElement videoDataIndicatorUi;
    @UiField
    DivElement audioDataIndicatorUi;

    public SectionHeaderDataIndicators(final RaceDataInfo raceDataInfo) {
        setElement(uiBinder.createAndBindUi(this));
        UIObject.setVisible(gpsDataIndicatorUi, raceDataInfo.hasGPSData());
        UIObject.setVisible(windDataIndicatorUi, raceDataInfo.hasWindData());
        UIObject.setVisible(videoDataIndicatorUi, raceDataInfo.hasVideoData());
        UIObject.setVisible(audioDataIndicatorUi, raceDataInfo.hasAudioData());
        this.setVisible(raceDataInfo.hasData());
    }

}
