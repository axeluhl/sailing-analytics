package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;

public class ParticipatedRegattaDTO {

    private final String regattaName;
    private final int regattaRank;
    private final SimpleCompetitorWithIdDTO competitorDto;
    private final String clubName;
    private final String regattaId;
    private final String eventId;
    private final double sumPoints;

    public ParticipatedRegattaDTO(String regattaName, int regattaRank, SimpleCompetitorWithIdDTO competitorDto,
            String clubName, String regattaId, String eventId, double sumPoints) {
        super();
        this.regattaName = regattaName;
        this.regattaRank = regattaRank;
        this.competitorDto = competitorDto;
        this.clubName = clubName;
        this.regattaId = regattaId;
        this.eventId = eventId;
        this.sumPoints = sumPoints;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public int getRegattaRank() {
        return regattaRank;
    }

    public SimpleCompetitorWithIdDTO getCompetitorDto() {
        return competitorDto;
    }

    public String getClubName() {
        return clubName;
    }

    public String getRegattaId() {
        return regattaId;
    }

    public String getEventId() {
        return eventId;
    }

    public double getSumPoints() {
        return sumPoints;
    }

}
