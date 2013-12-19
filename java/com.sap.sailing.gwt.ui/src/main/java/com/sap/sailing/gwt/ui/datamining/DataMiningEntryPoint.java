package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.GlobalNavigationPanel;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.datamining.execution.SimpleQueryRunner;
import com.sap.sailing.gwt.ui.datamining.presentation.ResultsChart;
import com.sap.sailing.gwt.ui.datamining.selection.QueryDefinitionProviderWithControls;
import com.sap.sailing.gwt.ui.datamining.settings.QueryRunnerSettings;
import com.sap.sailing.gwt.ui.datamining.settings.RefreshingSelectionTablesSettings;

public class DataMiningEntryPoint extends AbstractEntryPoint {

    private static DataMiningResources resources = GWT.create(DataMiningResources.class);

//    private static final String PARAM_BENCHMARK = "benchmark";

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();

//        String benchmarkParameter = Window.Location.getParameter(PARAM_BENCHMARK);
//        boolean showBenchmark = benchmarkParameter != null && benchmarkParameter.equals("true");

        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        SplitLayoutPanel splitPanel = new SplitLayoutPanel();
        rootPanel.add(splitPanel);

        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.PX);
        dockPanel.addNorth(createLogoAndTitlePanel(), 68);
        QueryDefinitionProviderWithControls queryDefinitionProviderWithControls = new QueryDefinitionProviderWithControls(stringMessages, sailingService, this);
        QueryDefinitionProvider queryDefinitionProvider = queryDefinitionProviderWithControls;
        queryDefinitionProvider.getWidget().addStyleName("dataMiningPanel");
        dockPanel.add(queryDefinitionProvider.getWidget());
        
        ResultsPresenter<Number> resultsPresenter = new ResultsChart(stringMessages);
        splitPanel.addSouth(resultsPresenter.getWidget(), 400);
        
        splitPanel.add(dockPanel);
        
        QueryRunner queryRunner = new SimpleQueryRunner(stringMessages, sailingService, this, queryDefinitionProvider, resultsPresenter);
        queryDefinitionProviderWithControls.addControl(queryRunner.getEntryWidget());
        queryDefinitionProviderWithControls.addControl(createSettingsControlWidget(queryRunner, queryDefinitionProvider));
        queryRunner.run(queryDefinitionProvider.getQueryDefinition());
    }

    private LogoAndTitlePanel createLogoAndTitlePanel() {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages.dataMining(), stringMessages, this);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        FlowPanel globalNavigationPanel = new GlobalNavigationPanel(stringMessages, true, null, null);
        logoAndTitlePanel.add(globalNavigationPanel);
        return logoAndTitlePanel;
    }

    private Widget createSettingsControlWidget(final QueryRunner queryRunner, final QueryDefinitionProvider queryDefinitionProvider) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(5);
        
        Label runnerSettingsLabel = new Label(queryRunner.getLocalizedShortName() + ":");
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
        
        Label selectionTablesSettingsLabel = new Label(queryDefinitionProvider.getSelectionProvider().getLocalizedShortName() + ":");
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