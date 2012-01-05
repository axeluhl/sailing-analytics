package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.EventDAO;

public interface EventDisplayer {
    void fillEvents(List<EventDAO> result);
}
