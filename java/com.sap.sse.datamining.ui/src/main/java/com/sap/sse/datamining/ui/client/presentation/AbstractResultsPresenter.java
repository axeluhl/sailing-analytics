package com.sap.sse.datamining.ui.client.presentation;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.ResultsPresenter;
import com.sap.sse.datamining.ui.client.WithControls;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public abstract class AbstractResultsPresenter<SettingsType extends Settings>
        extends AbstractDataMiningComponent<SettingsType> implements ResultsPresenter<SettingsType>, WithControls {

    private enum ResultsPresenterState {
        BUSY, ERROR, RESULT
    }

    private ResultsPresenterState state;

    private final DockLayoutPanel mainPanel;
    private final HorizontalPanel controlsPanel;
    protected final DeckLayoutPanel presentationPanel;

    private final HTML errorLabel;
    private final HTML labeledBusyIndicator;

    private StatisticQueryDefinitionDTO currentQueryDefinition;
    private QueryResultDTO<?> currentResult;
    private boolean isCurrentResultSimple;

    public AbstractResultsPresenter(Component<?> parent, ComponentContext<?> context) {
        super(parent, context);
        mainPanel = new DockLayoutPanel(Unit.PX);
        controlsPanel = new HorizontalPanel();
        controlsPanel.setSpacing(5);
        controlsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
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
        // addControl(exportButton);
        presentationPanel = new DeckLayoutPanel();
        mainPanel.add(presentationPanel);
        errorLabel = new HTML();
        errorLabel.setStyleName("chart-importantMessage");
        labeledBusyIndicator = new HTML(getDataMiningStringMessages().runningQuery());
        labeledBusyIndicator.setStyleName("chart-busyMessage");
        showError(getDataMiningStringMessages().runAQuery());
    }

    @Override
    public void addControl(Widget controlWidget) {
        controlsPanel.add(controlWidget);
    }

    @Override
    public void removeControl(Widget controlWidget) {
        controlsPanel.remove(controlWidget);
    }

    @Override
    public void showResult(StatisticQueryDefinitionDTO queryDefinition, QueryResultDTO<?> result) {
        if (result != null && !result.isEmpty()) {
            if (state != ResultsPresenterState.RESULT) {
                mainPanel.setWidgetHidden(controlsPanel, false);
                presentationPanel.setWidget(getPresentationWidget());
                state = ResultsPresenterState.RESULT;
            }
            this.currentResult = result;
            this.currentQueryDefinition = queryDefinition;
            updateCurrentResultInfo();
            internalShowResults(getCurrentResult());
        } else {
            showError(getDataMiningStringMessages().noDataFound() + ".");
        }
    }

    abstract protected void internalShowResults(QueryResultDTO<?> result);

    protected abstract Widget getPresentationWidget();

    @Override
    public void showError(String error) {
        showError(SafeHtmlUtils.fromString(error));
    }

    @Override
    public void showError(String mainError, Iterable<String> detailedErrors) {
        StringBuilder errorBuilder = new StringBuilder(mainError + ":<br /><ul>");
        for (String detailedError : detailedErrors) {
            errorBuilder.append("<li>" + detailedError + "</li>");
        }
        errorBuilder.append("</ul>");
        showError(SafeHtmlUtils.fromTrustedString(errorBuilder.toString()));
    }

    private void showError(SafeHtml error) {
        if (state != ResultsPresenterState.ERROR) {
            mainPanel.setWidgetHidden(controlsPanel, true);
            presentationPanel.setWidget(errorLabel);
            state = ResultsPresenterState.ERROR;
        }
        errorLabel.setHTML(error);
        currentResult = null;
        currentQueryDefinition = null;
        updateCurrentResultInfo();
    }

    @Override
    public void showBusyIndicator() {
        if (state != ResultsPresenterState.BUSY) {
            mainPanel.setWidgetHidden(controlsPanel, true);
            presentationPanel.setWidget(labeledBusyIndicator);
            state = ResultsPresenterState.BUSY;
        }
        currentResult = null;
        currentQueryDefinition = null;
        updateCurrentResultInfo();
    }

    private void updateCurrentResultInfo() {
        boolean isSimple = false;
        if (currentResult != null) {
            isSimple = true;
            for (GroupKey groupKey : getCurrentResult().getResults().keySet()) {
                int size = groupKey.size();
                if (size != 1) {
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

    @Override
    public QueryResultDTO<?> getCurrentResult() {
        return currentResult;
    }
    
    @Override
    public StatisticQueryDefinitionDTO getCurrentQueryDefinition() {
        return currentQueryDefinition;
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
