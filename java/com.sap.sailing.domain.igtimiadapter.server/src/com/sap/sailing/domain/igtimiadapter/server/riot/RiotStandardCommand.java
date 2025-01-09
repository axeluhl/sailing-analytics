package com.sap.sailing.domain.igtimiadapter.server.riot;

public enum RiotStandardCommand {
    CMD_POWER_OFF("POWER OFF"),
    CMD_GPS_OFF("GPS OFF"),
    CMD_GPS_ON("GPS ON"),
    CMD_RESTART("RESTART");

    RiotStandardCommand(String command) {
        this.command = command;
    }
    
    public String getCommand() {
        return command;
    }

    private final String command;
}
