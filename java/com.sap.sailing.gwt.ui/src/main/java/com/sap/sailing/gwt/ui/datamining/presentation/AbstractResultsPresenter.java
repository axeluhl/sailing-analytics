package com.sap.sailing.gwt.ui.datamining.presentation;

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
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenterWithControls;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public abstract class AbstractResultsPresenter<SettingsType extends Settings> implements ResultsPresenterWithControls<SettingsType> {
    
    private enum ResultsPresenterState { BUSY, ERROR, RESULT }
    
    protected final StringMessages stringMessages;
    private ResultsPresenterState state;
    
    private final DockLayoutPanel mainPanel;
    private final HorizontalPanel controlsPanel;
    protected final ValueListBox<String> dataSelectionListBox;
    protected final DeckLayoutPanel presentationPanel;
    
    private final HTML errorLabel;
    private final HTML labeledBusyIndicator;
    
    private QueryResultDTO<?> currentResult;
    private boolean isCurrentResultSimple;
    
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
//        addControl(exportButton);
        
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
                onDataSelectionValueChange();
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
    
    abstract protected void onDataSelectionValueChange();

    @Override
    public void addControl(Widget controlWidget) {
        controlsPanel.add(controlWidget);
    }
    
    @Override
    public void removeControl(Widget controlWidget) {
        controlsPanel.remove(controlWidget);
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
            updateIsCurrentResultSimple();
            
            internalShowResults(getCurrentResult());
        } else {
            this.currentResult = null;
            updateIsCurrentResultSimple();
            showError(getStringMessages().noDataFound() + ".");
        }
    }

    abstract protected void internalShowResults(QueryResultDTO<?> result);

    protected abstract Widget getPresentationWidget();

    @Override
    public void showError(String error) {
        if (state != ResultsPresenterState.ERROR) {
            mainPanel.setWidgetHidden(controlsPanel, true);
            errorLabel.setHTML(error);
            state = ResultsPresenterState.ERROR;
        }
        
        currentResult = null;
        updateIsCurrentResultSimple();
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
        updateIsCurrentResultSimple();
    }
    
    private void updateIsCurrentResultSimple() {
        boolean isSimple = false;
        if (currentResult != null) {
            isSimple = true;
            for (GroupKey groupKey : getCurrentResult().getResults().keySet()) {
                if (groupKey.hasSubKey()) {
                    isSimple = false;
                    break;
                }
            }
        }
        isCurrentResultSimple = isSimple;
    }

    protected boolean isCurrentResultSimple() {
        return isCurrentResultSimple;
    }
    
    protected StringMessages getStringMessages() {
        return stringMessages;
    }
    
    @Override
    public QueryResultDTO<?> getCurrentResult() {
        return currentResult;
    }

    @Override
    public Widget getEntryWidget() {
        return mainPanel;
    }

    @Override
    public boolean isVisible() {
        return mainPanel.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        mainPanel.setVisible(visibility);
    }

}
