package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO for a race log entry.
 */
public class RaceLogEventDTO extends AbstractLogEventDTO implements IsSerializable {
    private Integer passId;
    
    RaceLogEventDTO() {}

    public RaceLogEventDTO(int passId, String authorName, Integer authorPriority, Date createdAt, Date logicalTimePoint,
            String type, String info) {
        super(authorName, authorPriority, createdAt, logicalTimePoint, type, info);
        this.passId = passId;
    }

    public int getPassId() {
        return passId;
    }
}
