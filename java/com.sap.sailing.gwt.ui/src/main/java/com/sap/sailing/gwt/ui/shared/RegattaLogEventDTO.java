package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO for a race log entry.
 */
public class RegattaLogEventDTO extends AbstractLogEventDTO implements IsSerializable {
    RegattaLogEventDTO() {}

    public RegattaLogEventDTO(String authorName, Integer authorPriority, Date createdAt, Date logicalTimePoint,
            String type, String info) {
        super(authorName, authorPriority, createdAt, logicalTimePoint, type, info);
    }
}
