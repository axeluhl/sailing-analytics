package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.FilterWithUI;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public abstract class AbstractCompetitorsFilterSetDialog extends DataEntryDialog<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>> {
    private final FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> competitorsFilterSet;
    private final StringMessages stringMessages;
    private final List<FilterWithUI<CompetitorDTO>> availableCompetitorFilters;

    private ListBox filterListBox;
    private final Button addFilterButton;

    protected TextBox filterSetNameTextBox;
    private Grid competitorsFiltersGrid;
    private Label competitorsFiltersGridHeadline;
    private Label competitorsFiltersGridFooter;
    private VerticalPanel mainPanel;
    
    private final List<Widget> filterEditWidgets;
    private final List<FilterWithUI<CompetitorDTO>> filters;
    private final List<Label> filterNameLabels;
    private final List<String> filterNames;
    private final List<Button> filterDeleteButtons;
    
    protected static class CompetitorsFilterSetValidator implements Validator<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>> {
        private final StringMessages stringMessages;
        private final List<String> existingFilterSetNames;
        
        public CompetitorsFilterSetValidator(List<String> existingFilterSetNames, StringMessages stringMessages){
            super();
            this.existingFilterSetNames = existingFilterSetNames;
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> competitorsFilterSet) {
            String errorMessage = null;

            int filterCount = competitorsFilterSet.getFilters().size();
            boolean nameNotEmpty = competitorsFilterSet.getName() != null && competitorsFilterSet.getName().length() > 0;
            if (!nameNotEmpty) {
                errorMessage = stringMessages.pleaseEnterAName();
            } else if (existingFilterSetNames.contains(competitorsFilterSet.getName())) {
                errorMessage = stringMessages.filterThisNameAlreadyExists();
            } else if(filterCount < 1) {
                errorMessage = stringMessages.addAtLeastOneFilterCriteria();
            } else {
                for(FilterWithUI<CompetitorDTO> filter: competitorsFilterSet.getFilters()) {
                    errorMessage = filter.validate(stringMessages);
                    if(errorMessage != null) {
                        break;
                    }
                }
            }

            return errorMessage;
        }
    }

    public AbstractCompetitorsFilterSetDialog(FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> competitorsFilterSet, List<FilterWithUI<CompetitorDTO>> availableCompetitorFilters, List<String> existingFilterSetNames, 
            String dialogTitle, StringMessages stringMessages, DialogCallback<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>> callback) {
        super(dialogTitle, null, stringMessages.ok(), stringMessages.cancel(),
                new CompetitorsFilterSetValidator(existingFilterSetNames, stringMessages), callback);
        this.competitorsFilterSet = competitorsFilterSet;
        this.availableCompetitorFilters = availableCompetitorFilters;
        this.stringMessages = stringMessages; 
        
        competitorsFiltersGrid = new Grid(0,0);
        competitorsFiltersGridHeadline = new Label();
        competitorsFiltersGridFooter = new Label();

        filterEditWidgets = new ArrayList<Widget>();
        filterNameLabels = new ArrayList<Label>();
        filterNames = new ArrayList<String>();
        filters = new ArrayList<FilterWithUI<CompetitorDTO>>();
        filterDeleteButtons = new ArrayList<Button>();
        
        addFilterButton = new Button(stringMessages.add());
        filterListBox = createListBox(false);
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        mainPanel = new VerticalPanel();

        HorizontalPanel hPanel = new HorizontalPanel();
        mainPanel.add(hPanel);
        hPanel.add(new Label(stringMessages.filterName() + ":"));
        hPanel.add(filterSetNameTextBox);
        
        filterListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateSelectedFilterInfo();
            }
        });

        filterListBox.addItem(stringMessages.selectAFilterCriteria() + "...");
        for(FilterWithUI<CompetitorDTO> filter: availableCompetitorFilters) {
            filterListBox.addItem(filter.getLocalizedName(stringMessages));
        }
        updateSelectedFilterInfo();

        addFilterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                FilterWithUI<CompetitorDTO> selectedFilter = getSelectedFilter();
                if (selectedFilter != null) {
                    FilterWithUI<CompetitorDTO> newFilter = selectedFilter.copy();
                    filters.add(newFilter);
                    
                    createFilterNameAndLabel(newFilter);
                    createFilterEditWidget(newFilter);
                    createFilterDeleteButton(newFilter);
                }
                updateCompetitorsFiltersGrid(mainPanel);
                validate();
            }
        });       
       
        mainPanel.add(competitorsFiltersGridHeadline);
        mainPanel.add(competitorsFiltersGrid);
        mainPanel.add(competitorsFiltersGridFooter);
        
        for(FilterWithUI<CompetitorDTO> existingFilter: competitorsFilterSet.getFilters()) {
            FilterWithUI<CompetitorDTO> filter = existingFilter.copy();
            filters.add(filter);
            createFilterNameAndLabel(filter);
            createFilterEditWidget(filter);
            createFilterDeleteButton(filter);
        }

        updateCompetitorsFiltersGrid(mainPanel);

        HorizontalPanel addFilterPanel = new HorizontalPanel();
        mainPanel.add(addFilterPanel);
        addFilterPanel.add(new Label(stringMessages.filterCriteria() + ":"));
        addFilterPanel.add(filterListBox);
        addFilterPanel.add(addFilterButton);

        return mainPanel;
    }

    private Label createFilterNameAndLabel(FilterWithUI<CompetitorDTO> filter) {
        Label filterNameLabel = new Label(filter.getLocalizedName(stringMessages) + ":"); 
        filterNameLabels.add(filterNameLabel);
        filterNames.add(filter.getName());
        return filterNameLabel; 
    }

    private Widget createFilterEditWidget(FilterWithUI<CompetitorDTO> filter) {
        Widget filterEditWidget = filter.createFilterUIWidget(this);
        filterEditWidgets.add(filterEditWidget);
        return filterEditWidget;
    }

    private Button createFilterDeleteButton(FilterWithUI<CompetitorDTO> filter) {
        final Button filterDeleteBtn = new Button(stringMessages.delete()); 
        filterDeleteBtn.addStyleName("inlineButton");
        filterDeleteButtons.add(filterDeleteBtn);
        filterDeleteBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int index = 0;
                for(Button btn: filterDeleteButtons) {
                    if(filterDeleteBtn == btn) {
                        break;
                    }
                    index++;
                }
                filterNames.remove(index);
                filterNameLabels.remove(index);
                filterEditWidgets.remove(index);
                filterDeleteButtons.remove(index);
                filters.remove(index);
                updateCompetitorsFiltersGrid(mainPanel);
                validate();
            }
        });
        return filterDeleteBtn; 
    }
    
    private void updateSelectedFilterInfo() {
        Filter<CompetitorDTO> selectedFilter = getSelectedFilter();
        if(selectedFilter != null) {
            addFilterButton.setEnabled(true);
        } else {
            addFilterButton.setEnabled(false);
        }
    }

    @Override
    protected FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> getResult() {
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> result = new FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>(filterSetNameTextBox.getText());
        for (FilterWithUI<CompetitorDTO> filter : filters) {
            result.addFilter(filter.createFilterFromUIWidget());
        }
        return result;
    }
    
    private FilterWithUI<CompetitorDTO> getSelectedFilter() {
        FilterWithUI<CompetitorDTO> result = null;
        int selectedIndex = filterListBox.getSelectedIndex();
        if (selectedIndex > 0) {
            String selectedItemValue = filterListBox.getValue(selectedIndex);
            for (FilterWithUI<CompetitorDTO> filter : availableCompetitorFilters) {
                if (selectedItemValue.equals(filter.getLocalizedName(stringMessages))) {
                    result = filter;
                    break;
                }
            }
        }
        return result;
    }
    
    private void updateCompetitorsFiltersGrid(VerticalPanel parentPanel) {
        int widgetIndex = parentPanel.getWidgetIndex(competitorsFiltersGrid);
        parentPanel.remove(competitorsFiltersGrid);
        
        int filterCount = filterNameLabels.size();
        boolean showGridHeadline = filterCount >= 1 ? true : false;  
        boolean showGridFooter = filterCount >= 1 ? true : false;  
        if(filterCount > 0) {
            competitorsFiltersGrid = new Grid(filterCount, 3);
            competitorsFiltersGrid.setCellSpacing(4);

            competitorsFiltersGridHeadline.setVisible(showGridHeadline);
            competitorsFiltersGridFooter.setVisible(showGridFooter);
            if(showGridHeadline) {
                competitorsFiltersGridHeadline.setText("The filter will show all competitors matching the criterias:");
            }
            if(showGridFooter) {
                competitorsFiltersGridFooter.setText("");
            }
            
            for(int i = 0; i < filterCount; i++) {
                competitorsFiltersGrid.setWidget(i, 0, filterNameLabels.get(i));
                competitorsFiltersGrid.setWidget(i, 1, filterEditWidgets.get(i));
                competitorsFiltersGrid.setWidget(i, 2, filterDeleteButtons.get(i));
                competitorsFiltersGrid.getCellFormatter().setVerticalAlignment(i, 0, HasVerticalAlignment.ALIGN_MIDDLE);
                competitorsFiltersGrid.getCellFormatter().setVerticalAlignment(i, 1, HasVerticalAlignment.ALIGN_MIDDLE);
                competitorsFiltersGrid.getCellFormatter().setVerticalAlignment(i, 2, HasVerticalAlignment.ALIGN_MIDDLE);
           }
        } else {
            competitorsFiltersGrid = new Grid(0, 0);
        }
        parentPanel.insert(competitorsFiltersGrid, widgetIndex);
    }

    @Override
    public void show() {
        super.show();
        filterSetNameTextBox.setFocus(true);
    }
}
