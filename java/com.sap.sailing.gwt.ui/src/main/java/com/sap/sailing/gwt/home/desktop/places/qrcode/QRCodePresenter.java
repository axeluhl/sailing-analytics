package com.sap.sailing.gwt.home.desktop.places.qrcode;

import java.util.UUID;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.BranchIOConstants;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.gwt.home.communication.event.GetEventViewAction;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.user.profile.GetCompetitorAction;
import com.sap.sailing.gwt.home.desktop.places.qrcode.QRCodePlace.InvitationMode;
import com.sap.sailing.gwt.ui.shared.MarkDTO;

public class QRCodePresenter {
    private final QRCodeClientFactory clientFactory;
    private DataCollector dataCollector;
    private QRCodePlace place;

    public QRCodePresenter(QRCodeClientFactory clientFactory, QRCodePlace place) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    public void setView(QRCodeView view) {
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
                } else {
                    dataCollector.setCompetitor(null);
                }
                if (place.getBoatId() != null) {
                    retrieveBoat(place.getBoatId());
                } else {
                    dataCollector.setBoat(null);
                }
                if (place.getMarkId() != null) {
                    retrieveMark(place.getMarkId());
                } else {
                    dataCollector.setMark(null);
                }
                retrieveEvent(place.getEventId());
            }
        }
    }

    private void retrieveMark(UUID markId) {
        // FIXME after it is decided, how the mark will be resolved on the archive server for races on an eventserver
        dataCollector.setMark(null);
    }

    private void retrieveBoat(UUID boatId) {
        // FIXME after it is decided, how the boat will be resolved on the archive server for races on an eventserver
        dataCollector.setBoat(null);
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
            String serverForPublic = place.getServer();
            String checkInUrl = place.getEncodedCheckInUrl();
            String leaderBoardName = place.getLeaderboardName();
            UUID eventId = place.getEventId();
            UUID competitorId = place.getCompetitorId();
            if (invitationMode == InvitationMode.PUBLIC_INVITE) {
                String branchIoUrl = BranchIOConstants.OPEN_REGATTA_2_APP_BRANCHIO + "?"
                        + QRCodePlace.PARAM_REGATTA_NAME + "=" + regattaName + "&" + QRCodePlace.PARAM_REGATTA_SECRET
                        + "=" + regattaRegistrationLinkSecret + "&" + QRCodePlace.PARAM_SERVER + "=" + serverForPublic;
                view.setData(null, null, regattaName, branchIoUrl, invitationMode);
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
                        String participant = "";
                        if (competitor != null) {
                            participant = competitor.getName();
                        }
                        if (boat != null) {
                            participant = boat.getDisplayName();
                        }
                        if (mark != null) {
                            participant = mark.getName();
                        }
                        view.setData(event, participant, leaderBoardName, branchIoUrl, invitationMode);
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
