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
    IGTIMI_RIOT_PORT,
    
    /**
     * Use this variable to override the default {@code https://wind.sapsailing.com} base URL for obtaining
     * wind data from Igtimi devices. Authentication to this service by default will assume shared security
     * and therefore shared user bases with shared access tokens. If this does not apply to your set-up,
     * consider using {@link #IGTIMI_BEARER_TOKEN} in addition.
     */
    IGTIMI_BASE_URL,
    
    /**
     * Overrides the default authentication scheme for requests against the remote "Riot" service for Igtimi
     * wind connectivity whose base URL may be overridden using {@link #IGTIMI_BASE_URL}. Specify a bearer token
     * valid in the context of the security service of the remote Riot service.
     */
    IGTIMI_BEARER_TOKEN;
}
