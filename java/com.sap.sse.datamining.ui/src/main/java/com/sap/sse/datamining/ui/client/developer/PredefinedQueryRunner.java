package com.sap.sse.datamining.ui.client.developer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.PredefinedQueryIdentifier;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.QueryDefinitionProvider;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentWithoutSettings;
import com.sap.sse.gwt.client.shared.controls.AbstractObjectRenderer;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class PredefinedQueryRunner extends ComponentWithoutSettings {

    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final QueryDefinitionProvider<?> queryDefinitionProvider;
    private final Consumer<StatisticQueryDefinitionDTO> queryRunner;

    private final Button showDialogButton;
    private final DialogBox dialogBox;
    private final SimpleBusyIndicator busyIndicator;
    private final ValueListBox<PredefinedQueryIdentifier> selectionListBox;
    private final Button runButton;

    public PredefinedQueryRunner(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            QueryDefinitionProvider<?> queryDefinitionProvider, Consumer<StatisticQueryDefinitionDTO> queryRunner) {
        super(parent, context);
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        this.queryDefinitionProvider = queryDefinitionProvider;
        this.queryRunner = queryRunner;

        showDialogButton = new Button(stringMessages.runPredefinedQuery(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                show();
            }
        });

        dialogBox = new DialogBox(false, true);
        dialogBox.setText(stringMessages.runPredefinedQuery());
        dialogBox.setAnimationEnabled(true);

        Button closeButton = new Button(stringMessages.close(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });

        runButton = new Button(stringMessages.run(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                runSelectedPredefinedQuery();
            }
        });

        HorizontalPanel contentPanel = new HorizontalPanel();
        contentPanel.setSpacing(5);
        contentPanel.add(new Label(stringMessages.selectPredefinedQuery() + ":"));

        busyIndicator = new SimpleBusyIndicator(true, 0.7f);
        busyIndicator.setVisible(false);
        contentPanel.add(busyIndicator);

        selectionListBox = new ValueListBox<>(new AbstractObjectRenderer<PredefinedQueryIdentifier>() {
            @Override
            protected String convertObjectToString(PredefinedQueryIdentifier nonNullObject) {
                return nonNullObject.getDescription();
            }
        });
        contentPanel.add(selectionListBox);

        DockPanel dockPanel = new DockPanel();
        dockPanel.setSpacing(4);
        HorizontalPanel dialogButtonsPanel = new HorizontalPanel();
        dialogButtonsPanel.setSpacing(5);
        dialogButtonsPanel.add(runButton);
        dialogButtonsPanel.add(closeButton);
        dockPanel.add(dialogButtonsPanel, DockPanel.SOUTH);
        dockPanel.setCellHorizontalAlignment(dialogButtonsPanel, DockPanel.ALIGN_RIGHT);
        dockPanel.add(contentPanel, DockPanel.CENTER);
        dockPanel.setWidth("100%");
        dialogBox.setWidget(dockPanel);
    }

    public void show() {
        runButton.setEnabled(false);
        selectionListBox.setVisible(false);
        busyIndicator.setVisible(true);
        dialogBox.center();
        dataMiningService.getPredefinedQueryIdentifiers(new AsyncCallback<HashSet<PredefinedQueryIdentifier>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the predefined query names: " + caught.getMessage());
                busyIndicator.setVisible(false);
            }

            @Override
            public void onSuccess(HashSet<PredefinedQueryIdentifier> predefinedQueryIdentifiers) {
                if (!predefinedQueryIdentifiers.isEmpty()) {
                    PredefinedQueryIdentifier valueToBeSelected = Util.get(predefinedQueryIdentifiers, 0);
                    List<PredefinedQueryIdentifier> acceptableValues = new ArrayList<>(predefinedQueryIdentifiers);
                    selectionListBox.setValue(valueToBeSelected);
                    selectionListBox.setAcceptableValues(acceptableValues);

                    runButton.setEnabled(true);
                    selectionListBox.setVisible(true);
                } else {
                    errorReporter.reportError("No predefined query names have been found.");
                }
                busyIndicator.setVisible(false);
            }
        });
    }

    protected void runSelectedPredefinedQuery() {
        PredefinedQueryIdentifier predefinedQueryIdentifier = selectionListBox.getValue();
        dataMiningService.getPredefinedQueryDefinition(predefinedQueryIdentifier,
                LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<StatisticQueryDefinitionDTO>() {
                    @Override
                    public void onSuccess(StatisticQueryDefinitionDTO queryDefinition) {
                        queryDefinitionProvider.applyQueryDefinition(queryDefinition);
                        queryRunner.accept(queryDefinition);
                    }
                    @Override
                    public void onFailure(Throwable error) {
                        errorReporter.reportError("Error running the query: " + error.getMessage());
                    }
                });
        dialogBox.hide();
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.predefinedQueryRunner();
    }

    @Override
    public Widget getEntryWidget() {
        return showDialogButton;
    }

    @Override
    public boolean isVisible() {
        return showDialogButton.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        showDialogButton.setVisible(visibility);
    }

    @Override
    public String getDependentCssClassName() {
        return "predefinedQueryRunner";
    }

    @Override
    public String getId() {
        return "PredefinedQueryRunner";
    }
}
