package com.sap.sailing.gwt.managementconsole.services.factories;

import java.util.ArrayList;
import java.util.Collections;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Util;

public abstract class RegattaFactory {

    public static RegattaDTO createDefaultRegatta(String regattaName, String boatClassName, final RankingMetrics ranking, final ScoringSchemeType scoringSystem, 
            final SeriesDTO series, final EventDTO event) {
        RegattaDTO regatta = new RegattaDTO();
        regatta.setName(regattaName);
        regatta.boatClass = new BoatClassDTO(boatClassName, Distance.NULL, Distance.NULL);
        regatta.rankingMetricType = ranking;
        regatta.scoringScheme = scoringSystem;
        regatta.series = Collections.singletonList(series);
        regatta.useStartTimeInference = false;
        regatta.controlTrackingFromStartAndFinishTimes = false;
        regatta.autoRestartTrackingUponCompetitorSetChange = false;
        regatta.canBoatsOfCompetitorsChangePerRace = false;
        regatta.buoyZoneRadiusInHullLengths = Regatta.DEFAULT_BUOY_ZONE_RADIUS_IN_HULL_LENGTHS;      
        regatta.courseAreas = new ArrayList<>();
        Util.addAll(event.venue.getCourseAreas(), regatta.courseAreas);
        return regatta;
    }
    
}
