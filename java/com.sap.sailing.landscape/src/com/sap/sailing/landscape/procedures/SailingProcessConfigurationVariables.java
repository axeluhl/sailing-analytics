package com.sap.sailing.landscape.procedures;

import com.sap.sse.landscape.ProcessConfigurationVariable;

public enum SailingProcessConfigurationVariables implements ProcessConfigurationVariable {
    /**
     * The user data variable that sets the UDP port to listen on, particularly for messages coming
     * from the navigation tool "Expedition"
     */
    EXPEDITION_PORT,
    
    /**
     * The user data variable that sets the TCP port to listen on for Igtimi Riot (WindBot) connections;
     * a typical port would be 6000 which is the default for the WindBot devices. If not specified, the
     * Riot server will listen on any available, unused server port. Maps to the igtimi.riot.port property.
     */
    IGTIMI_RIOT_PORT;
}
