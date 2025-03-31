package com.sap.sailing.gwt.home.communication.user.profile.domain;

import java.io.Serializable;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;

/**
 * This object contains all relevant data of a regatta, a competitor ({@link #competitorDto}) participated in.
 * Additionally to the name of the regatta and the unique IDs of the regatta and the containing event, this objects
 * contains information about the rank and points of the participating competitor.
 */
public class ParticipatedRegattaDTO implements Serializable {
    private static final long serialVersionUID = -126348629933556831L;

    private String regattaName;
    private int regattaRank;
    private SimpleCompetitorWithIdDTO competitorDto;
    private String regattaId;
    private String eventId;
    private double sumPoints;

    protected ParticipatedRegattaDTO() {
    }

    public ParticipatedRegattaDTO(String regattaName, int regattaRank, SimpleCompetitorWithIdDTO competitorDto,
            String regattaId, String eventId, double sumPoints) {
        super();
        this.regattaName = regattaName;
        this.regattaRank = regattaRank;
        this.competitorDto = competitorDto;
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
