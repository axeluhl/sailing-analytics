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
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.FilterOperatorsFormatter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public abstract class AbstractCompetitorsFilterSetDialog extends DataEntryDialog<FilterSet<CompetitorDTO>> {
    private final FilterSet<CompetitorDTO> competitorsFilterSet;
    private final StringMessages stringMessages;
    private final List<Filter<CompetitorDTO, ?>> availableCompetitorsFilter;

    private ListBox filterListBox;
    private final Button addFilterButton;

    protected TextBox filterSetNameTextBox;
    private Grid competitorsFiltersGrid;
    private VerticalPanel mainPanel;
    
    private final List<ListBox> filterOperatorSelectionListBoxes;
    private final List<Widget> filterValueFields;
    private final List<Label> filterNameLabels;
    private final List<String> filterNames;
    private final List<Button> filterDeleteButtons;
    
    protected static class CompetitorsFilterSetValidator implements Validator<FilterSet<CompetitorDTO>> {
        private final StringMessages stringMessages;
        private final List<String> existingFilterSetNames;
        
        public CompetitorsFilterSetValidator(List<String> existingFilterSetNames, StringMessages stringMessages){
            super();
            this.existingFilterSetNames = existingFilterSetNames;
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(FilterSet<CompetitorDTO> competitorsFilterSet) {
            String errorMessage = null;

            int filterCount = competitorsFilterSet.getFilters().size();
            boolean nameNotEmpty = competitorsFilterSet.getName() != null && competitorsFilterSet.getName().length() > 0;
            if (!nameNotEmpty) {
                errorMessage = stringMessages.pleaseEnterAName();
            } else if (existingFilterSetNames.contains(competitorsFilterSet.getName())) {
                errorMessage = "A filter with this name already exists.";
            } else if(filterCount < 1) {
                errorMessage = "Add at least one filter criteria.";
            } else {
                for(Filter<CompetitorDTO, ?> filter: competitorsFilterSet.getFilters()) {
                    Object filterValue = filter.getConfiguration().getB();
                    if(filter.getValueType().equals(Integer.class)) {
                        if(filterValue != null) {
                            Integer intfilterValue = (Integer) filterValue;
                            if(intfilterValue <= 0) {
                                errorMessage = stringMessages.numberMustBePositive();
                                break;
                            }
                        } else {
                            errorMessage = stringMessages.pleaseEnterANumber();
                            break;
                        }
                    } else if(filter.getValueType().equals(String.class)) {
                        if(filterValue == null) {
                            errorMessage = stringMessages.pleaseEnterAValue();
                            break;
                        }
                    }
                }
            }

            return errorMessage;
        }
    }

    public AbstractCompetitorsFilterSetDialog(FilterSet<CompetitorDTO> competitorsFilterSet, List<Filter<CompetitorDTO, ?>> availableCompetitorsFilter, List<String> existingFilterSetNames, 
            String dialogTitle, StringMessages stringMessages, DialogCallback<FilterSet<CompetitorDTO>> callback) {
        super(dialogTitle, null, stringMessages.ok(), stringMessages.cancel(),
                new CompetitorsFilterSetValidator(existingFilterSetNames, stringMessages), callback);
        this.competitorsFilterSet = competitorsFilterSet;
        this.availableCompetitorsFilter = availableCompetitorsFilter;
        this.stringMessages = stringMessages; 
        
        competitorsFiltersGrid = new Grid(0,0);

        filterOperatorSelectionListBoxes = new ArrayList<ListBox>();
        filterValueFields = new ArrayList<Widget>();
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
        hPanel.add(new Label("Filter name:"));
        hPanel.add(filterSetNameTextBox);
        
        filterListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateSelectedFilterInfo();
            }
        });

        filterListBox.addItem("Select a filter criteria...");
        for(Filter<CompetitorDTO,?> filter: availableCompetitorsFilter) {
            filterListBox.addItem(CompetitorsFilterFormatter.format(filter));
        }
        updateSelectedFilterInfo();

        addFilterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Filter<CompetitorDTO,?> selectedFilter = getSelectedFilter();
                if(selectedFilter != null) {
                    createFilterNameAndLabel(selectedFilter);
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
            createFilterNameAndLabel(filter);
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

    private Label createFilterNameAndLabel(Filter<CompetitorDTO,?> filter) {
        Label filterNameLabel = new Label(CompetitorsFilterFormatter.format(filter) + ":"); 
        filterNameLabels.add(filterNameLabel);
        filterNames.add(filter.getName());
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
                filterNames.remove(index);
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
            String filterName = filterNames.get(i);
            Filter<CompetitorDTO,?> filter = CompetitorsFilterFactory.getFilter(filterName);
            ListBox operatorSelectionListbox = filterOperatorSelectionListBoxes.get(i);
            FilterOperators op = FilterOperators.valueOf(operatorSelectionListbox.getValue(operatorSelectionListbox.getSelectedIndex()));
            if(filter.getValueType().equals(Integer.class)) {
                IntegerBox integerBox = (IntegerBox) filterValueFields.get(i);
                Filter<CompetitorDTO,Integer> newFilter = (Filter<CompetitorDTO, Integer>) filter;
                newFilter.setConfiguration(new Pair<FilterOperators, Integer>(op, integerBox.getValue()));
                result.addFilter(newFilter);
            } else if(filter.getValueType().equals(String.class)) {
                TextBox textBox = (TextBox) filterValueFields.get(i);
                Filter<CompetitorDTO,String> newFilter = (Filter<CompetitorDTO, String>) filter;
                newFilter.setConfiguration(new Pair<FilterOperators, String>(op, textBox.getText()));
                result.addFilter(newFilter);
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
                if(selectedItemValue.equals(CompetitorsFilterFormatter.format(filter))) {
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
