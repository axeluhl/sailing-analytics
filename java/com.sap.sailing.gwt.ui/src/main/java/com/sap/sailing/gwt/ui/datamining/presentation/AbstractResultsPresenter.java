package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.dto.DistanceDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenterWithControls;
import com.sap.sailing.gwt.ui.datamining.presentation.dataproviders.AbstractResultDataProvider;
import com.sap.sailing.gwt.ui.datamining.presentation.dataproviders.DistanceDataProvider;
import com.sap.sailing.gwt.ui.datamining.presentation.dataproviders.NumberDataProvider;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public abstract class AbstractResultsPresenter implements ResultsPresenterWithControls {
    
    private enum ResultsPresenterState { BUSY, ERROR, RESULT }
    
    private final StringMessages stringMessages;
    private ResultsPresenterState state;
    
    private final DockLayoutPanel mainPanel;
    private final HorizontalPanel controlsPanel;
    private final ValueListBox<String> dataSelectionListBox;
    private final DeckLayoutPanel presentationPanel;
    
    private final HTML errorLabel;
    private final HTML labeledBusyIndicator;
    
    private final NumberDataProvider numberDataProvider;
    private final Map<String, AbstractResultDataProvider<?>> dataProviders;
    private AbstractResultDataProvider<?> currentDataProvider;
    private QueryResultDTO<?> currentResult;
    
    public AbstractResultsPresenter(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        mainPanel = new DockLayoutPanel(Unit.PX);
        
        numberDataProvider = new NumberDataProvider();
        dataProviders = new HashMap<>();
        AbstractResultDataProvider<DistanceDTO> distanceDataProvider = new DistanceDataProvider();
        dataProviders.put(distanceDataProvider.getResultType().getName(), distanceDataProvider);
        
        controlsPanel = new HorizontalPanel();
        controlsPanel.setSpacing(5);
        mainPanel.addNorth(controlsPanel, 40);
        
        Button exportButton = new Button("Export");
        exportButton.setEnabled(false);
        exportButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // TODO Do the magic
                // Push the current result to the server
                // Call a servlet to download the previously pushed result as json file
            }
        });
        addControl(exportButton);
        
        dataSelectionListBox = new ValueListBox<>(new AbstractObjectRenderer<String>() {
            @Override
            protected String convertObjectToString(String nonNullObject) {
                // TODO I18N
                return nonNullObject;
            }
        });
        dataSelectionListBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                Map<GroupKey, Number> resultValues = currentDataProvider.getData(getCurrentResult(), dataSelectionListBox.getValue());
                internalShowResult(resultValues);
            }
        });
        addControl(dataSelectionListBox);

        presentationPanel = new DeckLayoutPanel();
        mainPanel.add(presentationPanel);
        
        errorLabel = new HTML();
        errorLabel.setStyleName("chart-importantMessage");
        
        labeledBusyIndicator = new HTML(stringMessages.runningQuery());
        labeledBusyIndicator.setStyleName("chart-busyMessage");

        showError(getStringMessages().runAQuery());
    }
    
    @Override
    public void addControl(Widget controlWidget) {
        controlsPanel.add(controlWidget);
    }
    
    @Override
    public void showResult(QueryResultDTO<?> result) {
        if (result != null && !result.isEmpty()) {
            if (state != ResultsPresenterState.RESULT) {
                mainPanel.setWidgetHidden(controlsPanel, false);
                presentationPanel.setWidget(getPresentationWidget());
                state = ResultsPresenterState.RESULT;
            }
            
            this.currentResult = result;
            currentDataProvider = selectCurrentDataProvider();
            updateDataSelectionListBox();
            if (currentDataProvider != null) {
                Map<GroupKey, Number> resultValues = currentDataProvider.getData(getCurrentResult(), dataSelectionListBox.getValue());
                internalShowResult(resultValues);
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        presentationPanel.onResize();
                    }
                });
            } else {
                showError(getStringMessages().cantDisplayDataOfType(getCurrentResult().getResultType()));
            }
        } else {
            this.currentResult = null;
            showError(getStringMessages().noDataFound() + ".");
        }
    }

    private AbstractResultDataProvider<?> selectCurrentDataProvider() {
        if (numberDataProvider.acceptsResultsOfType(getCurrentResult().getResultType())) {
            return numberDataProvider;
        }
        return dataProviders.get(getCurrentResult().getResultType());
    }
    
    private void updateDataSelectionListBox() {
        if (currentDataProvider == null) {
            dataSelectionListBox.setAcceptableValues(Collections.<String>emptyList());
        } else {
            Collection<String> dataKeys = currentDataProvider.getDataKeys();
            String keyToSelect = currentDataProvider.getDefaultDataKeyFor(getCurrentResult());
            dataSelectionListBox.setValue(keyToSelect, false);
            dataSelectionListBox.setAcceptableValues(dataKeys);
        }
    }

    protected abstract Widget getPresentationWidget();
    
    protected abstract void internalShowResult(Map<GroupKey, Number> resultValues);

    @Override
    public void showError(String error) {
        if (state != ResultsPresenterState.ERROR) {
            mainPanel.setWidgetHidden(controlsPanel, true);
            errorLabel.setHTML(error);
            state = ResultsPresenterState.ERROR;
        }
        
        currentResult = null;
        presentationPanel.setWidget(errorLabel);
    }
    
    @Override
    public void showError(String mainError, Iterable<String> detailedErrors) {
        StringBuilder errorBuilder = new StringBuilder(mainError + ":<br /><ul>");
        for (String detailedError : detailedErrors) {
            errorBuilder.append("<li>" + detailedError + "</li>");
        }
        errorBuilder.append("</ul>");
        showError(errorBuilder.toString());
    }
    
    @Override
    public void showBusyIndicator() {
        if (state != ResultsPresenterState.BUSY) {
            presentationPanel.setWidget(labeledBusyIndicator);
            state = ResultsPresenterState.BUSY;
        }
        
        currentResult = null;
    }
    
    protected StringMessages getStringMessages() {
        return stringMessages;
    }
    
    @Override
    public QueryResultDTO<?> getCurrentResult() {
        return currentResult;
    }

    @Override
    public Widget getWidget() {
        return mainPanel;
    }

}
