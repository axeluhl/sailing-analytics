package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.domain.common.filter.FilterOperators;
import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public abstract class AbstractCompetitorsFilterSetDialog extends DataEntryDialog<FilterSet<CompetitorDTO>> {
    private final FilterSet<CompetitorDTO> competitorsFilterSet;
    private final StringMessages stringMessages;
    private final List<Filter<CompetitorDTO, ?>> availableCompetitorsFilter;

    protected ListBox filterListBox;
    private final Button addFilterButton;

    protected TextBox filterSetNameTextBox;
    private Grid competitorsFiltersGrid;
    private VerticalPanel mainPanel;
    
    private final List<ListBox> filterOperatorSelectionListBoxes;
    private final List<Widget> filterValueFields;
    private final List<Label> filterNameLabels;
    private final List<Button> filterDeleteButtons;
    
    protected static class CompetitorsFilterSetValidator implements Validator<FilterSet<CompetitorDTO>> {
        private final StringMessages stringMessages;
        
        public CompetitorsFilterSetValidator(StringMessages stringMessages){
            super();
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(FilterSet<CompetitorDTO> competitorsFilterSet) {
            String errorMessage = null;

            int filterCount = competitorsFilterSet.getFilters().size();
            boolean nameNotEmpty = competitorsFilterSet.getName() != null && competitorsFilterSet.getName().length() > 0;
            if (!nameNotEmpty) {
                errorMessage = stringMessages.pleaseEnterAName();
            } else if(filterCount < 1) {
                errorMessage = "Please add at least one filter criteria.";
            } else {
                for(Filter<CompetitorDTO, ?> filter: competitorsFilterSet.getFilters()) {
                    Object filterValue = filter.getConfiguration().getB();
                    if(filterValue == null) {
                        errorMessage = stringMessages.pleaseEnterAValue();
                        break;
                    }
                }
            }

            return errorMessage;
        }
    }

    public AbstractCompetitorsFilterSetDialog(FilterSet<CompetitorDTO> competitorsFilterSet, String dialogTitle, StringMessages stringMessages, DialogCallback<FilterSet<CompetitorDTO>> callback) {
        super(dialogTitle, null, stringMessages.ok(), stringMessages.cancel(), new CompetitorsFilterSetValidator(stringMessages), callback);
        this.competitorsFilterSet = competitorsFilterSet;
        this.stringMessages = stringMessages; 
        
        competitorsFiltersGrid = new Grid(0,0);

        filterOperatorSelectionListBoxes = new ArrayList<ListBox>();
        filterValueFields = new ArrayList<Widget>();
        filterNameLabels = new ArrayList<Label>();
        filterDeleteButtons = new ArrayList<Button>();
        
        addFilterButton = new Button(stringMessages.add());
        
        availableCompetitorsFilter = new ArrayList<Filter<CompetitorDTO, ?>>();
        availableCompetitorsFilter.add(new CompetitorRankFilter());
        availableCompetitorsFilter.add(new CompetitorNationalityFilter());
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        mainPanel = new VerticalPanel();

        HorizontalPanel hPanel = new HorizontalPanel();
        mainPanel.add(hPanel);
        hPanel.add(new Label("Filter name:"));
        hPanel.add(filterSetNameTextBox);
        
        filterListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateSelectedFilterInfo();
            }
        });

        filterListBox.addItem("Select a filter criteria...");
        for(Filter<?,?> filter: availableCompetitorsFilter) {
            filterListBox.addItem(filter.getName());
        }
        updateSelectedFilterInfo();

        addFilterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Filter<CompetitorDTO,?> selectedFilter = getSelectedFilter();
                if(selectedFilter != null) {
                    createFilterNameLabel(selectedFilter);
                    createFilterSelectionListBox(selectedFilter);
                    createFilterValueWidget(selectedFilter);
                    createFilterDeleteButton(selectedFilter);
                }
                updateCompetitorsFiltersGrid(mainPanel);
                validate();
            }
        });       
       
        mainPanel.add(competitorsFiltersGrid);
        
        for(Filter<CompetitorDTO, ?> filter: competitorsFilterSet.getFilters()) {
            createFilterNameLabel(filter);
            createFilterSelectionListBox(filter);
            createFilterValueWidget(filter);
            createFilterDeleteButton(filter);
        }

        updateCompetitorsFiltersGrid(mainPanel);

        HorizontalPanel addFilterPanel = new HorizontalPanel();
        mainPanel.add(addFilterPanel);
        addFilterPanel.add(new Label("Filter criterias:"));
        addFilterPanel.add(filterListBox);
        addFilterPanel.add(addFilterButton);

        return mainPanel;
    }

    private Label createFilterNameLabel(Filter<CompetitorDTO,?> filter) {
        Label filterNameLabel = new Label(filter.getName()); 
        filterNameLabels.add(filterNameLabel);
        return filterNameLabel; 
    }

    private ListBox createFilterSelectionListBox(Filter<CompetitorDTO,?> filter) {
        Iterable<FilterOperators> supportedOperators = filter.getSupportedOperators();
        ListBox operatorsListBox = this.createListBox(false);
        int i = 0;
        for(FilterOperators op: supportedOperators) {
            operatorsListBox.addItem(FilterOperatorsFormatter.format(op), op.name());
            if(filter.getConfiguration().getA() != null && filter.getConfiguration().getA().equals(op)) {
                operatorsListBox.setSelectedIndex(i);
            } else if (filter.getDefaultOperator() != null && filter.getDefaultOperator().equals(op)) {
                operatorsListBox.setSelectedIndex(i);
            }
            i++;
        }
        filterOperatorSelectionListBoxes.add(operatorsListBox);
        return operatorsListBox;
    }

    private Widget createFilterValueWidget(Filter<CompetitorDTO,?> filter) {
        Widget filterValueWidget = null;
        if(filter.getValueType().equals(String.class)) {
            String initialValue = null;
            if(filter.getConfiguration().getB() != null) {
                initialValue = (String) filter.getConfiguration().getB(); 
            }
            TextBox textBox = createTextBox(initialValue); 
            textBox.setVisibleLength(20);
            textBox.setFocus(true);
            filterValueWidget = textBox;
        } else if(filter.getValueType().equals(Integer.class)) {
            Integer initialValue = null;
            if(filter.getConfiguration().getB() != null) {
                initialValue = (Integer) filter.getConfiguration().getB(); 
            }
            IntegerBox integerBox = createIntegerBox(initialValue, 20); 
            integerBox.setFocus(true);
            filterValueWidget = integerBox;
        }
        filterValueFields.add(filterValueWidget);        
        return filterValueWidget; 
    }

    private Button createFilterDeleteButton(Filter<CompetitorDTO,?> filter) {
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
                filterNameLabels.remove(index);
                filterOperatorSelectionListBoxes.remove(index);
                filterValueFields.remove(index);
                filterDeleteButtons.remove(index);
                updateCompetitorsFiltersGrid(mainPanel);
                validate();
            }
        });
        return filterDeleteBtn; 
    }
    
    private void updateSelectedFilterInfo() {
        Filter<CompetitorDTO,?> selectedFilter = getSelectedFilter();
        if(selectedFilter != null) {
            addFilterButton.setEnabled(true);
        } else {
            addFilterButton.setEnabled(false);
        }
    }

    @Override
    protected FilterSet<CompetitorDTO> getResult() {
        FilterSet<CompetitorDTO> result = new FilterSet<CompetitorDTO>(filterSetNameTextBox.getText());

        int filterCount = filterNameLabels.size();
        for (int i = 0; i < filterCount; i++) {
            String filterName = filterNameLabels.get(i).getText();
            Filter<CompetitorDTO,?> filter = findFilterByName(filterName);
            ListBox operatorSelectionListbox = filterOperatorSelectionListBoxes.get(i);
            FilterOperators op = FilterOperators.valueOf(operatorSelectionListbox.getValue(operatorSelectionListbox.getSelectedIndex()));
            if(filter.getValueType().equals(Integer.class)) {
                IntegerBox integerBox = (IntegerBox) filterValueFields.get(i);
                Filter<CompetitorDTO,Integer> newFilter = (Filter<CompetitorDTO, Integer>) filter.copy();
                newFilter.setConfiguration(new Pair<FilterOperators, Integer>(op, integerBox.getValue()));
                result.addFilter(newFilter);
            } else if(filter.getValueType().equals(String.class)) {
                TextBox textBox = (TextBox) filterValueFields.get(i);
                Filter<CompetitorDTO,String> newFilter = (Filter<CompetitorDTO, String>) filter.copy();
                newFilter.setConfiguration(new Pair<FilterOperators, String>(op, textBox.getText()));
                result.addFilter(newFilter);
            }
        }

        return result;
    }

    private Filter<CompetitorDTO,?> findFilterByName(String filterName) {
        Filter<CompetitorDTO,?> result = null;
        for(Filter<CompetitorDTO,?> filter: availableCompetitorsFilter) {
            if(filter.getName().equals(filterName)) {
                result = filter;
                break;
            }
        }
        return result;
    }
    
    private Filter<CompetitorDTO,?> getSelectedFilter() {
        Filter<CompetitorDTO,?> result = null;
        int selectedIndex = filterListBox.getSelectedIndex();
        if(selectedIndex > 0) {
            String selectedItemValue = filterListBox.getValue(selectedIndex);
            for(Filter<CompetitorDTO,?> filter: availableCompetitorsFilter) {
                if(filter.getName().equals(selectedItemValue)) {
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
                competitorsFiltersGrid.setWidget(0 ,0, new Label("Filter criteria"));
                competitorsFiltersGrid.setWidget(0 ,1, new Label("Operator"));
                competitorsFiltersGrid.setWidget(0 ,2, new Label("Value"));
            }
            
            for(int i = 0; i < filterCount; i++) {
                competitorsFiltersGrid.setWidget(i + headlineRow, 0, filterNameLabels.get(i));
                competitorsFiltersGrid.setWidget(i + headlineRow, 1, filterOperatorSelectionListBoxes.get(i));
                competitorsFiltersGrid.setWidget(i + headlineRow, 2, filterValueFields.get(i));
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
