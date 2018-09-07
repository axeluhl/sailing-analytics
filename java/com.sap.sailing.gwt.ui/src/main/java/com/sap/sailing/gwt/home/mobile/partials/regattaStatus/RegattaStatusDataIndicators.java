package com.sap.sailing.gwt.home.mobile.partials.regattaStatus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO.RaceDataInfo;

class RegattaStatusDataIndicators extends Widget {

    private static RegattaStatusDataIndicatorsUiBinder uiBinder = GWT.create(RegattaStatusDataIndicatorsUiBinder.class);

    interface RegattaStatusDataIndicatorsUiBinder extends UiBinder<Element, RegattaStatusDataIndicators> {
    }

    @UiField
    DivElement gpsDataIndicatorUi;
    @UiField
    DivElement windDataIndicatorUi;
    @UiField
    DivElement videoDataIndicatorUi;
    @UiField
    DivElement audioDataIndicatorUi;

    RegattaStatusDataIndicators(final RaceDataInfo raceDataInfo) {
        setElement(uiBinder.createAndBindUi(this));
        UIObject.setVisible(gpsDataIndicatorUi, raceDataInfo.hasGPSData());
        UIObject.setVisible(windDataIndicatorUi, raceDataInfo.hasWindData());
        UIObject.setVisible(videoDataIndicatorUi, raceDataInfo.hasVideoData());
        UIObject.setVisible(audioDataIndicatorUi, raceDataInfo.hasAudioData());
        this.setVisible(raceDataInfo.hasData());
    }

}
