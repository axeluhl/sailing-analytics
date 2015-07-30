package com.sap.sailing.gwt.ui.datamining.presentation;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenterWithControls;
import com.sap.sse.datamining.shared.QueryResult;

public abstract class AbstractResultsPresenter<ResultType> implements ResultsPresenterWithControls<ResultType> {
    
    private enum ResultsPresenterState { BUSY, ERROR, RESULT }
    
    private final StringMessages stringMessages;
    private ResultsPresenterState state;
    
    private final DockLayoutPanel mainPanel;
    private final HorizontalPanel controlsPanel;
    private final DeckLayoutPanel presentationPanel;
    
    private final HTML errorLabel;
    private final HTML labeledBusyIndicator;
    
    private QueryResult<ResultType> currentResult;
    
    public AbstractResultsPresenter(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        mainPanel = new DockLayoutPanel(Unit.PX);
        
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
    public void showResult(QueryResult<ResultType> result) {
        if (result != null && !result.isEmpty()) {
            if (state != ResultsPresenterState.RESULT) {
                mainPanel.setWidgetHidden(controlsPanel, false);
                presentationPanel.setWidget(getPresentationWidget());
                state = ResultsPresenterState.RESULT;
            }
            
            this.currentResult = result;
            internalShowResult();
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    presentationPanel.onResize();
                }
            });
        } else {
            this.currentResult = null;
            showError(getStringMessages().noDataFound() + ".");
        }
    }
    
    protected abstract Widget getPresentationWidget();
    
    protected abstract void internalShowResult();

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
    public QueryResult<ResultType> getCurrentResult() {
        return currentResult;
    }

    @Override
    public Widget getWidget() {
        return mainPanel;
    }

}
