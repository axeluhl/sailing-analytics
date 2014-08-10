package com.sap.sailing.gwt.ui.datamining.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.GlobalNavigationPanel;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.client.EntryPointHelper;

public class DataMiningEntryPoint extends AbstractEntryPoint {
    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);

//    private static DataMiningResources resources = GWT.create(DataMiningResources.class);
    
    private final DataMiningServiceAsync dataMiningService = GWT.create(DataMiningService.class);

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();

        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.sailingServiceRemotePath);
        EntryPointHelper.registerASyncService((ServiceDefTarget) dataMiningService, RemoteServiceMappingConstants.dataMiningServiceRemotePath);

        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        SplitLayoutPanel splitPanel = new SplitLayoutPanel();
        rootPanel.add(splitPanel);

        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.PX);
        dockPanel.addNorth(createLogoAndTitlePanel(), 68);
        
        HTML siteInConstructionLabel = new HTML("This site is currently under construction.");
        siteInConstructionLabel.setStyleName("chart-importantMessage");
        dockPanel.add(siteInConstructionLabel);
        
//        QueryDefinitionProviderWithControls queryDefinitionProviderWithControls = new QueryDefinitionProviderWithControls(stringMessages, sailingService, this);
//        QueryDefinitionProvider queryDefinitionProvider = queryDefinitionProviderWithControls;
//        queryDefinitionProvider.getEntryWidget().addStyleName("dataMiningPanel");
//        dockPanel.add(queryDefinitionProvider.getEntryWidget());
//        
//        ResultsPresenter<Number> resultsPresenter = new ResultsChart(stringMessages);
//        splitPanel.addSouth(resultsPresenter.getWidget(), 400);
//        
//        splitPanel.add(dockPanel);
//        
//        QueryRunner queryRunner = new SimpleQueryRunner(stringMessages, dataMiningService, this, queryDefinitionProvider, resultsPresenter);
//        queryDefinitionProviderWithControls.addControl(queryRunner.getEntryWidget());
//        queryDefinitionProviderWithControls.addControl(createSettingsControlWidget(queryRunner, queryDefinitionProvider));
//        
//        SimpleQueryDefinition queryDefinition = new SimpleQueryDefinition(LocaleInfo.getCurrentLocale().getLocaleName(), GrouperType.Dimensions, StatisticType.Speed, AggregatorType.Average, DataTypes.GPSFix);
//        queryDefinition.appendDimensionToGroupBy(DimensionIdentifier.RegattaName);
//        queryDefinitionProvider.applyQueryDefinition(queryDefinition);
//        queryRunner.run(queryDefinitionProvider.getQueryDefinition());
    }

    private LogoAndTitlePanel createLogoAndTitlePanel() {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages.dataMining(), stringMessages, this);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        FlowPanel globalNavigationPanel = new GlobalNavigationPanel(stringMessages, true, null, null, /* event */ null);
        logoAndTitlePanel.add(globalNavigationPanel);
        return logoAndTitlePanel;
    }

//    private Widget createSettingsControlWidget(final QueryRunner queryRunner, final QueryDefinitionProvider queryDefinitionProvider) {
//        HorizontalPanel panel = new HorizontalPanel();
//        panel.setSpacing(5);
//        
//        Label runnerSettingsLabel = new Label(queryRunner.getLocalizedShortName() + ":");
//        panel.add(runnerSettingsLabel);
//        Anchor runnerSettingsAnchor = new Anchor(AbstractImagePrototype.create(resources.settingsIcon()).getSafeHtml());
//        runnerSettingsAnchor.setTitle(stringMessages.settings());
//        runnerSettingsAnchor.addClickHandler(new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                new SettingsDialog<QueryRunnerSettings>(queryRunner, stringMessages).show();
//            }
//        });
//        panel.add(runnerSettingsAnchor);
//        
//        Label selectionTablesSettingsLabel = new Label(queryDefinitionProvider.getSelectionProvider().getLocalizedShortName() + ":");
//        panel.add(selectionTablesSettingsLabel);
//        Anchor selectionTablesSettingsAnchor = new Anchor(AbstractImagePrototype.create(resources.settingsIcon()).getSafeHtml());
//        selectionTablesSettingsAnchor.setTitle(stringMessages.settings());
//        selectionTablesSettingsAnchor.addClickHandler(new ClickHandler() {
//            @SuppressWarnings("unchecked")
//            @Override
//            public void onClick(ClickEvent event) {
//                new SettingsDialog<RefreshingSelectionTablesSettings>((Component<RefreshingSelectionTablesSettings>) queryDefinitionProvider.getSelectionProvider(), stringMessages).show();
//            }
//        });
//        panel.add(selectionTablesSettingsAnchor);
//        
//        return panel;
//    }

}