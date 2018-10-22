package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.sap.sse.security.shared.NamedDTO;

public class MarkPassingTimesDTO extends NamedDTO {
    private static final long serialVersionUID = 6582422144259670004L;

    /**
     * The time point when the leg was entered first by a competitor; for the first leg the semantics are slightly
     * different: if the official race start time is known it is used instead of the first passing event.
     */
    public Date firstPassingDate;

    public Date lastPassingDate;

    public MarkPassingTimesDTO() {
    }

    public MarkPassingTimesDTO(String name) {
        this.setName(name);
    }

    @Override
    public String toString() {
        return "MarkPassingTimesDTO [firstPassingDate=" + firstPassingDate + ", lastPassingDate=" + lastPassingDate
                + ", name=" + getName() + "]";
    }
}
