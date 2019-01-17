package com.sap.sailing.gwt.home.desktop.places.qrcode;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.regatta.SimpleRegattaDTO;
import com.sap.sailing.gwt.home.desktop.places.qrcode.QRCodePlace.InvitationMode;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.QRCodeWrapper;

public class QRCodeView extends Composite {
    private static QRCodeViewUiBinder uiBinder = GWT.create(QRCodeViewUiBinder.class);

    interface QRCodeViewUiBinder extends UiBinder<Widget, QRCodeView> {
    }

    @UiField
    DivElement errorDivUi;

    @UiField
    DivElement titleDivUi;

    @UiField
    DivElement subtitleDivUi;

    @UiField
    DivElement infoDivUi;

    @UiField
    DivElement qrCodeDivUi;

    @UiField
    DivElement eventImageUi;

    @UiField
    AnchorElement urlAnchor;

    public QRCodeView() {
        super();
        initWidget(uiBinder.createAndBindUi(this));

    }

    public void setData(EventViewDTO event, SimpleCompetitorWithIdDTO competitor, String leaderboardName, SimpleRegattaDTO regatta, String url,
            InvitationMode invitationMode) {
        switch (invitationMode) {
        case COMPETITOR:
        case COMPETITOR_2:
            titleDivUi.setInnerText(StringMessages.INSTANCE.qrCodeTitle(competitor.getName(), leaderboardName));
            subtitleDivUi.setInnerText(
                    StringMessages.INSTANCE.qrCodeSubtitle(event.getDisplayName(), event.getLocationAndVenue()));
            break;
        case PUBLIC_INVITE:
            titleDivUi.setInnerText(StringMessages.INSTANCE.qrCodeTitleOpenRegatta(regatta.getName()));
            subtitleDivUi.setInnerText(StringMessages.INSTANCE.qrCodeSubtitleOpenRegatta());
            break;
        case BOUY_TENDER:
            titleDivUi.setInnerText(StringMessages.INSTANCE.qrCodeTitleBouy(leaderboardName));
            subtitleDivUi.setInnerText(
                    StringMessages.INSTANCE.qrCodeSubtitle(event.getDisplayName(), event.getLocationAndVenue()));
            break;
        }

        urlAnchor.setHref(url);
        ScriptInjector.fromUrl("qrcode/qrcode.min.js").setWindow(ScriptInjector.TOP_WINDOW)
                .setCallback(new Callback<Void, Exception>() {

                    @Override
                    public void onSuccess(Void result) {
                        QRCodeWrapper qrCodeWrapper = QRCodeWrapper.wrap(qrCodeDivUi, 600,
                                QRCodeWrapper.ERROR_CORRECTION_LEVEL_H);
                        qrCodeWrapper.setQrCodeContent(url);
                    }

                    @Override
                    public void onFailure(Exception reason) {
                        Window.alert("could not load qrcode library " + reason.getMessage());
                    }
                }).inject();

        if (event != null) {
            eventImageUi.getStyle().setBackgroundImage("url('" + event.getLogoImage().getSourceRef() + "')");
            eventImageUi.getStyle().setWidth(event.getLogoImage().getWidthInPx(), Unit.PX);
            eventImageUi.getStyle().setHeight(event.getLogoImage().getHeightInPx(), Unit.PX);
        }
    }

    public void setError() {
        errorDivUi.setInnerText(StringMessages.INSTANCE.qrCodeErrorMessage());
        infoDivUi.removeFromParent();
    }

}
