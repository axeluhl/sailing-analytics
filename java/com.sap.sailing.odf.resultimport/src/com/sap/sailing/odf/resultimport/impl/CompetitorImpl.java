package com.sap.sailing.odf.resultimport.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.CountryCodeFactory;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.odf.resultimport.Competitor;
import com.sap.sailing.odf.resultimport.CumulativeResult.CompetitorType;

public class CompetitorImpl extends AbstractNodeWrapper implements Competitor {

    public CompetitorImpl(Node node) {
        super(node);
    }

    @Override
    public CompetitorType getCompetitorType() {
        return CompetitorType.valueOf(((Element) getNode()).getAttribute("Type"));
    }
    
    @Override
    public String getCode() {
        return ((Element) getNode()).getAttribute("Code");
    }

    @Override
    public CountryCode getCountryCode() {
        return CountryCodeFactory.INSTANCE.getFromThreeLetterIOCName((((Element) getNode()).getAttribute("Bib")));
    }

    @Override
    public Double getPointsInMedalRace() {
        final TreeMap<Integer, String> pointsInMedalRace = getExtendedResults("ER_SA").get("SA_POINTS_IN_MEDAL_RACE");
        String medalPoints = pointsInMedalRace == null ? null : pointsInMedalRace.firstEntry().getValue();
        return medalPoints == null ? null : Double.valueOf(medalPoints);
    }

    @Override
    public Iterable<Triple<Double, Integer, MaxPointsReason>> getPointsAndRanksAndMaxPointsAfterEachRace() {
        List<Triple<Double, Integer, MaxPointsReason>> result = new ArrayList<>();
        TreeMap<Integer, String> points = getExtendedResults("ER_SA").get("SA_POINTS_IN_RACE");
        TreeMap<Integer, String> ranks = getExtendedResults("ER_SA").get("SA_RANK_AFTER_RACE");
        TreeMap<Integer, String> irmStatus = getExtendedResults("ER_SA").get("SA_IRM_STATUS_IN_RACE");
        Iterator<Entry<Integer, String>> pointsIter = points.entrySet().iterator();
        Iterator<Entry<Integer, String>> ranksIter = ranks.entrySet().iterator();
        while (pointsIter.hasNext() && ranksIter.hasNext()) {
            final Entry<Integer, String> pointsEntry = pointsIter.next();
            final Entry<Integer, String> ranksEntry = ranksIter.next();
            final String maxPointsReasonAsString = irmStatus == null ? null : irmStatus.get(pointsEntry.getKey());
            final MaxPointsReason maxPointsReason = maxPointsReasonAsString == null ? MaxPointsReason.NONE :
                MaxPointsReason.valueOf(maxPointsReasonAsString);
            result.add(new Triple<Double, Integer, MaxPointsReason>(
                    Double.valueOf(pointsEntry.getValue()), Integer.valueOf(ranksEntry.getValue()),
                    maxPointsReason));
        }
        return result;
    }
}
