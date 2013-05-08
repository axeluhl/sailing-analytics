package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.filter.BinaryOperator;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.FilterWithUI;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardFetcher;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;

public class CompetitorTotalRankFilter extends AbstractCompetitorNumberFilterWithUI<Integer> implements LeaderboardFilterContext { 
    public static final String FILTER_NAME = "CompetitorTotalRankFilter";

    private IntegerBox valueIntegerBox;
    private ListBox operatorSelectionListBox;

    private LeaderboardFetcher leaderboardFetcher;

    public CompetitorTotalRankFilter() {
        super(BinaryOperator.Operators.LessThanEquals);
        
        supportedOperators.add(BinaryOperator.Operators.LessThanEquals);
        supportedOperators.add(BinaryOperator.Operators.GreaterThanEquals);
        supportedOperators.add(BinaryOperator.Operators.LessThan);
        supportedOperators.add(BinaryOperator.Operators.GreaterThan);
        supportedOperators.add(BinaryOperator.Operators.NotEqualTo);
        supportedOperators.add(BinaryOperator.Operators.Equals);
        
        valueIntegerBox = null;
        operatorSelectionListBox = null;
    }

    public Class<Integer> getValueType() {
        return Integer.class;
    }

    private LeaderboardDTO getLeaderboard() {
        return leaderboardFetcher != null ? leaderboardFetcher.getLeaderboard() : null;
    }

    @Override
    public boolean matches(CompetitorDTO competitorDTO) {
        boolean result = false;
        
        if (value > 0 && operator != null && getLeaderboard() != null) {
            int totalRank = getLeaderboard().getRank(competitorDTO);
            result = operator.matchValues(value, totalRank);
        }
        
        return result;
    }
        
    @Override
    public String getName() {
        return FILTER_NAME;
    }

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        return stringMessages.totalRank();
    }

    @Override 
    public Widget createFilterUIWidget(DataEntryDialog<?> dataEntryDialog) {
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(createOperatorSelectionWidget(dataEntryDialog));
        hp.add(createValueInputWidget(dataEntryDialog));
        hp.setSpacing(5);
        return hp;
    }

    private Widget createValueInputWidget(DataEntryDialog<?> dataEntryDialog) {
        if(valueIntegerBox == null) {
            valueIntegerBox = dataEntryDialog.createIntegerBox(value, 20);
            valueIntegerBox.setFocus(true);
        }
        return valueIntegerBox;
    }

    private Widget createOperatorSelectionWidget(DataEntryDialog<?> dataEntryDialog) {
        if(operatorSelectionListBox == null) {
            operatorSelectionListBox = createOperatorSelectionListBox(dataEntryDialog);
        }
        return operatorSelectionListBox;
    }

    @Override
    public String validate(StringMessages stringMessages) {
        String errorMessage = null;
        if(value != null) {
            Integer intfilterValue = (Integer) value;
            if(intfilterValue <= 0) {
                errorMessage = stringMessages.numberMustBePositive();
            }
        } else {
            errorMessage = stringMessages.pleaseEnterANumber();
        }
        return errorMessage;
    }

    @Override
    public FilterWithUI<CompetitorDTO> copy() {
        CompetitorTotalRankFilter result = new CompetitorTotalRankFilter();
        result.setValue(getValue());
        result.setOperator(getOperator());
        return result;
    }

    @Override
    public FilterWithUI<CompetitorDTO> createFilterFromUIWidget() {
        CompetitorTotalRankFilter result = null;
        if(valueIntegerBox != null && operatorSelectionListBox != null) {
            result = new CompetitorTotalRankFilter();

            BinaryOperator.Operators op = BinaryOperator.Operators.valueOf(operatorSelectionListBox.getValue(operatorSelectionListBox.getSelectedIndex()));
            BinaryOperator<Integer> binaryOperator = new BinaryOperator<Integer>(op);
            
            Integer value = valueIntegerBox.getValue();
            result.setOperator(binaryOperator);
            result.setValue(value);
        }
        return result;
    }

    @Override
    public LeaderboardFetcher getLeaderboardFetcher() {
        return leaderboardFetcher;
    }

    @Override
    public void setLeaderboardFetcher(LeaderboardFetcher leaderboardFetcher) {
        this.leaderboardFetcher = leaderboardFetcher;
    }
}
