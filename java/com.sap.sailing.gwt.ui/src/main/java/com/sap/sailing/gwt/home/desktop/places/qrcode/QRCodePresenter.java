package com.sap.sailing.gwt.home.desktop.places.qrcode;

import java.util.UUID;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.BranchIOConstants;
import com.sap.sailing.gwt.home.communication.event.GetEventViewAction;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.user.profile.GetCompetitorAction;
import com.sap.sailing.gwt.home.desktop.places.qrcode.QRCodePlace.InvitationMode;

public class QRCodePresenter {

    private UUID eventId, competitorId;
    private String regattaName, regattaRegistrationLinkSecret;
    private String leaderboardNameFromUrl, checkInUrl;
    private final QRCodeClientFactory clientFactory;
    private DataCollector dataCollector;
    private InvitationMode invitationMode;
    private String serverForPublic;

    public QRCodePresenter(UUID eventId, UUID competitorId, String leaderboardName,
            String regattaName, String regattaRegistrationLinkSecret,
            String checkInUrl, QRCodeClientFactory clientFactory, InvitationMode invitationMode,
            String serverForPublic) {
        this.serverForPublic = serverForPublic;
        this.eventId = eventId;
        this.checkInUrl = checkInUrl;
        this.competitorId = competitorId;
        this.leaderboardNameFromUrl = leaderboardName;
        this.regattaName = regattaName;
        this.regattaRegistrationLinkSecret = regattaRegistrationLinkSecret;
        this.clientFactory = clientFactory;
        this.invitationMode = invitationMode;
    }

    public void setView(QRCodeView view) {
        if (invitationMode == InvitationMode.PUBLIC_INVITE) {
            // as the event is most likely displayed on a different server anyway, do not load additional data
            dataCollector = new DataCollector(view);
            dataCollector.proceedIfFinished();
        } else {
        if (eventId == null
                || (invitationMode == InvitationMode.COMPETITOR || invitationMode == InvitationMode.COMPETITOR_2)
                        && competitorId == null
                    || leaderboardNameFromUrl == null || leaderboardNameFromUrl.equals("") || checkInUrl == null
                    || checkInUrl.equals("")) {
                view.setError();
            } else {
                dataCollector = new DataCollector(view);
                if (competitorId != null) {
                    retrieveCompetitor(competitorId);
                } else {
                    dataCollector.setCompetitor(null);
                }
                retrieveEvent(eventId);
            }
        }
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
        private boolean competitorIsSet = false;

        private SimpleCompetitorWithIdDTO competitor;
        private EventViewDTO event;

        private final QRCodeView view;

        public DataCollector(QRCodeView view) {
            this.view = view;
        }

        public void setCompetitor(SimpleCompetitorWithIdDTO competitor) {
            competitorIsSet = true;
            this.competitor = competitor;
            proceedIfFinished();
        }

        public void setEvent(EventViewDTO event) {
            eventIsSet = true;
            this.event = event;
            proceedIfFinished();
        }

        private void proceedIfFinished() {
            if (invitationMode == InvitationMode.PUBLIC_INVITE) {
                String branchIoUrl = BranchIOConstants.OPEN_REGATTA_2_APP_BRANCHIO + "?"
                        + QRCodePlace.PARAM_REGATTA_NAME + "=" + regattaName + "&" + QRCodePlace.PARAM_REGATTA_SECRET
                        + "=" + regattaRegistrationLinkSecret + "&" + QRCodePlace.PARAM_SERVER + "=" + serverForPublic;
                view.setData(null, null, "", regattaName, branchIoUrl, invitationMode);
            } else {
                if (competitorIsSet && eventIsSet) {
                    if (checkInUrl != null) {
                        String branchIoUrl = null;
                        switch (invitationMode) {
                        case BOUY_TENDER:
                            branchIoUrl = BranchIOConstants.BUOYPINGER_APP_BRANCHIO + "?"
                                    + BranchIOConstants.BUOYPINGER_APP_BRANCHIO_PATH + "="
                                    + QRCodePresenter.this.checkInUrl;
                            break;
                        case COMPETITOR:
                            branchIoUrl = BranchIOConstants.SAILINSIGHT_APP_BRANCHIO + "?"
                                    + BranchIOConstants.SAILINSIGHT_APP_BRANCHIO_PATH + "="
                                    + QRCodePresenter.this.checkInUrl;
                            break;
                        case COMPETITOR_2:
                            branchIoUrl = BranchIOConstants.SAILINSIGHT_2_APP_BRANCHIO + "?"
                                    + BranchIOConstants.SAILINSIGHT_APP_BRANCHIO_PATH + "="
                                    + QRCodePresenter.this.checkInUrl;
                            break;
                        default:
                            break;
                        }
                        view.setData(event, competitor, leaderboardNameFromUrl, null, branchIoUrl, invitationMode);
                    } else {
                        view.setError();
                        GWT.log("checkInUrl " + checkInUrl);
                        GWT.log("competitorId " + competitorId);
                        GWT.log("invitationMode " + invitationMode);
                        GWT.log("eventId " + eventId);
                    }
                }
            }
        }
    }

}
