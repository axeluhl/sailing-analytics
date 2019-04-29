package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * A dialog to create, edit and delete filter sets for competitors
 * @author Frank
 *
 */
public class CompetitorsFilterSetsDialog extends DataEntryDialog<CompetitorsFilterSets> {
    private final CompetitorsFilterSets competitorsFilterSets;
    private final StringMessages stringMessages;

    private final Button addFilterSetButton;
    private Grid competitorsFilterSetsGrid;
    private VerticalPanel mainPanel;
    
    private final List<String> availableCompetitorFilterNames;
    
    private final List<RadioButton> activeFilterSetRadioButtons;
    private final List<Button> editFilterSetButtons;
    private final List<Button> deleteFilterSetButtons;
    private final List<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>> filterSets;
    private final String ACTIVE_FILTERSET_RADIOBUTTON_GROUPNAME = "ActiveFilterSetRB"; 
    private final String filterNothingFiltersetName;
    
    protected static class CompetitorsFilterSetsValidator implements Validator<CompetitorsFilterSets> {
        public CompetitorsFilterSetsValidator() {
            super();
        }

        @Override
        public String getErrorMessage(CompetitorsFilterSets competitorsFilterSets) {
            return null;
        }
    }

    public CompetitorsFilterSetsDialog(CompetitorsFilterSets competitorsFilterSets, StringMessages stringMessages, DialogCallback<CompetitorsFilterSets> callback) {
        super(stringMessages.competitorsFilter(), null, stringMessages.ok(), stringMessages.cancel(), new CompetitorsFilterSetsValidator(), callback);
        this.competitorsFilterSets = competitorsFilterSets;
        this.stringMessages = stringMessages; 
        filterNothingFiltersetName = stringMessages.filterNothing();
        competitorsFilterSetsGrid = new Grid(0,0);
        activeFilterSetRadioButtons = new ArrayList<RadioButton>();
        editFilterSetButtons = new ArrayList<Button>();
        deleteFilterSetButtons = new ArrayList<Button>();
        filterSets = new ArrayList<>();
        addFilterSetButton = new Button(stringMessages.actionAddFilter());
        availableCompetitorFilterNames = new ArrayList<String>();
        availableCompetitorFilterNames.add(CompetitorTotalRankFilter.FILTER_NAME);
        availableCompetitorFilterNames.add(CompetitorRaceRankFilter.FILTER_NAME);
        availableCompetitorFilterNames.add(CompetitorNationalityFilter.FILTER_NAME);
        availableCompetitorFilterNames.add(CompetitorSailNumbersFilter.FILTER_NAME);
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        mainPanel = new VerticalPanel();
        String headLineText;
        if (competitorsFilterSets.getFilterSets().size() < 1) {
            headLineText = stringMessages.createFilterHint();
        } else {
            headLineText = stringMessages.availableFilters();
        }
        mainPanel.add(new Label(headLineText));
        mainPanel.add(competitorsFilterSetsGrid);
        // create a dummy filter for the "filter nothing" option
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> noFilterSet = new FilterSet<>(filterNothingFiltersetName);
        createActiveFilterSetRadioButton(noFilterSet, competitorsFilterSets.getActiveFilterSet() == null);
        Button noFilterSetEditBtn = createEditFilterSetButton(noFilterSet);
        Button noFilterSetDeleteBtn = createDeleteFilterSetButton(noFilterSet);
        filterSets.add(noFilterSet);
        noFilterSetEditBtn.setVisible(false);
        noFilterSetDeleteBtn.setVisible(false);
        for (FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet : competitorsFilterSets.getFilterSets()) {
            createActiveFilterSetRadioButton(filterSet, competitorsFilterSets.getActiveFilterSet() == filterSet);
            createEditFilterSetButton(filterSet);
            createDeleteFilterSetButton(filterSet);
            filterSets.add(filterSet);
        }
        updateCompetitorsFilterSetsGrid(mainPanel);
        mainPanel.add(addFilterSetButton);
        addFilterSetButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                List<String> existingFilterSetNames = new ArrayList<String>();
                for (FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet : getResult().getFilterSets()) {
                    existingFilterSetNames.add(filterSet.getName());
                }
                CreateCompetitorsFilterSetDialog dialog = new CreateCompetitorsFilterSetDialog(existingFilterSetNames,
                        availableCompetitorFilterNames, stringMessages, new DialogCallback<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>>() {
                    @Override
                    public void ok(final FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet) {
                        createActiveFilterSetRadioButton(filterSet, false);
                        createEditFilterSetButton(filterSet);
                        createDeleteFilterSetButton(filterSet);
                        filterSets.add(filterSet);
                        updateCompetitorsFilterSetsGrid(mainPanel);
                        validateAndUpdate();
                    }

                    @Override
                    public void cancel() { 
                    }
                });
                dialog.show();
            }
        });       
   
        return mainPanel;
    }

    private RadioButton createActiveFilterSetRadioButton(FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet, boolean isActiveFilterSet) {
        RadioButton activeFilterSetRadioButton = createRadioButton(ACTIVE_FILTERSET_RADIOBUTTON_GROUPNAME, filterSet.getName());
        activeFilterSetRadioButton.setValue(isActiveFilterSet);
        activeFilterSetRadioButtons.add(activeFilterSetRadioButton);
        return activeFilterSetRadioButton; 
    }

    private Button createEditFilterSetButton(final FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSetToEdit) {
        final Button editFilterSetBtn = new Button(stringMessages.edit());
        final String filterSetToEditName = filterSetToEdit.getName();
        editFilterSetBtn.addStyleName("inlineButton");
        editFilterSetBtn.setVisible(filterSetToEdit.isEditable());
        editFilterSetButtons.add(editFilterSetBtn);
        editFilterSetBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                List<String> existingFilterSetNames = new ArrayList<String>();
                for (FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet : getResult().getFilterSets()) {
                    if (!filterSet.getName().equals(filterSetToEditName)) {
                        existingFilterSetNames.add(filterSet.getName());
                    }
                }
                EditCompetitorsFilterSetDialog dialog = new EditCompetitorsFilterSetDialog(filterSetToEdit, availableCompetitorFilterNames, 
                        existingFilterSetNames, stringMessages, new DialogCallback<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>>() {
                    @Override
                    public void ok(final FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> changedFilterSet) {
                        // update the changed filter set
                        int index = -1;
                        for (int i = 0; i < filterSets.size(); i++) {
                            if(filterSetToEditName.equals(filterSets.get(i).getName())) {
                                index = i;
                                break;
                            }
                        }
                        boolean isActiveFilterSet = activeFilterSetRadioButtons.get(index).getValue();
                        activeFilterSetRadioButtons.remove(index);
                        editFilterSetButtons.remove(index);
                        deleteFilterSetButtons.remove(index);
                        filterSets.remove(index);
                        
                        createActiveFilterSetRadioButton(changedFilterSet, isActiveFilterSet);
                        createEditFilterSetButton(changedFilterSet);
                        createDeleteFilterSetButton(changedFilterSet);
                        filterSets.add(changedFilterSet);                        

                        updateCompetitorsFilterSetsGrid(mainPanel);
                        validateAndUpdate();
                    }

                    @Override
                    public void cancel() { 
                    }
                });
                dialog.show();
            }
        });
        return editFilterSetBtn;
    }

    private Button createDeleteFilterSetButton(FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet) {
        final Button deleteFilterSetBtn = new Button(stringMessages.delete()); 
        deleteFilterSetBtn.addStyleName("inlineButton");
        deleteFilterSetBtn.setVisible(filterSet.isEditable());
        deleteFilterSetButtons.add(deleteFilterSetBtn);
        deleteFilterSetBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int index = 0;
                for(Button btn: deleteFilterSetButtons) {
                    if(deleteFilterSetBtn == btn) {
                        break;
                    }
                    index++;
                }
                // in case the filter set to delete is the 'active' one, we set the "Filter nothing" filter set 'active'
                if (activeFilterSetRadioButtons.get(index).getValue()) {
                    activeFilterSetRadioButtons.get(0).setValue(true);
                }
                activeFilterSetRadioButtons.remove(index);
                editFilterSetButtons.remove(index);
                deleteFilterSetButtons.remove(index);
                filterSets.remove(index);
                updateCompetitorsFilterSetsGrid(mainPanel);
                validateAndUpdate();
            }
        });
        return deleteFilterSetBtn; 
    }

    @Override
    protected CompetitorsFilterSets getResult() {
        CompetitorsFilterSets result = new CompetitorsFilterSets();
        int filterSetCount = activeFilterSetRadioButtons.size();        
        for (int i = 0; i < filterSetCount; i++) {
            FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet = filterSets.get(i);
            boolean isActiveFilterSet = activeFilterSetRadioButtons.get(i).getValue(); 
            if (!filterSet.getName().equals(filterNothingFiltersetName)) {
                result.addFilterSet(filterSet);
                if (isActiveFilterSet) {
                    result.setActiveFilterSet(filterSet);
                }
            } else {
                if (isActiveFilterSet) {
                    result.setActiveFilterSet(null);
                }
            }
        }

        return result;
    }
    
    private void updateCompetitorsFilterSetsGrid(VerticalPanel parentPanel) {
        int widgetIndex = parentPanel.getWidgetIndex(competitorsFilterSetsGrid);
        parentPanel.remove(competitorsFilterSetsGrid);
        
        int filterCount = activeFilterSetRadioButtons.size();
        if(filterCount > 0) {
            competitorsFilterSetsGrid = new Grid(filterCount, 3);
            competitorsFilterSetsGrid.setCellSpacing(3);
            for(int i = 0; i < filterCount; i++) {
                competitorsFilterSetsGrid.setWidget(i, 0, activeFilterSetRadioButtons.get(i));
                competitorsFilterSetsGrid.setWidget(i, 1, editFilterSetButtons.get(i));
                competitorsFilterSetsGrid.setWidget(i, 2, deleteFilterSetButtons.get(i));
                competitorsFilterSetsGrid.getCellFormatter().setVerticalAlignment(i , 0 , HasVerticalAlignment.ALIGN_MIDDLE);
                competitorsFilterSetsGrid.getCellFormatter().setVerticalAlignment(i , 1 , HasVerticalAlignment.ALIGN_MIDDLE);
                competitorsFilterSetsGrid.getCellFormatter().setVerticalAlignment(i , 2 , HasVerticalAlignment.ALIGN_MIDDLE);
            }
        } else {
            competitorsFilterSetsGrid = new Grid(0, 0);
        }
        parentPanel.insert(competitorsFilterSetsGrid, widgetIndex);
    }
}
