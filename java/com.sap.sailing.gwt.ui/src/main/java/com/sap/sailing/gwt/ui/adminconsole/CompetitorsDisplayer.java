package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.CompetitorDAO;

public interface CompetitorsDisplayer {
    void fillCompetitors(List<CompetitorDAO> competitors);
}
