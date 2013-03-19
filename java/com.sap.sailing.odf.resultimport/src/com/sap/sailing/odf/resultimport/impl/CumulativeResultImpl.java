package com.sap.sailing.odf.resultimport.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.odf.resultimport.Athlete;
import com.sap.sailing.odf.resultimport.Competitor;
import com.sap.sailing.odf.resultimport.CumulativeResult;
import com.sap.sailing.odf.resultimport.Result;

public class CumulativeResultImpl extends AbstractNodeWrapper implements CumulativeResult {
    private final Competitor competitor;

    public CumulativeResultImpl(Node node) {
        super(node);
        this.competitor = new CompetitorImpl(((Element) getNode()).getElementsByTagName("Competitor").item(0));
    }

    @Override
    public int getRank() {
        return Integer.valueOf(getNode().getAttributes().getNamedItem("Rank").getNodeValue());
    }

    @Override
    public Result getResult() {
        final String resultType = getNode().getAttributes().getNamedItem("ResultType").getNodeValue();
        if ("POINTS".equals(resultType)) {
            return new PointsResultImpl(Double.valueOf(getNode().getAttributes().getNamedItem("Result").getNodeValue()));
        } else {
            throw new IllegalArgumentException("Don't know yet how to deal with result type "+resultType);
        }
    }

    @Override
    public int getSortOrder() {
        return Integer.valueOf(getNode().getAttributes().getNamedItem("SortOrder").getNodeValue());
    }

    @Override
    public CompetitorType getCompetitorType() {
        return competitor.getCompetitorType();
    }
    
    @Override
    public String getCompetitorCode() {
        return competitor.getCode();
    }

    @Override
    public CountryCode getCountryCode() {
        return competitor.getCountryCode();
    }

    @Override
    public Iterable<Triple<Double, Integer, MaxPointsReason>> getPointsAndRanksAfterEachRace() {
        return competitor.getPointsAndRanksAndMaxPointsAfterEachRace();
    }

    @Override
    public Double getPointsInMedalRace() {
        return competitor.getPointsInMedalRace();
    }

    @Override
    public Iterable<Athlete> getAthletes() {
        List<Athlete> result = new ArrayList<>();
        Node composition = ((Element) getNode()).getElementsByTagName("Composition").item(0);
        NodeList athletes = ((Element) composition).getElementsByTagName("Athlete");
        for (int i=0; i<athletes.getLength(); i++) {
            final Node athleteNode = athletes.item(i);
            AthleteImpl athlete = new AthleteImpl(athleteNode);
            if ("Skipper".equals(athlete.getExtendedResults("ER_SA").get("SA_POSITION").firstEntry().getValue())) {
                result.add(new SkipperImpl(athleteNode));
            } else {
                result.add(new CrewmemberImpl(athleteNode));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Cumulative Results for competitor "+getCompetitorCode()+" ("+getCountryCode()+") "+getAthletes()+": Rank: "+getRank()+", Result: "+getResult()+", sort order "+getSortOrder()
                +", race results: "+getPointsAndRanksAfterEachRace()+", medal race results: "+getPointsInMedalRace();
    }

    @Override
    public Double getTotalPoints() {
        TreeMap<Integer, String> totalPoints = getExtendedResults("ER_SA").get("SA_TOTAL_POINTS");
        final Double result;
        if (totalPoints == null) {
            result = null;
        } else {
            result = Double.valueOf(totalPoints.firstEntry().getValue());
        }
        return result;
    }

    
}
