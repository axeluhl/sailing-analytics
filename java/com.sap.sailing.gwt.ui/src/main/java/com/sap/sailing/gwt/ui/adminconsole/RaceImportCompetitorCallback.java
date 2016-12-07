package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class RaceImportCompetitorCallback extends ImportCompetitorCallback {
    private final String leaderboardName;
    private final String raceColumnName;
    private final String fleetName;
    private final String boatClassName;

    public RaceImportCompetitorCallback(String leaderboardName, String raceColumnName, String fleetName,
            String boatClassName, SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        super(sailingService, errorReporter, stringMessages);
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        this.boatClassName = boatClassName;
    }

    @Override
    public List<CompetitorDTO> prepareCompetitorsForSaving(Set<CompetitorDTO> competitors) {
        List<CompetitorDTO> competitorsForSaving = super.prepareCompetitorsForSaving(competitors);
        return getCompetitorsWithNewBoatClass(competitorsForSaving);
    }

    private List<CompetitorDTO> getCompetitorsWithNewBoatClass(List<CompetitorDTO> competitors) {
        List<CompetitorDTO> newCompetitors = new ArrayList<>();
        for (CompetitorDTO competitor : competitors) {
            final BoatClassMasterdata boatClassMasterdata = BoatClassMasterdata.resolveBoatClass(boatClassName);
            if (boatClassMasterdata == null) {
                return competitors;
            }

            BoatClassDTO boatClass = new BoatClassDTO(boatClassMasterdata.getDisplayName(),
                    boatClassMasterdata.getHullLength());
            CompetitorDTO newCompetitor = new CompetitorDTOImpl(competitor.getName(), competitor.getColor(),
                    competitor.getEmail(), competitor.getTwoLetterIsoCountryCode(),
                    competitor.getThreeLetterIocCountryCode(), competitor.getCountryName(), competitor.getIdAsString(),
                    competitor.getImageURL(), competitor.getFlagImageURL(), competitor.getBoat(), boatClass,
                    competitor.getTimeOnTimeFactor(), competitor.getTimeOnDistanceAllowancePerNauticalMile(),
                    competitor.getSailID());
            newCompetitors.add(newCompetitor);
        }
        return newCompetitors;
    }

    @Override
    protected void registerCompetitors(Set<CompetitorDTO> competitors) {
        sailingService.setCompetitorRegistrationsInRaceLog(leaderboardName, raceColumnName, fleetName, competitors,
                new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.couldNotSaveCompetitorRegistrations(caught.getMessage()));
                    }
                });
    }
}
