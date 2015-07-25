package com.sap.sailing.gwt.ui.datamining.presentation;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenter;
import com.sap.sse.datamining.shared.QueryResult;

public abstract class AbstractResultsPresenter<ResultType> implements ResultsPresenter<ResultType> {
    
    private final DockLayoutPanel widget;
    private final HorizontalPanel controlsPanel;
    
    private QueryResult<ResultType> result;
    
    public AbstractResultsPresenter() {
        widget = new DockLayoutPanel(Unit.PX);
        
        controlsPanel = new HorizontalPanel();
        controlsPanel.setSpacing(5);
        widget.addNorth(controlsPanel, 40);
        
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
    }
    
    protected void setPresentationWidget(Widget presentationWidget) {
        widget.add(presentationWidget);
    }
    
    protected void addControlWidget(Widget controlWidget) {
        controlsPanel.add(controlWidget);
    }
    
    @Override
    public void showResult(QueryResult<ResultType> result) {
        this.result = result;
        widget.setWidgetHidden(controlsPanel, false);
        internalShowResult(result);
    }
    
    protected abstract void internalShowResult(QueryResult<ResultType> result);

    @Override
    public void showError(String error) {
        this.result = null;
        widget.setWidgetHidden(controlsPanel, true);
        internalShowError(error);
    }
    
    protected abstract void internalShowError(String error);

    @Override
    public void showError(String mainError, Iterable<String> detailedErrors) {
        this.result = null;
        widget.setWidgetHidden(controlsPanel, true);
        internalShowError(mainError, detailedErrors);
    }
    
    protected abstract void internalShowError(String mainError, Iterable<String> detailedErrors);
    
    @Override
    public QueryResult<ResultType> getCurrentResult() {
        return result;
    }

    @Override
    public Widget getWidget() {
        return widget;
    }

}
