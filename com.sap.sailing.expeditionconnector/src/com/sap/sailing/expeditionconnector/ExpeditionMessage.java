package com.sap.sailing.expeditionconnector;

import java.util.Set;

public interface ExpeditionMessage {
    /**
     * A message's checksum determines whether the package is to be considered valid.
     */
    boolean isValid();

    /**
     * The ID of the boat that sent this message
     */
    int getBoatID();

    /**
     * Lists all variable IDs for which this message has a value
     */
    Set<Integer> getVariableIDs();

    /**
     * Tells if <code>variableID</code> appears in {@link #getVariableIDs()}.
     */
    boolean hasValue(int variableID);

    /**
     * If {@link #hasValue(int)} is <code>true</code> for <code>variableID</code>, the variable's value is returned.
     * Otherwise, an {@link IllegalArgumentException} is thrown.
     */
    double getValue(int variableID);
}
