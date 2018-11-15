package com.sap.sailing.gwt.home.desktop.places.qrcode;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class QRCodeActivity extends AbstractActivity {

    private final QRCodePlace place;
    private final QRCodeClientFactory clientFactory;

    public QRCodeActivity(QRCodePlace place, QRCodeClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        QRCodePresenter presenter = new QRCodePresenter(place.getEventId(), place.getCompetitorId(),
                place.getLeaderboardName(), place.getRegattaName(), place.getRegattaRegistrationLinkSecret(),
                place.getEncodedCheckInUrl(), clientFactory, place.getMode());
        QRCodeView qrCodeWidget = new QRCodeView();
        panel.setWidget(qrCodeWidget);
        presenter.setView(qrCodeWidget);
    }

}
