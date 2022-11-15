package com.sap.sailing.sailti.resultimport;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;

public class EventResultDescriptor extends NamedImpl {
    private static final long serialVersionUID = 2371582096105999349L;
    private final List<RegattaResultDescriptor> regattaResults;
    private final String id;

    public EventResultDescriptor(String id, String name, Iterable<RegattaResultDescriptor> regattaResults) {
        super(name);
        this.id = id;
        this.regattaResults = new ArrayList<RegattaResultDescriptor>();
        Util.addAll(regattaResults, this.regattaResults);
    }

    public List<RegattaResultDescriptor> getRegattaResults() {
        return regattaResults;
    }

    public String getId() {
        return id;
    }
}
