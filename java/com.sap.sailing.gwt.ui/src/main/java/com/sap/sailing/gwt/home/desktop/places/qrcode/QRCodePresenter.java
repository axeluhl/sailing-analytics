package com.sap.sailing.gwt.home.desktop.places.qrcode;

import java.util.UUID;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.BranchIOConstants;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.gwt.home.communication.event.GetEventViewAction;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.user.profile.GetCompetitorAction;
import com.sap.sailing.gwt.home.desktop.places.qrcode.QRCodePlace.InvitationMode;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;

public class QRCodePresenter {
    private static final Logger logger = Logger.getLogger(QRCodePresenter.class.getName());
    
    private final QRCodeClientFactory clientFactory;
    private DataCollector dataCollector;
    private QRCodePlace place;

    public QRCodePresenter(QRCodeClientFactory clientFactory, QRCodePlace place) {
        this.clientFactory = clientFactory;
        this.place = place;
        logger.info("Creating QRCodePresenter with place: " + place);
    }

    public void setView(QRCodeView view) {
        String rawTargetServer = place.getTargetServer();
        logger.info("Target server for QR Code: " + rawTargetServer);
        Triple<String, String, Integer> correctServerHost = getServerAndPort(rawTargetServer);
        if (isCorrectServer(correctServerHost)) {
            logger.info("Already the right server for QR Code");
            showQrCode(view);
        } else {
            logger.info("Not the right server for QR Code");
            if (isSecureServer(correctServerHost)) {
                String url = getCorrectUrl(correctServerHost);
                Window.Location.assign(url);
            } else {
                logger.info("Unsafe target server for QR Code -> ask the user");
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
        final String targetProtocol = correctServerHost.getA();
        final String currentProtocol = Window.Location.getProtocol();
        boolean protocolSame = Util.equalsWithNull(targetProtocol, currentProtocol);
        logger.info("Checking Protocol: current=" + currentProtocol + "; target=" + targetProtocol + "; same=" + protocolSame);
        String targetHost = correctServerHost.getB();
        String currentHost = Window.Location.getHostName();
        boolean hostSame = Util.equalsWithNull(targetHost, currentHost);
        logger.info("Checking Host: current=" + currentHost + "; target=" + targetHost + "; same=" + hostSame);
        final Integer targetPort = correctServerHost.getC();
        final String targetPortAsString = correctServerHost.getC() == null ? "" : Integer.toString(targetPort);
        final String currentPort = Window.Location.getPort();
        boolean portSame = Util.equalsWithNull(targetPortAsString, currentPort);
        logger.info("Checking Port: current=" + currentPort + "; target=" + targetPortAsString + "; same=" + portSame);
        return protocolSame && hostSame && portSame;
    }

    private void showQrCode(QRCodeView view) {
        switch (place.getMode()) {
        case BOUY_TENDER:
            logger.info("QR Code for buoy tender to be shown");
            if (place.getEncodedCheckInUrl() == null || place.getEncodedCheckInUrl().isEmpty()) {
                view.setError();
            } else {
                dataCollector = new DataCollector(view);
                retrieveEvent(place.getEventId());
            }
            break;
        case COMPETITOR:
        case COMPETITOR_2:
            logger.info("QR Code for competitor/boat/buoy tracking to be shown");
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
            logger.info("QR Code for public regatta invite to be shown");
            // as the event is most likely displayed on a different server anyway, do not load additional data
            dataCollector = new DataCollector(view);
            dataCollector.proceedIfFinished();
            break;
        }
    }

    private void retrieveMark(UUID markId) {
        logger.info("retrieving mark: " + markId);
        clientFactory.getSailingService().getMark(markId, place.getLeaderboardName(),
                place.getRegattaRegistrationLinkSecret(), new AsyncCallback<MarkDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        logger.info("Could not load mark for QR Code");
                        dataCollector.setMark(null);
                    }

                    @Override
                    public void onSuccess(MarkDTO result) {
                        logger.info("Received mark for QR Code");
                        dataCollector.setMark(result);
                    }
                });
    }

    private void retrieveBoat(UUID boatId) {
        logger.info("retrieving boat: " + boatId);
        clientFactory.getSailingService().getBoat(boatId, place.getLeaderboardName(),
                place.getRegattaRegistrationLinkSecret(), new AsyncCallback<BoatDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        logger.info("Could not load boat for QR Code");
                        dataCollector.setBoat(null);
                    }

                    @Override
                    public void onSuccess(BoatDTO result) {
                        logger.info("Received boat for QR Code");
                        dataCollector.setBoat(result);
                    }
                });
    }

    private void retrieveEvent(UUID eventId) {
        logger.info("retrieving event: " + eventId);
        clientFactory.getDispatch().execute(new GetEventViewAction(eventId), new AsyncCallback<EventViewDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                logger.info("Could not load event for QR Code");
                dataCollector.setEvent(null);
            }

            @Override
            public void onSuccess(EventViewDTO result) {
                logger.info("Received event for QR Code");
                dataCollector.setEvent(result);
            }
        });
    }

    private void retrieveCompetitor(UUID competitorId) {
        GWT.log("retrieving competitor: " + competitorId);
        clientFactory.getDispatch().execute(new GetCompetitorAction(competitorId),
                new AsyncCallback<SimpleCompetitorWithIdDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        logger.info("Could not load competitor for QR Code");
                        dataCollector.setCompetitor(null);
                    }

                    @Override
                    public void onSuccess(SimpleCompetitorWithIdDTO result) {
                        logger.info("Received competitor for QR Code");
                        dataCollector.setCompetitor(result);
                    }
                });
    }

    private final class DataCollector {
        private boolean eventIsSet = false;
        private boolean participantIsSet = false;

        private SimpleCompetitorWithIdDTO competitor;
        private EventViewDTO event;
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

        public void setCompetitor(SimpleCompetitorWithIdDTO competitor) {
            participantIsSet = true;
            this.competitor = competitor;
            proceedIfFinished();
        }

        public void setBoat(BoatDTO boat) {
            participantIsSet = true;
            this.boat = boat;
            proceedIfFinished();
        }

        public void setEvent(EventViewDTO event) {
            eventIsSet = true;
            this.event = event;
            proceedIfFinished();
        }

        private void proceedIfFinished() {
            logger.info("Checking if data for QR Code is loaded");
            switch (place.getMode()) {
            case BOUY_TENDER:
                if (eventIsSet) {
                    logger.info("About to show QR Code for buoy tender");
                    String branchIoUrl = BranchIOConstants.BUOYPINGER_APP_BRANCHIO + "?"
                            + BranchIOConstants.BUOYPINGER_APP_BRANCHIO_PATH + "=" + place.getEncodedCheckInUrl();
                    view.showBouyTender(event, place.getLeaderboardName(), branchIoUrl);
                } else {
                    logger.info("Event is missing for buoy tender QR Code");
                }
                break;
            case COMPETITOR:
            case COMPETITOR_2:
                if (participantIsSet && eventIsSet) {
                    logger.info("About to show QR Code for competitor/boat/mark tracking");
                    String sailInsightBranch = place.getMode() == InvitationMode.COMPETITOR
                            ? BranchIOConstants.SAILINSIGHT_APP_BRANCHIO
                            : BranchIOConstants.SAILINSIGHT_2_APP_BRANCHIO;
                    String branchIoUrl = sailInsightBranch + "?"
                            + BranchIOConstants.SAILINSIGHT_APP_BRANCHIO_PATH + "=" + place.getEncodedCheckInUrl();
                    view.showCompetitor(event, competitor, boat, mark, place.getLeaderboardName(), branchIoUrl);
                }
                break;
            case PUBLIC_INVITE:
                logger.info("About to show QR Code for public regatta invite");
                view.showPublic(place.getPublicRegattaName(), place.getPublicInviteBranchIOUrl());
                break;
            }
        }
    }

}
