package com.sap.sailing.gwt.ui.datamining;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.GlobalNavigationPanel;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.datamining.execution.SimpleQueryRunner;
import com.sap.sailing.gwt.ui.datamining.presentation.TabbedResultsPresenter;
import com.sap.sailing.gwt.ui.datamining.selection.BufferingQueryDefinitionProviderWithControls;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.impl.UUIDDataMiningSession;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.shared.components.ComponentResources;
import com.sap.sse.gwt.resources.Highcharts;

public class DataMiningEntryPoint extends AbstractSailingEntryPoint {

    public static final ComponentResources resources = GWT.create(ComponentResources.class);
    
    private final DataMiningServiceAsync dataMiningService = GWT.create(DataMiningService.class);
    
    private DataMiningSession session;

    @Override
    protected void doOnModuleLoad() {
        Highcharts.ensureInjectedWithMore();
        super.doOnModuleLoad();
        session = new UUIDDataMiningSession(UUID.randomUUID());
        
        EntryPointHelper.registerASyncService((ServiceDefTarget) dataMiningService, RemoteServiceMappingConstants.dataMiningServiceRemotePath);

        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        SplitLayoutPanel splitPanel = new SplitLayoutPanel(15);
        rootPanel.add(splitPanel);
        
        DataMiningSettingsControl settingsControl = new AnchorDataMiningSettingsControl(getStringMessages());
        final ResultsPresenter<?> resultsPresenter = new TabbedResultsPresenter(getStringMessages());

        DockLayoutPanel selectionDockPanel = new DockLayoutPanel(Unit.PX);
        selectionDockPanel.addNorth(createLogoAndTitlePanel(), 68);
        BufferingQueryDefinitionProviderWithControls queryDefinitionProviderWithControls =
                new BufferingQueryDefinitionProviderWithControls(session, getStringMessages(), dataMiningService, this, settingsControl, resultsPresenter);
        queryDefinitionProviderWithControls.getEntryWidget().addStyleName("dataMiningPanel");
        selectionDockPanel.add(queryDefinitionProviderWithControls.getEntryWidget());

        splitPanel.addSouth(resultsPresenter.getEntryWidget(), 350);
        splitPanel.add(selectionDockPanel);
        
        final QueryRunner queryRunner = new SimpleQueryRunner(session, getStringMessages(), dataMiningService, this, queryDefinitionProviderWithControls, resultsPresenter);
        queryDefinitionProviderWithControls.addControl(queryRunner.getEntryWidget());
        settingsControl.addSettingsComponent(queryRunner);
    }

    private LogoAndTitlePanel createLogoAndTitlePanel() {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(getStringMessages().dataMining(), getStringMessages(), this, getUserService());
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        FlowPanel globalNavigationPanel = new GlobalNavigationPanel(getStringMessages(), true, null, null, /* event */ null, null);
        logoAndTitlePanel.add(globalNavigationPanel);
        return logoAndTitlePanel;
    }

}