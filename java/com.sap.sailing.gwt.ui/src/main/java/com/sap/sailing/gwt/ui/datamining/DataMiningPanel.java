package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.datamining.execution.SimpleQueryRunner;
import com.sap.sailing.gwt.ui.datamining.presentation.BenchmarkResultsPanel;
import com.sap.sailing.gwt.ui.datamining.presentation.ResultsChart;
import com.sap.sailing.gwt.ui.datamining.selection.QueryDefinitionProviderWithControls;
import com.sap.sailing.gwt.ui.datamining.settings.QueryRunnerSettings;
import com.sap.sailing.gwt.ui.datamining.settings.RefreshingSelectionTablesSettings;

public class DataMiningPanel extends FlowPanel {

    private static DataMiningResources resources = GWT.create(DataMiningResources.class);

    private StringMessages stringMessages;

    private final QueryDefinitionProvider queryDefinitionProvider;
    private final ResultsPresenter<Number> resultsPresenter;
    private final QueryRunner queryRunner;

    public DataMiningPanel(StringMessages stringMessages, SailingServiceAsync sailingService, ErrorReporter errorReporter, boolean showBenchmark) {
        this.stringMessages = stringMessages;
        this.addStyleName("dataMiningPanel");

        QueryDefinitionProviderWithControls queryDefinitionProviderWithControls = new QueryDefinitionProviderWithControls(stringMessages, sailingService, errorReporter);
        queryDefinitionProvider = queryDefinitionProviderWithControls;
        resultsPresenter = new ResultsChart(stringMessages);
        queryRunner = new SimpleQueryRunner(stringMessages, sailingService, errorReporter, queryDefinitionProvider, resultsPresenter);

        queryDefinitionProviderWithControls.addControl(queryRunner.getEntryWidget());
        queryDefinitionProviderWithControls.addControl(createSettingsControlWidget());

        this.add(queryDefinitionProvider.getEntryWidget());
        this.add(resultsPresenter.getWidget());

        queryRunner.run(queryDefinitionProvider.getQueryDefinition());

        if (showBenchmark) {
            BenchmarkResultsPanel benchmarkPanel = new BenchmarkResultsPanel(stringMessages, sailingService, errorReporter, queryDefinitionProvider);
            this.add(benchmarkPanel);
        }
    }

    private Widget createSettingsControlWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(5);
        
        Label runnerSettingsLabel = new Label(queryRunner.getLocalizedShortName());
        panel.add(runnerSettingsLabel);
        Anchor runnerSettingsAnchor = new Anchor(AbstractImagePrototype.create(resources.settingsIcon()).getSafeHtml());
        runnerSettingsAnchor.setTitle(stringMessages.settings());
        runnerSettingsAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<QueryRunnerSettings>(queryRunner, stringMessages).show();
            }
        });
        panel.add(runnerSettingsAnchor);
        
        Label selectionTablesSettingsLabel = new Label(queryDefinitionProvider.getSelectionProvider().getLocalizedShortName());
        panel.add(selectionTablesSettingsLabel);
        Anchor selectionTablesSettingsAnchor = new Anchor(AbstractImagePrototype.create(resources.settingsIcon()).getSafeHtml());
        selectionTablesSettingsAnchor.setTitle(stringMessages.settings());
        selectionTablesSettingsAnchor.addClickHandler(new ClickHandler() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<RefreshingSelectionTablesSettings>((Component<RefreshingSelectionTablesSettings>) queryDefinitionProvider.getSelectionProvider(), stringMessages).show();
            }
        });
        panel.add(selectionTablesSettingsAnchor);
        
        return panel;
    }

}
