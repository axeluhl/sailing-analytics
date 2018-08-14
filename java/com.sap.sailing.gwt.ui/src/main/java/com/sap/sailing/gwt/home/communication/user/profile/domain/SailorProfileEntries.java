package com.sap.sailing.gwt.home.communication.user.profile.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.Util;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

public class SailorProfileEntries implements Result, Serializable {
    private static final long serialVersionUID = -7062326258445057995L;
    private ArrayList<SailorProfileEntry> entries = new ArrayList<>();

    protected SailorProfileEntries() {

    }

    public SailorProfileEntries(Iterable<SailorProfileEntry> entries) {
        super();
        Util.addAll(entries, this.entries);
    }

    public List<SailorProfileEntry> getEntries() {
        return entries;
    }

}
