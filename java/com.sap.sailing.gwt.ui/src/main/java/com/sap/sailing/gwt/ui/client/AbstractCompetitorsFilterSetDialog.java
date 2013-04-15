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
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public abstract class AbstractCompetitorsFilterSetDialog extends DataEntryDialog<FilterSet<CompetitorDTO>> {
    private final FilterSet<CompetitorDTO> competitorsFilterSet;
    private final StringMessages stringMessages;
    private final List<Filter<CompetitorDTO, ?>> availableCompetitorsFilter;

    protected ListBox filterListBox;
    private final Label filterDescriptionLabel;
    private final Label filterDescriptionText;
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

            boolean nameNotEmpty = competitorsFilterSet.getName() != null && competitorsFilterSet.getName().length() > 0;
            if (!nameNotEmpty) {
                errorMessage = stringMessages.pleaseEnterAName();
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

    public AbstractCompetitorsFilterSetDialog(FilterSet<CompetitorDTO> competitorsFilterSet, StringMessages stringMessages, DialogCallback<FilterSet<CompetitorDTO>> callback) {
        super("Filter competitors...", null, stringMessages.ok(), stringMessages.cancel(), new CompetitorsFilterSetValidator(stringMessages), callback);
        this.competitorsFilterSet = competitorsFilterSet;
        this.stringMessages = stringMessages; 
        
        filterDescriptionLabel = new Label("Filter description:");
        filterDescriptionText = new Label();
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
        hPanel.add(new Label("Name of the filter set:"));
        hPanel.add(filterSetNameTextBox);
        
        filterListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateSelectedFilterInfo();
            }
        });

        filterListBox.addItem("Select a filter...");
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
       
        Grid grid = new Grid(2, 3);
        mainPanel.add(grid);
        grid.setWidget(0, 0, new Label("Available Filters:"));
        grid.setWidget(0, 1, filterListBox);
        grid.setWidget(0, 2, addFilterButton);

        grid.setWidget(1, 0, filterDescriptionLabel);
        grid.setWidget(1, 1, filterDescriptionText);       

        mainPanel.add(competitorsFiltersGrid);
        
        for(Filter<CompetitorDTO, ?> filter: competitorsFilterSet.getFilters()) {
            createFilterNameLabel(filter);
            createFilterSelectionListBox(filter);
            createFilterValueWidget(filter);
            createFilterDeleteButton(filter);
        }

        updateCompetitorsFiltersGrid(mainPanel);
        
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
            operatorsListBox.addItem(op.name());
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

    private Widget createFilterDeleteButton(Filter<CompetitorDTO,?> filter) {
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
            filterDescriptionLabel.setVisible(true);
            filterDescriptionText.setText(selectedFilter.getDescription());
        } else {
            addFilterButton.setEnabled(false);
            filterDescriptionLabel.setVisible(false);
            filterDescriptionText.setText("");
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
            FilterOperators op = FilterOperators.valueOf(operatorSelectionListbox.getItemText(operatorSelectionListbox.getSelectedIndex()));
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
            String selectedItemText = filterListBox.getItemText(selectedIndex);
            for(Filter<CompetitorDTO,?> filter: availableCompetitorsFilter) {
                if(filter.getName().equals(selectedItemText)) {
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
        if(filterCount > 0) {
            competitorsFiltersGrid = new Grid(filterCount, 4);
            competitorsFiltersGrid.setCellSpacing(4);
            for(int i = 0; i < filterCount; i++) {
                competitorsFiltersGrid.setWidget(i, 0, filterNameLabels.get(i));
                competitorsFiltersGrid.setWidget(i, 1, filterOperatorSelectionListBoxes.get(i));
                competitorsFiltersGrid.setWidget(i, 2, filterValueFields.get(i));
                competitorsFiltersGrid.setWidget(i, 3, filterDeleteButtons.get(i));
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
