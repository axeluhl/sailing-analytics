package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.filter.BinaryOperator;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.FilterWithUI;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardFetcher;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;

public class CompetitorRaceRankFilter extends AbstractCompetitorNumberFilterWithUI<Integer>
    implements LeaderboardFilterContext, SelectedRaceFilterContext { 
    public static final String FILTER_NAME = "CompetitorRaceRankFilter";

    private IntegerBox valueIntegerBox;
    private ListBox operatorSelectionListBox;
    
    private LeaderboardFetcher leaderboardFetcher;
    private RaceIdentifier selectedRace;

    public CompetitorRaceRankFilter() {
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
        
        if (value != null && operator != null && getLeaderboard() != null && getSelectedRace() != null) {
            String raceColumnName = null;
            for(RaceColumnDTO raceColumnDTO: getLeaderboard().getRaceList()) {
                if(raceColumnDTO.containsRace(getSelectedRace())) {
                    raceColumnName = raceColumnDTO.name;
                    break;
                }
            }
            if(raceColumnName != null) {
                LeaderboardRowDTO leaderboardRowDTO = getLeaderboard().rows.get(competitorDTO);
                LeaderboardEntryDTO leaderboardEntryDTO = leaderboardRowDTO.fieldsByRaceColumnName.get(raceColumnName);
                if(leaderboardEntryDTO.rank != null) {
                    int raceRank = leaderboardEntryDTO.rank;
                    result = operator.matchValues(value, raceRank);
                }
            }
        }
        
        return result;
    }
        
    @Override
    public String getName() {
        return FILTER_NAME;
    }

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        return stringMessages.raceRank();
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
        CompetitorRaceRankFilter result = new CompetitorRaceRankFilter();
        result.setValue(getValue());
        result.setOperator(getOperator());
        return result;
    }

    @Override
    public FilterWithUI<CompetitorDTO> createFilterFromUIWidget() {
        CompetitorRaceRankFilter result = null;
        if(valueIntegerBox != null && operatorSelectionListBox != null) {
            result = new CompetitorRaceRankFilter();

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

    @Override
    public RaceIdentifier getSelectedRace() {
        return selectedRace;
    }

    @Override
    public void setSelectedRace(RaceIdentifier selectedRace) {
        this.selectedRace = selectedRace;
    }
}
