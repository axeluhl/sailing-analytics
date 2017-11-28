package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.statistic;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.ui.client.shared.controls.QRCodeWrapper;

class PreRaceStatisticsBoxQR extends UIObject {

    private static PreRaceStatisticsBoxItemUiBinder uiBinder = GWT.create(PreRaceStatisticsBoxItemUiBinder.class);

    interface PreRaceStatisticsBoxItemUiBinder extends UiBinder<Element, PreRaceStatisticsBoxQR> {
    }
    
    @UiField ImageElement iconUi;
    @UiField SpanElement nameUi;
    @UiField
    DivElement valueUi;

    public PreRaceStatisticsBoxQR(String iconUrl, String name, String url) {
        setElement(uiBinder.createAndBindUi(this));
        iconUi.setSrc(iconUrl);
        nameUi.setInnerText(name);

        QRCodeWrapper qrCodeWrapper = QRCodeWrapper.wrap(valueUi, 200, QRCodeWrapper.ERROR_CORRECTION_LEVEL_H);
        qrCodeWrapper.setQrCodeContent(url);
    }

}
