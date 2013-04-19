package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

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
    
    private final List<RadioButton> activeFilterSetRadioButtons;
    private final List<Button> editFilterSetButtons;
    private final List<Button> deleteFilterSetButtons;
    private final List<FilterSet<CompetitorDTO>> filterSets;
    private final String ACTIVE_FILTERSET_RADIOBUTTON_GROUPNAME = "ActiveFilterSetRB"; 
    private final String FILTER_NOTHING_FILTERSET = "Filter nothing";
    
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
        super("Competitors filters", null, stringMessages.ok(), stringMessages.cancel(), new CompetitorsFilterSetsValidator(), callback);
        this.competitorsFilterSets = competitorsFilterSets;
        this.stringMessages = stringMessages; 
        
        competitorsFilterSetsGrid = new Grid(0,0);

        activeFilterSetRadioButtons = new ArrayList<RadioButton>();
        editFilterSetButtons = new ArrayList<Button>();
        deleteFilterSetButtons = new ArrayList<Button>();
        filterSets = new ArrayList<FilterSet<CompetitorDTO>>();
        
        addFilterSetButton = new Button("Add filter");
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        mainPanel = new VerticalPanel();

        String headLineText;
        if(competitorsFilterSets.getFilterSets().size() < 1) {
            headLineText = "Please create a filter set to filter the list of competitors.";
        } else {
            headLineText = "Available filters";
        }
        
        mainPanel.add(new Label(headLineText));
        mainPanel.add(competitorsFilterSetsGrid);
        
        // create a dummy filter for the "filter nothing" option
        FilterSet<CompetitorDTO> noFilterSet = new FilterSet<CompetitorDTO>(FILTER_NOTHING_FILTERSET);
        createActiveFilterSetRadioButton(noFilterSet, competitorsFilterSets.getActiveFilterSet() == null);
        Button noFilterSetEditBtn = createEditFilterSetButton(noFilterSet);
        Button noFilterSetDeleteBtn = createDeleteFilterSetButton(noFilterSet);
        filterSets.add(noFilterSet);
        noFilterSetEditBtn.setVisible(false);
        noFilterSetDeleteBtn.setVisible(false);
        
        for(FilterSet<CompetitorDTO> filterSet: competitorsFilterSets.getFilterSets()) {
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
                
                CreateCompetitorsFilterSetDialog dialog = new CreateCompetitorsFilterSetDialog(existingFilterSetNames, stringMessages,
                        new DialogCallback<FilterSet<CompetitorDTO>>() {
                    @Override
                    public void ok(final FilterSet<CompetitorDTO> filterSet) {
                        createActiveFilterSetRadioButton(filterSet, false);
                        createEditFilterSetButton(filterSet);
                        createDeleteFilterSetButton(filterSet);
                        filterSets.add(filterSet);
                        
                        updateCompetitorsFilterSetsGrid(mainPanel);
                        validate();
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

    private RadioButton createActiveFilterSetRadioButton(FilterSet<CompetitorDTO> filterSet, boolean isActiveFilterSet) {
        RadioButton activeFilterSetRadioButton = createRadioButton(ACTIVE_FILTERSET_RADIOBUTTON_GROUPNAME, filterSet.getName());
        activeFilterSetRadioButton.setValue(isActiveFilterSet);
        activeFilterSetRadioButtons.add(activeFilterSetRadioButton);
        return activeFilterSetRadioButton; 
    }

    private Button createEditFilterSetButton(final FilterSet<CompetitorDTO> filterSet) {
        final Button editFilterSetBtn = new Button(stringMessages.edit()); 
        final String filterSetName = filterSet.getName();
        editFilterSetBtn.addStyleName("inlineButton");
        editFilterSetButtons.add(editFilterSetBtn);
        editFilterSetBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                List<String> existingFilterSetNames = new ArrayList<String>();
                
                EditCompetitorsFilterSetDialog dialog = new EditCompetitorsFilterSetDialog(filterSet, existingFilterSetNames, 
                        stringMessages, new DialogCallback<FilterSet<CompetitorDTO>>() {
                    @Override
                    public void ok(final FilterSet<CompetitorDTO> changedFilterSet) {
                        // update the changed filter set
                        int index = -1;
                        for (int i = 0; i < filterSets.size(); i++) {
                            if(filterSetName.equals(filterSets.get(i).getName())) {
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
                        validate();
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

    private Button createDeleteFilterSetButton(FilterSet<CompetitorDTO> filterSet) {
        final Button deleteFilterSetBtn = new Button(stringMessages.delete()); 
        deleteFilterSetBtn.addStyleName("inlineButton");
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
                if(activeFilterSetRadioButtons.get(index).getValue()) {
                    activeFilterSetRadioButtons.get(0).setValue(true);
                }
                
                activeFilterSetRadioButtons.remove(index);
                editFilterSetButtons.remove(index);
                deleteFilterSetButtons.remove(index);
                filterSets.remove(index);
                updateCompetitorsFilterSetsGrid(mainPanel);
                validate();
            }
        });
        return deleteFilterSetBtn; 
    }

    @Override
    protected CompetitorsFilterSets getResult() {
        CompetitorsFilterSets result = new CompetitorsFilterSets();

        int filterSetCount = activeFilterSetRadioButtons.size();        
        for (int i = 0; i < filterSetCount; i++) {
            FilterSet<CompetitorDTO> filterSet = filterSets.get(i);
            boolean isActiveFilterSet = activeFilterSetRadioButtons.get(i).getValue(); 
            
            if(!filterSet.getName().equals(FILTER_NOTHING_FILTERSET)) {
                result.addFilterSet(filterSet);
                if(isActiveFilterSet) {
                    result.setActiveFilterSet(filterSet);
                }
            } else {
                if(isActiveFilterSet) {
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
            }
        } else {
            competitorsFilterSetsGrid = new Grid(0, 0);
        }
        parentPanel.insert(competitorsFilterSetsGrid, widgetIndex);
    }

    @Override
    public void show() {
        super.show();
        getOkButton().setFocus(true);
    }
}
