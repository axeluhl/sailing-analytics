package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.GlobalNavigationPanel;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.datamining.execution.SimpleQueryRunner;
import com.sap.sailing.gwt.ui.datamining.presentation.BenchmarkResultsPanel;
import com.sap.sailing.gwt.ui.datamining.presentation.ResultsChart;
import com.sap.sailing.gwt.ui.datamining.selection.BufferingQueryDefinitionProviderWithControls;
import com.sap.sailing.gwt.ui.datamining.settings.QueryRunnerSettings;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class DataMiningEntryPoint extends AbstractSailingEntryPoint {
    private static DataMiningResources resources = GWT.create(DataMiningResources.class);
    
    private final DataMiningServiceAsync dataMiningService = GWT.create(DataMiningService.class);

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();

        EntryPointHelper.registerASyncService((ServiceDefTarget) dataMiningService, RemoteServiceMappingConstants.dataMiningServiceRemotePath);

        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        SplitLayoutPanel splitPanel = new SplitLayoutPanel();
        rootPanel.add(splitPanel);

        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.PX);
        dockPanel.addNorth(createLogoAndTitlePanel(), 68);
        BufferingQueryDefinitionProviderWithControls queryDefinitionProviderWithControls = new BufferingQueryDefinitionProviderWithControls(getStringMessages(), sailingService, dataMiningService, this);
        queryDefinitionProviderWithControls.getEntryWidget().addStyleName("dataMiningPanel");
        dockPanel.add(queryDefinitionProviderWithControls.getEntryWidget());
        
        ResultsPresenter<Number> resultsPresenter = new ResultsChart(getStringMessages());
        if (GwtHttpRequestUtils.getBooleanParameter("benchmark", false)) {
            BenchmarkResultsPanel benchmarkResultsPanel = new BenchmarkResultsPanel(getStringMessages(), dataMiningService, this, queryDefinitionProviderWithControls);
            splitPanel.addSouth(benchmarkResultsPanel, 500);
        } else {
            splitPanel.addSouth(resultsPresenter.getWidget(), 400);
        }
        
        splitPanel.add(dockPanel);
        
        QueryRunner queryRunner = new SimpleQueryRunner(getStringMessages(), dataMiningService, this, queryDefinitionProviderWithControls, resultsPresenter);
        queryDefinitionProviderWithControls.addControl(queryRunner.getEntryWidget());
        queryDefinitionProviderWithControls.addControl(createSettingsControlWidget(queryRunner, queryDefinitionProviderWithControls));
    }

    private LogoAndTitlePanel createLogoAndTitlePanel() {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(getStringMessages().dataMining(), getStringMessages(), this, getUserService());
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        FlowPanel globalNavigationPanel = new GlobalNavigationPanel(getStringMessages(), true, null, null, /* event */ null, null);
        logoAndTitlePanel.add(globalNavigationPanel);
        return logoAndTitlePanel;
    }

    private Widget createSettingsControlWidget(final QueryRunner queryRunner, final QueryDefinitionProvider queryDefinitionProvider) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(5);
        
        Label runnerSettingsLabel = new Label(queryRunner.getLocalizedShortName() + ":");
        panel.add(runnerSettingsLabel);
        Anchor runnerSettingsAnchor = new Anchor(AbstractImagePrototype.create(resources.settingsIcon()).getSafeHtml());
        runnerSettingsAnchor.setTitle(getStringMessages().settings());
        runnerSettingsAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<QueryRunnerSettings>(queryRunner, getStringMessages()).show();
            }
        });
        panel.add(runnerSettingsAnchor);
        
        return panel;
    }

}