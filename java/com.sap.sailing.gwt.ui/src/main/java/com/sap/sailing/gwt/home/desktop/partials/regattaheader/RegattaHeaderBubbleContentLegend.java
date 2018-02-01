package com.sap.sailing.gwt.home.desktop.partials.regattaheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO.RaceDataInfo;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RegattaHeaderBubbleContentLegend extends Composite {
    
    private static BubbleContentLegendUiBinder uiBinder = GWT.create(BubbleContentLegendUiBinder.class);

    interface BubbleContentLegendUiBinder extends UiBinder<Widget, RegattaHeaderBubbleContentLegend> {
    }

    @UiField
    DivElement txtGPS;
    @UiField
    DivElement txtWind;
    @UiField
    DivElement txtVideo;
    @UiField
    DivElement txtAudio;

    @UiField
    DivElement iconGPS;
    @UiField
    DivElement iconWind;
    @UiField
    DivElement iconVideo;
    @UiField
    DivElement iconAudio;

    public RegattaHeaderBubbleContentLegend(RaceDataInfo raceDataInfo) {
        initWidget(uiBinder.createAndBindUi(this));

        txtGPS.setInnerText(
                raceDataInfo.hasGPSData() ? StringMessages.INSTANCE.eventRegattaHeaderLegendGps()
                        : StringMessages.INSTANCE.eventRegattaHeaderLegendGpsNo());
        txtWind.setInnerText(raceDataInfo.hasWindData() ? StringMessages.INSTANCE.eventRegattaHeaderLegendWind()
                : StringMessages.INSTANCE.eventRegattaHeaderLegendWindNo());
        txtVideo.setInnerText(raceDataInfo.hasVideoData() ? StringMessages.INSTANCE.eventRegattaHeaderLegendVideo()
                : StringMessages.INSTANCE.eventRegattaHeaderLegendVideoNo());
        txtAudio.setInnerText(raceDataInfo.hasAudioData() ? StringMessages.INSTANCE.eventRegattaHeaderLegendAudio()
                : StringMessages.INSTANCE.eventRegattaHeaderLegendAudioNo());

        iconGPS.getStyle().setOpacity(raceDataInfo.hasGPSData() ? 1.0 : 0.15);
        iconWind.getStyle().setOpacity(raceDataInfo.hasWindData() ? 1.0 : 0.15);
        iconVideo.getStyle().setOpacity(raceDataInfo.hasVideoData() ? 1.0 : 0.15);
        iconAudio.getStyle().setOpacity(raceDataInfo.hasAudioData() ? 1.0 : 0.15);
    }
}
