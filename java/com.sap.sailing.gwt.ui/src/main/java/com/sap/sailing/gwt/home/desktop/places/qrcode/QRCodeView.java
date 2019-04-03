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
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.home.shared.partials.dialog.confirm.ConfirmDialogFactory;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.QRCodeWrapper;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.QRCodeEvent;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

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

    public void showCompetitor(QRCodeEvent event, CompetitorDTO competitor, BoatDTO boat, MarkDTO mark,
            String leaderboardName, String branchIoUrl) {
        if (boat != null) {
            titleDivUi.setInnerText(
                    StringMessages.INSTANCE.qrCodeBoatInviteTitle(boat.getDisplayName(), leaderboardName));
        } else if (mark != null) {
            titleDivUi.setInnerText(StringMessages.INSTANCE.qrCodeMarkInviteTitle(mark.getName(), leaderboardName));
        } else if (competitor != null) {
            titleDivUi.setInnerText(
                    StringMessages.INSTANCE.qrCodeCompetitorInviteTitle(competitor.getName(), leaderboardName));
        } else {
            titleDivUi.setInnerText(StringMessages.INSTANCE.qrCodeTitle(leaderboardName));
        }
        if (event != null) {
            subtitleDivUi.setInnerText(
                    StringMessages.INSTANCE.qrCodeSubtitle(event.getDisplayName(), event.getLocationAndVenue()));
        }
        showQrCodeForURL(branchIoUrl);
        showEventImageIfPossible(event);
    }

    public void showPublic(String publicRegattaName, String publicInviteBranchIOUrl) {
        titleDivUi.setInnerText(StringMessages.INSTANCE.qrCodeTitleOpenRegatta(publicRegattaName));
        showQrCodeForURL(publicInviteBranchIOUrl);
    }

    public void showBouyTender(QRCodeEvent event, String leaderboardName, String branchIoUrl) {
        titleDivUi.setInnerText(StringMessages.INSTANCE.qrCodeTitleBouy(leaderboardName));
        if (event != null) {
            subtitleDivUi.setInnerText(
                    StringMessages.INSTANCE.qrCodeSubtitle(event.getDisplayName(), event.getLocationAndVenue()));
        }
        showQrCodeForURL(branchIoUrl);
        showEventImageIfPossible(event);
    }

    private void showEventImageIfPossible(QRCodeEvent event) {
        if (event != null && event.getLogoImage() != null) {
            eventImageUi.getStyle().setBackgroundImage("url('" + event.getLogoImage().getSourceRef() + "')");
            eventImageUi.getStyle().setWidth(event.getLogoImage().getWidthInPx(), Unit.PX);
            eventImageUi.getStyle().setHeight(event.getLogoImage().getHeightInPx(), Unit.PX);
        }
    }

    private void showQrCodeForURL(String url) {
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
    }

    public void setError() {
        errorDivUi.setInnerText(StringMessages.INSTANCE.qrCodeErrorMessage());
        infoDivUi.removeFromParent();
    }

    public void showRedirectionDialog(Triple<String, String, Integer> correctServerHost, Runnable runnable) {
        ConfirmDialogFactory.showConfirmDialog(
                StringMessages.INSTANCE.qrCodeUnsecureServerRedirect(correctServerHost.getB()),
                StringMessages.INSTANCE.qrCodeUnsecureServerRedirectTitle(correctServerHost.getB()),
                new DialogCallback<Void>() {
                    @Override
                    public void ok(Void editedObject) {
                        runnable.run();
                    }

                    @Override
                    public void cancel() {
                    }
                });
    }
}
