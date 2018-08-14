package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SwissTimingConfigurationDTO implements IsSerializable {
    private String name;
    private String jsonURL;
    private String hostname;
    private Integer port;
    
    /**
     * The URL to use for updates fed back to the SwissTiming server, such as changes of race timings
     * or course updates.
     */
    private String updateURL;
    
    /**
     * The username to use as part of the credentials for requests to the {@link #updateURL}.
     */
    private String updateUsername;

    /**
     * The password to use as part of the credentials for requests to the {@link #updateURL}.
     */
    private String updatePassword;

    public SwissTimingConfigurationDTO() {}

    public SwissTimingConfigurationDTO(String name, String jsonURL, String hostname, Integer port, String updateURL, String updateUsername, String updatePassword) {
        super();
        this.name = name;
        this.jsonURL = jsonURL;
        this.hostname = hostname;
        this.port = port;
        this.updateURL = updateURL;
        this.updateUsername = updateUsername;
        this.updatePassword = updatePassword;
    }

    public String getName() {
        return name;
    }

    public String getJsonURL() {
        return jsonURL;
    }

    public String getHostname() {
        return hostname;
    }

    public Integer getPort() {
        return port;
    }

    public String getUpdateURL() {
        return updateURL;
    }

    public String getUpdateUsername() {
        return updateUsername;
    }

    public String getUpdatePassword() {
        return updatePassword;
    }
}
