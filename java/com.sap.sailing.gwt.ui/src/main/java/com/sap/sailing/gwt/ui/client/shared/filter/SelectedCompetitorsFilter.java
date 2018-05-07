package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;

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

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        return stringMessages.selectedCompetitors();
    }

    @Override
    public String getLocalizedDescription(StringMessages stringMessages) {
        return stringMessages.selectedCompetitors();
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
    public FilterUIFactory<CompetitorDTO> createUIFactory() {
        return null;
    }

    @Override
    public FilterWithUI<CompetitorDTO> copy() {
        return new SelectedCompetitorsFilter();
    }
}
