package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.AbstractLeaderboardConfigPanel.RaceColumnDTOAndFleetDTOWithNameBasedEquality;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.ErrorReporter;

public class CourseAndCompetitorCopyOperation {

    final Set<RaceColumnDTOAndFleetDTOWithNameBasedEquality> raceLogsToCopyTo;
    final boolean copyCourse;
    final boolean copyCompetitors;
    final Integer priority;
    final private ErrorReporter errorReporter;
    final private SailingServiceAsync sailingService;

    public CourseAndCompetitorCopyOperation(Set<RaceColumnDTOAndFleetDTOWithNameBasedEquality> racesToCopyTo,
            boolean copyCourse, boolean copyCompetitors, Integer priority, SailingServiceAsync sailingService, ErrorReporter errorReporter) {
        this.raceLogsToCopyTo = racesToCopyTo;
        this.copyCourse = copyCourse;
        this.copyCompetitors = copyCompetitors;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.priority = priority;
    }

    public Set<RaceColumnDTOAndFleetDTOWithNameBasedEquality> getRaceLogsToCopyTo() {
        return raceLogsToCopyTo;
    }

    public boolean copyCourse() {
        return copyCourse;
    }

    public boolean copyCompetitors() {
        return copyCompetitors;
    }
    
    public Integer getPriority() {
        return priority;
    }

    private Set<Triple<String, String, String>> convertToRacelogs(String leaderboardName) {
        Set<Util.Triple<String, String, String>> toRaceLogs = new java.util.HashSet<>();
        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality race : raceLogsToCopyTo) {
            toRaceLogs.add(toTriple(leaderboardName, race));
        }
        return toRaceLogs;
    }

    private Util.Triple<String, String, String> toTriple(String leaderboardName,
            RaceColumnDTOAndFleetDTOWithNameBasedEquality race) {
        return new Util.Triple<String, String, String>(leaderboardName, race.getA().getName(), race.getB().getName());
    }

    public void perform(String leaderboardName, RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnDTOAndFleetDTO,
            final Runnable onSuccessCallback) {
        Triple<String, String, String> fromTriple = toTriple(leaderboardName, raceColumnDTOAndFleetDTO);
        Set<Triple<String, String, String>> toRacelogs = convertToRacelogs(leaderboardName);
        if (copyCourse) {
            sailingService.copyCourseToOtherRaceLogs(fromTriple, toRacelogs, getPriority(), new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Could not copy course and competitors: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                }
            });
        }
        if (copyCompetitors) {
            sailingService.copyCompetitorsToOtherRaceLogs(fromTriple, toRacelogs, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Could not copy course and competitors: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                }
            });
        }
    }
}