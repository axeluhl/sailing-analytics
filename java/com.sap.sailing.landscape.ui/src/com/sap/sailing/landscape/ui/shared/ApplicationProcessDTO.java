package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.TimePoint;

public class ApplicationProcessDTO extends ProcessDTO implements IsSerializable {
    private String releaseName;
    private int telnetPortToOSGiConsole;
    private String serverName;
    private String serverDirectory;
    private TimePoint startTimePoint;
    
    @Deprecated
    ApplicationProcessDTO() {} // for GWT RPC serialization only

    public ApplicationProcessDTO(AwsInstanceDTO host, int port, String hostname, String releaseName,
            int telnetPortToOSGiConsole, String serverName, String serverDirectory, TimePoint startTimePoint) {
        super(host, port, hostname);
        this.releaseName = releaseName;
        this.telnetPortToOSGiConsole = telnetPortToOSGiConsole;
        this.serverName = serverName;
        this.serverDirectory = serverDirectory;
        this.startTimePoint = startTimePoint;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public int getTelnetPortToOSGiConsole() {
        return telnetPortToOSGiConsole;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerDirectory() {
        return serverDirectory;
    }

    public TimePoint getStartTimePoint() {
        return startTimePoint;
    }
}
