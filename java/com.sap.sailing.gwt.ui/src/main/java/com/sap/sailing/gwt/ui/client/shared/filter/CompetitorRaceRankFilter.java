package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.domain.common.filter.FilterOperators;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;

public class CompetitorRaceRankFilter extends AbstractCompetitorInLeaderboardFilter<Integer> { 
    public static final String FILTER_NAME = "CompetitorRaceRankFilter";

    private IntegerBox valueInputWidget;
    private ListBox operatorSelectionWidget;

    public CompetitorRaceRankFilter() {
        super(FilterOperators.LessThanEquals);
        
        supportedOperators.add(FilterOperators.LessThanEquals);
        supportedOperators.add(FilterOperators.GreaterThanEquals);
        supportedOperators.add(FilterOperators.LessThan);
        supportedOperators.add(FilterOperators.GreaterThan);
        supportedOperators.add(FilterOperators.NotEqualTo);
        supportedOperators.add(FilterOperators.Equals);
    }

    public Class<Integer> getValueType() {
        return Integer.class;
    }

    @Override
    public boolean matches(CompetitorDTO competitorDTO) {
        boolean result = false;
        
        if (filterValue > 0 && filterOperator != null && getLeaderboard() != null && getSelectedRace() != null) {
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
                    switch (filterOperator) {
                        case LessThanEquals:
                            result = raceRank <= filterValue;
                            break;
                        case Equals:
                            result = raceRank == filterValue;
                            break;
                        case GreaterThanEquals:
                            result = raceRank >= filterValue;
                            break;
                        case LessThan:
                            result = raceRank < filterValue;
                            break;
                        case GreaterThan:
                            result = raceRank > filterValue;
                            break;
                        case NotEqualTo:
                            result = raceRank != filterValue;
                            break;
                        case NotContains:
                        case StartsWith:
                        case Contains:
                        case EndsWith:
                        default:
                            throw new RuntimeException("Operator " + filterOperator.name() + " is not supported."); 
                    }
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
    public Widget createValueInputWidget(DataEntryDialog<?> dataEntryDialog) {
        valueInputWidget = dataEntryDialog.createIntegerBox(filterValue, 20);
        valueInputWidget.setFocus(true);
        return valueInputWidget;
    }

    @Override
    public String validate(StringMessages stringMessages) {
        String errorMessage = null;
        if(filterValue != null) {
            Integer intfilterValue = (Integer) filterValue;
            if(intfilterValue <= 0) {
                errorMessage = stringMessages.numberMustBePositive();
            }
        } else {
            errorMessage = stringMessages.pleaseEnterANumber();
        }
        return errorMessage;
    }
    
    @Override
    public Widget createOperatorSelectionWidget(DataEntryDialog<?> dataEntryDialog) {
        operatorSelectionWidget = createOperatorSelectionListBox(dataEntryDialog);
        return operatorSelectionWidget;
    }

    @Override
    public Filter<CompetitorDTO, Integer> createFilterFromWidgets(Widget valueInputWidget, Widget operatorSelectionWidget) {
        Filter<CompetitorDTO, Integer> result = null;
        if(valueInputWidget instanceof IntegerBox && operatorSelectionWidget instanceof ListBox) {
            result = new CompetitorRaceRankFilter();
            IntegerBox valueInputIntegerBox = (IntegerBox) valueInputWidget;
            ListBox operatorSelectionListBox = (ListBox) operatorSelectionWidget;

            FilterOperators op = FilterOperators.valueOf(operatorSelectionListBox.getValue(operatorSelectionListBox.getSelectedIndex()));
            Integer value = valueInputIntegerBox.getValue();
            result.setConfiguration(new Pair<FilterOperators, Integer>(op, value));
        }
        return result;
    }
}
