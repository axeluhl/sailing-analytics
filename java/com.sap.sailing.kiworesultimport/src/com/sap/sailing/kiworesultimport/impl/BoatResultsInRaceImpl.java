package com.sap.sailing.kiworesultimport.impl;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.kiworesultimport.BoatResultInRace;

public class BoatResultsInRaceImpl extends AbstractNodeWrapper implements BoatResultInRace {
    private static final Logger logger = Logger.getLogger(BoatResultsInRaceImpl.class.getName());
    private static final Pattern leadingPercentagePattern = Pattern.compile("^[0-9][0-9]*%.*$");
    private static final Pattern pointsPattern = Pattern.compile("[0-9]*(,[0-9]*)");

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
        Matcher m = pointsPattern.matcher(getNode().getAttributes().getNamedItem("points").getNodeValue());
        Double result = null;
        if (m.find()) {
            final String pointsAsString = m.group().replace(',', '.').trim();
            if (!pointsAsString.trim().equals("-")) {
                try {
                    result = Double.valueOf(pointsAsString);
                } catch (NumberFormatException nfe) {
                    logger.throwing(BoatResultsInRaceImpl.class.getName(), "getPoints", nfe);
                }
            }
        }
        return result;
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
            if (startsWithPercentage(status)) {
                result = MaxPointsReason.ZFP;
            } else {
                result = MaxPointsReason.valueOf(status.substring(0, 3));
            }
        } else {
            result = MaxPointsReason.NONE;
        }
        return result;
    }

    private boolean startsWithPercentage(String status) {
        return leadingPercentagePattern.matcher(status).matches();
    }

    @Override
    public String toString() {
        return "{" + getRaceNumber() + ": " + getMaxPointsReason() + ", " + getPoints() + (isDiscarded()?", discarded":"")+"}";
    }
}
