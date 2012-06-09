package com.sap.sailing.kiworesultimport.impl;

import org.w3c.dom.Node;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.kiworesultimport.BoatResultInRace;

public class BoatResultsInRaceImpl extends AbstractNodeWrapper implements BoatResultInRace {

    public BoatResultsInRaceImpl(Node node) {
        super(node);
    }

    @Override
    public boolean isDiscarded() {
        final String pointsAsString = getNode().getAttributes().getNamedItem("points").getNodeValue().trim();
        return pointsAsString.startsWith("(") && pointsAsString.endsWith(")");
    }

    @Override
    public Double getPoints() {
        final String pointsAsString = getNode().getAttributes().getNamedItem("points").getNodeValue().replace('(', ' ')
                .replace(')', ' ').replace(',', '.').trim();
        return Double.valueOf(pointsAsString);
    }

    @Override
    public Integer getRaceNumber() {
        return Integer.valueOf(getNode().getAttributes().getNamedItem("number").getNodeValue());
    }

    @Override
    public String getStatus() {
        final Node namedItem = getNode().getAttributes().getNamedItem("status");
        return namedItem == null || namedItem.getNodeValue() == null || namedItem.getNodeValue().trim().length() == 0 ? null
                : namedItem.getNodeValue();
    }

    @Override
    public MaxPointsReason getMaxPointsReason() {
        MaxPointsReason result;
        String status = getStatus();
        if (status != null) {
            result = MaxPointsReason.valueOf(status.substring(0, 3));
        } else {
            result = MaxPointsReason.NONE;
        }
        return result;
    }

    @Override
    public String toString() {
        return "{" + getRaceNumber() + ": " + getMaxPointsReason() + ", " + getPoints() + (isDiscarded()?", discarded":"")+"}";
    }
}
