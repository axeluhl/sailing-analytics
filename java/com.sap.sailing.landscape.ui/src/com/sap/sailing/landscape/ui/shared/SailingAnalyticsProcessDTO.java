package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.TimePoint;

public class SailingAnalyticsProcessDTO extends ApplicationProcessDTO implements IsSerializable {
    private int expeditionUdpPort;
    
    @Deprecated
    SailingAnalyticsProcessDTO() {} // for GWT RPC serialization only

    public SailingAnalyticsProcessDTO(AwsInstanceDTO host, int port, String hostname, String releaseName,
            int telnetPortToOSGiConsole, String serverName, String serverDirectory, int expeditionUdpPort, TimePoint startTimePoint) {
        super(host, port, hostname, releaseName, telnetPortToOSGiConsole, serverName, serverDirectory, startTimePoint);
        this.expeditionUdpPort = expeditionUdpPort;
    }

    public int getExpeditionUdpPort() {
        return expeditionUdpPort;
    }
}
