package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO for a race log with all raw race log entries
 */
public class RegattaLogDTO extends AbstractLogDTO<RegattaLogEventDTO> implements IsSerializable {
    RegattaLogDTO() {}

    public RegattaLogDTO(String leaderboardName, List<RegattaLogEventDTO> entries) {
        super(leaderboardName, entries);
    }
}
