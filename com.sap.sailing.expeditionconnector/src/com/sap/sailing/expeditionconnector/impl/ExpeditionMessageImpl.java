package com.sap.sailing.expeditionconnector.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.expeditionconnector.ExpeditionMessage;

public class ExpeditionMessageImpl implements ExpeditionMessage {
    private final int boatID;
    private final Map<Integer, Double> values;
    private final boolean valid;
    
    public ExpeditionMessageImpl(int boatID, Map<Integer, Double> values, boolean valid) {
        this.boatID = boatID;
        // ensure that nobody can manipulate the map used by this message object from outside
        this.values = new HashMap<Integer, Double>(values);
        this.valid = valid;
    }
    
    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public int getBoatID() {
        return boatID;
    }

    @Override
    public Set<Integer> getVariableIDs() {
        return Collections.unmodifiableSet(values.keySet());
    }

    @Override
    public boolean hasValue(int variableID) {
        return values.containsKey(variableID);
    }

    @Override
    public double getValue(int variableID) {
        if (!hasValue(variableID)) {
            throw new IllegalArgumentException("Variable ID "+variableID+" not present in message");
        }
        return values.get(variableID);
    }

}
