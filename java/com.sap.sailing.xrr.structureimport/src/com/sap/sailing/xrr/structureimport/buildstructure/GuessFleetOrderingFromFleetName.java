package com.sap.sailing.xrr.structureimport.buildstructure;

import java.util.NoSuchElementException;

import com.sap.sailing.domain.common.FleetColors;

public class GuessFleetOrderingFromFleetName implements GuessFleetOrderingStrategy {
    @Override
    public int guessOrder(String fleetName) {
        int result;
        try {
            final FleetColors recognizedFleetColor = FleetColors.valueOf(fleetName.toUpperCase());
            result = recognizedFleetColor.getDefaultOrderNo();
        } catch (IllegalArgumentException e) {
            result = 0;
        }
        return result;
    }
}
