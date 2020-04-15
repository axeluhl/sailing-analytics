package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.details.events;

import java.util.Arrays;
import java.util.HashSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedRegattaDTO;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.events.CompetitorWithoutClubnameItemDescription;
import com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.SailorProfileMobileResources;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;

/**
 * regatta entry on mobile in the sailor profile details: displays a single regatta in the context of a single event
 * with name, rank, competitor, club name, points and a leaderboard button.
 */
public class SailorProfileRegattaEntry extends Composite {

    private static SailorProfileOverviewEntryUiBinder uiBinder = GWT.create(SailorProfileOverviewEntryUiBinder.class);

    interface SailorProfileOverviewEntryUiBinder extends UiBinder<Widget, SailorProfileRegattaEntry> {
    }

    @UiField
    DivElement regattaNameUi;

    @UiField
    DivElement regattaRankUi;

    @UiField
    FlowPanel competitorUi;

    @UiField
    DivElement clubNameUi;

    @UiField
    DivElement sumPointsUi;

    @UiField
    Button showLeaderboardButtonUi;

    @UiField
    HTMLPanel contentContainerRegattasUi;

    private final String eventId, regattaId, competitorId;

    private final PlaceController placeController;

    public SailorProfileRegattaEntry(ParticipatedRegattaDTO regatta, PlaceController placeController,
            FlagImageResolver flagImageResolver) {
        initWidget(uiBinder.createAndBindUi(this));
        SailorProfileMobileResources.INSTANCE.css().ensureInjected();
        this.placeController = placeController;
        this.regattaId = regatta.getRegattaId();
        this.eventId = regatta.getEventId();
        this.competitorId = regatta.getCompetitorDto().getIdAsString();
        this.regattaNameUi.setInnerText(regatta.getRegattaName());
        this.regattaRankUi.setInnerText("Rank " + regatta.getRegattaRank());
        CompetitorWithoutClubnameItemDescription competitorDescription = new CompetitorWithoutClubnameItemDescription(
                regatta.getCompetitorDto(), flagImageResolver);
        competitorDescription.fixFlagPosition(-2);
        this.competitorUi.add(competitorDescription);
        this.clubNameUi.setInnerText(regatta.getCompetitorDto().getName());
        this.sumPointsUi.setInnerText("" + regatta.getSumPoints());

    }

    @UiHandler("showLeaderboardButtonUi")
    void onClick(ClickEvent e) {
        placeController
                .goTo(new RegattaLeaderboardPlace(eventId, regattaId, new HashSet<>(Arrays.asList(competitorId))));
    }

}
