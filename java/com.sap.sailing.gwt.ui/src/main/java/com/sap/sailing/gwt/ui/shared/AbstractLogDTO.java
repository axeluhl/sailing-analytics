package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO for a race log with all raw race log entries
 */
public class AbstractLogDTO<E extends AbstractLogEventDTO> implements IsSerializable {
    private String leaderboardName;
    private List<E> entries;

    AbstractLogDTO() {}

    public AbstractLogDTO(String leaderboardName, List<E> entries) {
        this.leaderboardName = leaderboardName;
        this.entries = entries;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public List<E> getEntries() {
        return entries;
    }
}
