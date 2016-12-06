package com.sap.sailing.gwt.home.desktop.partials.regattaheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO.RaceDataInfo;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RegattaHeaderLegendPopup extends PopupPanel {
    private static RegattaHeaderLegendPopupUiBinder uiBinder = GWT.create(RegattaHeaderLegendPopupUiBinder.class);

    interface RegattaHeaderLegendPopupUiBinder extends UiBinder<Widget, RegattaHeaderLegendPopup> {
    }

    @UiField
    DivElement iconGPS;
    @UiField
    DivElement iconWind;
    @UiField
    DivElement iconVideo;
    @UiField
    DivElement iconAudio;

    public RegattaHeaderLegendPopup(RaceDataInfo raceDataInfo) {
        super(true);
        setWidget(uiBinder.createAndBindUi(this));

        iconGPS.setInnerText(
                raceDataInfo.hasGPSData() ? StringMessages.INSTANCE.eventRegattaHeaderLegendGps()
                        : StringMessages.INSTANCE.eventRegattaHeaderLegendGpsNo());
        iconWind.setInnerText(raceDataInfo.hasWindData() ? StringMessages.INSTANCE.eventRegattaHeaderLegendWind()
                : StringMessages.INSTANCE.eventRegattaHeaderLegendWindNo());
        iconVideo.setInnerText(raceDataInfo.hasVideoData() ? StringMessages.INSTANCE.eventRegattaHeaderLegendVideo()
                : StringMessages.INSTANCE.eventRegattaHeaderLegendVideoNo());
        iconAudio.setInnerText(raceDataInfo.hasAudioData() ? StringMessages.INSTANCE.eventRegattaHeaderLegendAudio()
                : StringMessages.INSTANCE.eventRegattaHeaderLegendAudioNo());
    }
}
