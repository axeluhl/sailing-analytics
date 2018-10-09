package com.sap.sailing.gwt.home.communication.user.profile.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.Util;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

/**
 * This domain object contains a list of {@link SailorProfileDTO} objects which is loaded to display the sailor profile
 * overview.
 */
public class SailorProfilesDTO implements Result, Serializable {
    private static final long serialVersionUID = -7062326258445057995L;
    private ArrayList<SailorProfileDTO> entries = new ArrayList<>();

    protected SailorProfilesDTO() {

    }

    public SailorProfilesDTO(Iterable<SailorProfileDTO> entries) {
        super();
        Util.addAll(entries, this.entries);
    }

    public List<SailorProfileDTO> getEntries() {
        return entries;
    }

}
