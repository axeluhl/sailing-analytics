package com.sap.sailing.gwt.home.desktop.places.qrcode;

import java.util.UUID;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.BranchIOConstants;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.home.desktop.places.qrcode.QRCodePlace.InvitationMode;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.QRCodeEvent;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;

public class QRCodePresenter {
    private final QRCodeClientFactory clientFactory;
    private DataCollector dataCollector;
    private QRCodePlace place;

    public QRCodePresenter(QRCodeClientFactory clientFactory, QRCodePlace place) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    public void setView(QRCodeView view) {
        String rawTargetServer = place.getTargetServer();
        Triple<String, String, Integer> correctServerHost = getServerAndPort(rawTargetServer);
        if (isCorrectServer(correctServerHost)) {
            showQrCode(view);
        } else {
            if (isSecureServer(correctServerHost)) {
                String url = getCorrectUrl(correctServerHost);
                Window.Location.assign(url);
            } else {
                view.showRedirectionDialog(correctServerHost, new Runnable() {
                    @Override
                    public void run() {
                        String url = getCorrectUrl(correctServerHost);
                        Window.Location.assign(url);
                    }
                });
            }
        }
    }

    private boolean isSecureServer(Triple<String, String, Integer> correctServerHost) {
        String host = correctServerHost.getB();
        if (host.endsWith("sapsailing.com")) {
            return true;
        }
        if (host.equals("127.0.0.1")) {
            return true;
        }
        return false;
    }

    private Triple<String, String, Integer> getServerAndPort(String rawTargetServer) {
        final String protocol;
        final String host;
        final Integer port;
        String[] protocolAndHostPort = rawTargetServer.split("://");
        protocol = protocolAndHostPort[0] + ":";

        if (protocolAndHostPort[1].contains(":")) {
            String[] parts = protocolAndHostPort[1].split(":");
            host = parts[0];
            port = Integer.parseInt(parts[1]);
        } else {
            port = null;
            host = protocolAndHostPort[1];
        }
        Triple<String, String, Integer> answer = new Triple<>(protocol, host, port);
        return answer;
    }

    private String getCorrectUrl(Triple<String, String, Integer> correctServerHost) {
        String newUrl = Window.Location.createUrlBuilder().setHost(correctServerHost.getB())
                .setProtocol(correctServerHost.getA())
                .setPort(correctServerHost.getC() == null ? UrlBuilder.PORT_UNSPECIFIED : correctServerHost.getC())
                .buildString();
        return newUrl;
    }

    private boolean isCorrectServer(Triple<String, String, Integer> correctServerHost) {
        boolean protocolSame = Util.equalsWithNull(correctServerHost.getA(), Window.Location.getProtocol());
        boolean hostSame = Util.equalsWithNull(correctServerHost.getB(), Window.Location.getHostName());
        boolean portSame = Util.equalsWithNull("" + correctServerHost.getC(), Window.Location.getPort());
        return protocolSame && hostSame && portSame;
    }

    private void showQrCode(QRCodeView view) {
        switch (place.getMode()) {
        case BOUY_TENDER:
            if (place.getEncodedCheckInUrl() == null || place.getEncodedCheckInUrl().isEmpty()) {
                view.setError();
            } else {
                dataCollector = new DataCollector(view);
                retrieveEvent(place.getEventId());
            }
            break;
        case COMPETITOR:
        case COMPETITOR_2:
            if (place.getEncodedCheckInUrl() == null || place.getEncodedCheckInUrl().isEmpty()) {
                view.setError();
            } else {
                dataCollector = new DataCollector(view);
                if (place.getCompetitorId() != null) {
                    retrieveCompetitor(place.getCompetitorId());
                } else if (place.getBoatId() != null) {
                    retrieveBoat(place.getBoatId());
                } else if (place.getMarkId() != null) {
                    retrieveMark(place.getMarkId());
                } else {
                    // bouytenders work without additional, proceed without participant
                    dataCollector.setCompetitor(null);
                }
                retrieveEvent(place.getEventId());
            }
            break;
        case PUBLIC_INVITE:
            // as the event is most likely displayed on a different server anyway, do not load additional data
            dataCollector = new DataCollector(view);
            dataCollector.proceedIfFinished();
            break;
        }
    }

    private void retrieveMark(UUID markId) {
        clientFactory.getSailingService().getMark(markId, place.getLeaderboardName(),
                place.getRegattaRegistrationLinkSecret(), new AsyncCallback<MarkDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        dataCollector.setMark(null);
                    }

                    @Override
                    public void onSuccess(MarkDTO result) {
                        dataCollector.setMark(result);
                    }
                });
    }

    private void retrieveBoat(UUID boatId) {
        clientFactory.getSailingService().getBoat(boatId, place.getLeaderboardName(),
                place.getRegattaRegistrationLinkSecret(), new AsyncCallback<BoatDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        dataCollector.setBoat(null);
                    }

                    @Override
                    public void onSuccess(BoatDTO result) {
                        dataCollector.setBoat(result);
                    }
                });
    }

    private void retrieveEvent(UUID eventId) {
        GWT.log("retrieving event: " + eventId);
        clientFactory.getSailingService().getEvent(eventId, place.getLeaderboardName(),
                place.getRegattaRegistrationLinkSecret(), new AsyncCallback<QRCodeEvent>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        dataCollector.setEvent(null);
                    }

                    @Override
                    public void onSuccess(QRCodeEvent result) {
                        dataCollector.setEvent(result);
                    }
                });
    }

    private void retrieveCompetitor(UUID competitorId) {
        GWT.log("retrieving competitor: " + competitorId);
        clientFactory.getSailingService().getCompetitor(competitorId, place.getLeaderboardName(),
                place.getRegattaRegistrationLinkSecret(), new AsyncCallback<CompetitorDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        dataCollector.setCompetitor(null);
                    }

                    @Override
                    public void onSuccess(CompetitorDTO result) {
                        dataCollector.setCompetitor(result);
                    }
                });
    }

    private final class DataCollector {
        private boolean eventIsSet = false;
        private boolean participantIsSet = false;

        private CompetitorDTO competitor;
        private QRCodeEvent event;
        private BoatDTO boat;
        private MarkDTO mark;

        private final QRCodeView view;

        public DataCollector(QRCodeView view) {
            this.view = view;
        }

        public void setMark(MarkDTO mark) {
            participantIsSet = true;
            this.mark = mark;
            proceedIfFinished();
        }

        public void setCompetitor(CompetitorDTO competitor) {
            participantIsSet = true;
            this.competitor = competitor;
            proceedIfFinished();
        }

        public void setBoat(BoatDTO boat) {
            participantIsSet = true;
            this.boat = boat;
            proceedIfFinished();
        }

        public void setEvent(QRCodeEvent event) {
            eventIsSet = true;
            this.event = event;
            proceedIfFinished();
        }

        private void proceedIfFinished() {
            switch (place.getMode()) {
            case BOUY_TENDER:
                if (eventIsSet) {
                    String branchIoUrl = BranchIOConstants.BUOYPINGER_APP_BRANCHIO + "?"
                            + BranchIOConstants.BUOYPINGER_APP_BRANCHIO_PATH + "=" + place.getEncodedCheckInUrl();
                    view.showBouyTender(event, place.getLeaderboardName(), branchIoUrl);
                }
                break;
            case COMPETITOR:
            case COMPETITOR_2:
                if (participantIsSet && eventIsSet) {
                    String sailInsightBranch = place.getMode() == InvitationMode.COMPETITOR
                            ? BranchIOConstants.SAILINSIGHT_APP_BRANCHIO
                            : BranchIOConstants.SAILINSIGHT_2_APP_BRANCHIO;
                    String branchIoUrl = sailInsightBranch + "?"
                            + BranchIOConstants.SAILINSIGHT_APP_BRANCHIO_PATH + "=" + place.getEncodedCheckInUrl();
                    view.showCompetitor(event, competitor, boat, mark, place.getLeaderboardName(), branchIoUrl);
                }
                break;
            case PUBLIC_INVITE:
                view.showPublic(place.getPublicRegattaName(), place.getPublicInviteBranchIOUrl());
                break;

            }
        }
    }

}
