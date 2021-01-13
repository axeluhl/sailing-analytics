package com.sap.sailing.landscape.procedures;

import com.sap.sse.landscape.ProcessConfigurationVariable;

public enum SailingProcessConfigurationVariables implements ProcessConfigurationVariable {
    /**
     * The user data variable that sets the UDP port to listen on, particularly for messages coming
     * from the navigation tool "Expedition"
     */
    EXPEDITION_PORT;
}
