package com.sap.sailing.gwt.home.desktop.places.qrcode;

import java.util.UUID;

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
        if (place.getMode() == InvitationMode.PUBLIC_INVITE) {
            // as the event is most likely displayed on a different server anyway, do not load additional data
            dataCollector = new DataCollector(view);
            dataCollector.proceedIfFinished();
        } else {
            if (place.getMode() == InvitationMode.COMPETITOR
                    && (place.getEncodedCheckInUrl() == null || place.getEncodedCheckInUrl().equals(""))) {
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
                    // try to display anyway
                    dataCollector.setCompetitor(null);
                }
                retrieveEvent(place.getEventId());
            }
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
        clientFactory.getDispatch().execute(new GetEventViewAction(eventId), new AsyncCallback<EventViewDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                dataCollector.setEvent(null);
            }

            @Override
            public void onSuccess(EventViewDTO result) {
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
                        dataCollector.setCompetitor(null);
                    }

                    @Override
                    public void onSuccess(SimpleCompetitorWithIdDTO result) {
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
            InvitationMode invitationMode = place.getMode();
            String regattaName = place.getRegattaName();
            String regattaRegistrationLinkSecret = place.getRegattaRegistrationLinkSecret();
            String serverForPublic = place.getTargetServer();
            String checkInUrl = place.getEncodedCheckInUrl();
            String leaderBoardName = place.getLeaderboardName();
            UUID eventId = place.getEventId();
            UUID competitorId = place.getCompetitorId();
            if (invitationMode == InvitationMode.PUBLIC_INVITE) {
                String branchIoUrl = BranchIOConstants.OPEN_REGATTA_2_APP_BRANCHIO + "?"
                        + QRCodePlace.PARAM_REGATTA_NAME + "=" + regattaName + "&" + QRCodePlace.PARAM_REGATTA_SECRET
                        + "=" + regattaRegistrationLinkSecret + "&" + QRCodePlace.PARAM_SERVER + "=" + serverForPublic;
                view.setData(null, null, null, null, regattaName, branchIoUrl, invitationMode);
            } else {
                if (participantIsSet && eventIsSet) {
                    if (checkInUrl != null) {
                        String branchIoUrl = null;
                        switch (invitationMode) {
                        case BOUY_TENDER:
                            branchIoUrl = BranchIOConstants.BUOYPINGER_APP_BRANCHIO + "?"
                                    + BranchIOConstants.BUOYPINGER_APP_BRANCHIO_PATH + "=" + checkInUrl;
                            break;
                        case COMPETITOR:
                            branchIoUrl = BranchIOConstants.SAILINSIGHT_APP_BRANCHIO + "?"
                                    + BranchIOConstants.SAILINSIGHT_APP_BRANCHIO_PATH + "=" + checkInUrl;
                            break;
                        case COMPETITOR_2:
                            branchIoUrl = BranchIOConstants.SAILINSIGHT_2_APP_BRANCHIO + "?"
                                    + BranchIOConstants.SAILINSIGHT_APP_BRANCHIO_PATH + "=" + checkInUrl;
                            break;
                        default:
                            break;
                        }
                        view.setData(event, competitor, boat, mark, leaderBoardName, branchIoUrl, invitationMode);
                    } else {
                        view.setError();
                        GWT.log("checkInUrl " + checkInUrl);
                        GWT.log("competitorId " + competitorId);
                        GWT.log("boatId " + boat);
                        GWT.log("invitationMode " + invitationMode);
                        GWT.log("eventId " + eventId);
                    }
                }
            }
        }
    }

}
