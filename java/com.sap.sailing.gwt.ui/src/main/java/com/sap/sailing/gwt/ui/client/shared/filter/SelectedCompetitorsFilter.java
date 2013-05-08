package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.FilterWithUI;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class SelectedCompetitorsFilter implements FilterWithUI<CompetitorDTO>, CompetitorSelectionProviderFilterContext {
    public static final String FILTER_NAME = "SelectedCompetitorsFilter";

    private CompetitorSelectionProvider competitorSelectionProvider;
    
    public SelectedCompetitorsFilter() {
        competitorSelectionProvider = null;
    }

    @Override
    public boolean matches(CompetitorDTO competitor) {
        boolean result = false;
        if(competitorSelectionProvider != null && competitorSelectionProvider.isSelected(competitor)) {
            result = true;
        }
        return result;
    }
    
    @Override
    public String getName() {
        return FILTER_NAME;
    }

    public String getLocalizedName(StringMessages stringMessages) {
        return "Selected competitors";
    }

    @Override
    public CompetitorSelectionProvider getCompetitorSelectionProvider() {
        return competitorSelectionProvider;
    }

    @Override
    public void setCompetitorSelectionProvider(CompetitorSelectionProvider competitorSelectionProvider) {
        this.competitorSelectionProvider = competitorSelectionProvider;
    }

    @Override
    public String validate(StringMessages stringMessages) {
        return null;
    }

    @Override
    public Widget createFilterUIWidget(DataEntryDialog<?> dataEntryDialog) {
        return null;
    }

    @Override
    public FilterWithUI<CompetitorDTO> createFilterFromUIWidget() {
        return null;
    }

    @Override
    public FilterWithUI<CompetitorDTO> copy() {
        return new SelectedCompetitorsFilter();
    }
}
