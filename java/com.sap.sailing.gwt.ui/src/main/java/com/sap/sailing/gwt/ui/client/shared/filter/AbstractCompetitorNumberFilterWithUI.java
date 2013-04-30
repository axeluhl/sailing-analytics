package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.filter.AbstractNumberFilter;
import com.sap.sailing.domain.common.filter.BinaryOperator;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardFetcher;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;

public abstract class AbstractCompetitorNumberFilterWithUI<T extends Number> extends AbstractNumberFilter<CompetitorDTO, T> 
    implements CompetitorInLeaderboardFilter<T> {
    
    protected List<BinaryOperator.Operators> supportedOperators;
    protected BinaryOperator.Operators defaultOperator;
    
    private LeaderboardFetcher contextProvider;
    private RaceIdentifier selectedRace;

    public AbstractCompetitorNumberFilterWithUI(BinaryOperator.Operators defaultOperator) {
        this.defaultOperator = defaultOperator;
        supportedOperators = new ArrayList<BinaryOperator.Operators>();
    }

    public void setContextProvider(LeaderboardFetcher contextProvider) {
        this.contextProvider = contextProvider;
    }

    public LeaderboardDTO getLeaderboard() {
        return contextProvider.getLeaderboard();
    }

    public LeaderboardFetcher getContextProvider() {
        return contextProvider;
    }

    public RaceIdentifier getSelectedRace() {
        return selectedRace;
    }

    public void setSelectedRace(RaceIdentifier selectedRace) {
        this.selectedRace = selectedRace;
    }

    protected ListBox createOperatorSelectionListBox(DataEntryDialog<?> dataEntryDialog) {
        ListBox operatorsListBox = dataEntryDialog.createListBox(false);
        int i = 0;
        for(BinaryOperator.Operators op: supportedOperators) {
            operatorsListBox.addItem(FilterOperatorsFormatter.format(op), op.name());
            if(operator != null && operator.equals(op)) {
                operatorsListBox.setSelectedIndex(i);
            } else if (defaultOperator != null && defaultOperator.equals(op)) {
                operatorsListBox.setSelectedIndex(i);
            }
            i++;
        }
        return operatorsListBox;
    }
}
