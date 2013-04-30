package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.ValueFilterWithUI;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public abstract class AbstractCompetitorsFilterSetDialog extends DataEntryDialog<FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>>> {
    private final FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> competitorsFilterSet;
    private final StringMessages stringMessages;
    private final List<ValueFilterWithUI<CompetitorDTO, ?>> availableCompetitorsFilter;

    private ListBox filterListBox;
    private final Button addFilterButton;

    protected TextBox filterSetNameTextBox;
    private Grid competitorsFiltersGrid;
    private VerticalPanel mainPanel;
    
    private final List<Widget> filterOperatorSelectionWidgets;
    private final List<Widget> filterValueInputWidgets;
    private final List<Label> filterNameLabels;
    private final List<String> filterNames;
    private final List<Button> filterDeleteButtons;
    
    protected static class CompetitorsFilterSetValidator implements Validator<FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>>> {
        private final StringMessages stringMessages;
        private final List<String> existingFilterSetNames;
        
        public CompetitorsFilterSetValidator(List<String> existingFilterSetNames, StringMessages stringMessages){
            super();
            this.existingFilterSetNames = existingFilterSetNames;
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> competitorsFilterSet) {
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
                for(ValueFilterWithUI<CompetitorDTO, ?> filter: competitorsFilterSet.getFilters()) {
                    errorMessage = filter.validate(stringMessages);
                    if(errorMessage != null) {
                        break;
                    }
                }
            }

            return errorMessage;
        }
    }

    public AbstractCompetitorsFilterSetDialog(FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> competitorsFilterSet, List<ValueFilterWithUI<CompetitorDTO, ?>> availableCompetitorsFilter, List<String> existingFilterSetNames, 
            String dialogTitle, StringMessages stringMessages, DialogCallback<FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>>> callback) {
        super(dialogTitle, null, stringMessages.ok(), stringMessages.cancel(),
                new CompetitorsFilterSetValidator(existingFilterSetNames, stringMessages), callback);
        this.competitorsFilterSet = competitorsFilterSet;
        this.availableCompetitorsFilter = availableCompetitorsFilter;
        this.stringMessages = stringMessages; 
        
        competitorsFiltersGrid = new Grid(0,0);

        filterOperatorSelectionWidgets = new ArrayList<Widget>();
        filterValueInputWidgets = new ArrayList<Widget>();
        filterNameLabels = new ArrayList<Label>();
        filterNames = new ArrayList<String>();
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
        for(ValueFilterWithUI<CompetitorDTO,?> filter: availableCompetitorsFilter) {
            filterListBox.addItem(filter.getLocalizedName(stringMessages));
        }
        updateSelectedFilterInfo();

        addFilterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ValueFilterWithUI<CompetitorDTO,?> selectedFilter = getSelectedFilter();
                if(selectedFilter != null) {
                    createFilterNameAndLabel(selectedFilter);
                    createFilterOperatorSelectionWidget(selectedFilter);
                    createFilterValueInputWidget(selectedFilter);
                    createFilterDeleteButton(selectedFilter);
                }
                updateCompetitorsFiltersGrid(mainPanel);
                validate();
            }
        });       
       
        mainPanel.add(competitorsFiltersGrid);
        
        for(ValueFilterWithUI<CompetitorDTO, ?> filter: competitorsFilterSet.getFilters()) {
            createFilterNameAndLabel(filter);
            createFilterOperatorSelectionWidget(filter);
            createFilterValueInputWidget(filter);
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

    private Label createFilterNameAndLabel(ValueFilterWithUI<CompetitorDTO,?> filter) {
        Label filterNameLabel = new Label(filter.getLocalizedName(stringMessages) + ":"); 
        filterNameLabels.add(filterNameLabel);
        filterNames.add(filter.getName());
        return filterNameLabel; 
    }

    private Widget createFilterOperatorSelectionWidget(ValueFilterWithUI<CompetitorDTO,?> filter) {
        Widget filterOperatorSelectionWidget = filter.createOperatorSelectionWidget(this);
        filterOperatorSelectionWidgets.add(filterOperatorSelectionWidget);
        return filterOperatorSelectionWidget;
    }

    private Widget createFilterValueInputWidget(ValueFilterWithUI<CompetitorDTO,?> filter) {
        Widget filterValueWidget = filter.createValueInputWidget(this);
        filterValueInputWidgets.add(filterValueWidget);
        return filterValueWidget; 
    }

    private Button createFilterDeleteButton(ValueFilterWithUI<CompetitorDTO,?> filter) {
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
                filterOperatorSelectionWidgets.remove(index);
                filterValueInputWidgets.remove(index);
                filterDeleteButtons.remove(index);
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
    protected FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> getResult() {
        FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> result = new FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>>(filterSetNameTextBox.getText());
        int filterCount = filterNameLabels.size();
        for (int i = 0; i < filterCount; i++) {
            String filterName = filterNames.get(i);
            ValueFilterWithUI<CompetitorDTO, ?> filter = CompetitorsFilterFactory.getFilter(filterName);
            result.addFilter((ValueFilterWithUI<CompetitorDTO, ?>) filter.createFilterFromWidgets(filterValueInputWidgets.get(i), filterOperatorSelectionWidgets.get(i)));
        }
        return result;
    }
    
    private ValueFilterWithUI<CompetitorDTO,?> getSelectedFilter() {
        ValueFilterWithUI<CompetitorDTO,?> result = null;
        int selectedIndex = filterListBox.getSelectedIndex();
        if(selectedIndex > 0) {
            String selectedItemValue = filterListBox.getValue(selectedIndex);
            for(ValueFilterWithUI<CompetitorDTO,?> filter: availableCompetitorsFilter) {
                if(selectedItemValue.equals(filter.getLocalizedName(stringMessages))) {
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
        int headlineRow = filterCount >= 1 ? 1 : 0;  
        int footerRow = filterCount >= 1 ? 1 : 0;  
        if(filterCount > 0) {
            competitorsFiltersGrid = new Grid(filterCount + headlineRow + footerRow, 4);
            competitorsFiltersGrid.setCellSpacing(4);
            if(headlineRow == 1) {
                competitorsFiltersGrid.setWidget(0 ,0, new Label(stringMessages.filterCriteria()));
                competitorsFiltersGrid.setWidget(0 ,1, new Label(stringMessages.operator()));
                competitorsFiltersGrid.setWidget(0 ,2, new Label(stringMessages.value()));
            }
            
            for(int i = 0; i < filterCount; i++) {
                competitorsFiltersGrid.setWidget(i + headlineRow, 0, filterNameLabels.get(i));
                competitorsFiltersGrid.setWidget(i + headlineRow, 1, filterOperatorSelectionWidgets.get(i));
                competitorsFiltersGrid.setWidget(i + headlineRow, 2, filterValueInputWidgets.get(i));
                competitorsFiltersGrid.setWidget(i + headlineRow, 3, filterDeleteButtons.get(i));
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
