package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MarkPassingTimesDTO extends NamedDTO implements IsSerializable {
    /**
     * The time point when the leg was entered first by a competitor; for the first leg the semantics
     * are slightly different: if the official race start time is known it is used instead of the first
     * passing event.
     */
    public Date firstPassingDate;

    public Date lastPassingDate;

    public MarkPassingTimesDTO() {}

    public MarkPassingTimesDTO(String name) {
        this.name = name;
    }

	@Override
	public String toString() {
		return "MarkPassingTimesDTO [firstPassingDate=" + firstPassingDate
				+ ", lastPassingDate=" + lastPassingDate + ", name=" + name
				+ "]";
	}
}
