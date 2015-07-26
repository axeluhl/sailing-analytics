package com.sap.sailing.gwt.ui.datamining.presentation;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenter;
import com.sap.sse.datamining.shared.QueryResult;

public abstract class AbstractResultsPresenter<ResultType> implements ResultsPresenter<ResultType> {
    
    private enum ResultsPresenterState { BUSY, ERROR, RESULT }
    
    private final StringMessages stringMessages;
    private ResultsPresenterState state;
    
    private final DockLayoutPanel mainPanel;
    private final HorizontalPanel controlsPanel;
    private final SimpleLayoutPanel presentationPanel;
    
    private final HTML errorLabel;
    private final HTML labeledBusyIndicator;
    
    private QueryResult<ResultType> result;
    
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
        addControlWidget(exportButton);
        
        presentationPanel = new SimpleLayoutPanel();
        mainPanel.add(presentationPanel);
        
        errorLabel = new HTML();
        errorLabel.setStyleName("chart-importantMessage");
        
        labeledBusyIndicator = new HTML(stringMessages.runningQuery());
        labeledBusyIndicator.setStyleName("chart-busyMessage");

        showError(getStringMessages().runAQuery());
    }
    
    protected void addControlWidget(Widget controlWidget) {
        controlsPanel.add(controlWidget);
    }
    
    @Override
    public void showResult(QueryResult<ResultType> result) {
        if (state != ResultsPresenterState.RESULT) {
            mainPanel.setWidgetHidden(controlsPanel, false);
            presentationPanel.setWidget(getPresentationWidget());
            state = ResultsPresenterState.RESULT;
        }
        
        this.result = result;
        internalShowResult();
        presentationPanel.onResize();
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
        
        result = null;
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
        
        result = null;
    }
    
    protected StringMessages getStringMessages() {
        return stringMessages;
    }
    
    @Override
    public QueryResult<ResultType> getCurrentResult() {
        return result;
    }

    @Override
    public Widget getWidget() {
        return mainPanel;
    }

}
