package com.sap.sailing.gwt.ui.datamining;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.datamining.execution.SimpleQueryRunner;
import com.sap.sailing.gwt.ui.datamining.presentation.TabbedResultsPresenter;
import com.sap.sailing.gwt.ui.datamining.selection.BufferingQueryDefinitionProviderWithControls;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.impl.UUIDDataMiningSession;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.shared.components.ComponentResources;
import com.sap.sse.gwt.resources.Highcharts;
import com.sap.sse.security.ui.authentication.decorator.AuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.decorator.WidgetFactory;
import com.sap.sse.security.ui.authentication.generic.GenericAuthentication;
import com.sap.sse.security.ui.authentication.generic.GenericAuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.generic.sapheader.SAPHeaderWithAuthentication;

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
        createDataminingPanel();
    }
    
    private void createDataminingPanel() {
        SAPHeaderWithAuthentication header  = new SAPHeaderWithAuthentication(getStringMessages().sapSailingAnalytics(), getStringMessages().dataMining());
        GenericAuthentication genericSailingAuthentication = new FixedSailingAuthentication(getUserService(), header.getAuthenticationMenuView());
        AuthorizedContentDecorator authorizedContentDecorator = new GenericAuthorizedContentDecorator(genericSailingAuthentication);
        authorizedContentDecorator.setContentWidgetFactory(new WidgetFactory() {
            @Override
            public Widget get() {
                DataMiningSettingsControl settingsControl = new AnchorDataMiningSettingsControl(null, null,
                        getStringMessages());
                ResultsPresenter<?> resultsPresenter = new TabbedResultsPresenter(null, null, getStringMessages());
                
                DockLayoutPanel selectionDockPanel = new DockLayoutPanel(Unit.PX);
                BufferingQueryDefinitionProviderWithControls queryDefinitionProviderWithControls =
                        new BufferingQueryDefinitionProviderWithControls(null, null, session, getStringMessages(),
                                dataMiningService, DataMiningEntryPoint.this, settingsControl, resultsPresenter);
                queryDefinitionProviderWithControls.getEntryWidget().addStyleName("dataMiningPanel");
                selectionDockPanel.add(queryDefinitionProviderWithControls.getEntryWidget());
                
                QueryRunner queryRunner = new SimpleQueryRunner(null, null, session, getStringMessages(),
                        dataMiningService,
                        DataMiningEntryPoint.this, queryDefinitionProviderWithControls, resultsPresenter);
                queryDefinitionProviderWithControls.addControl(queryRunner.getEntryWidget());
                settingsControl.addSettingsComponent(queryRunner);
                
                SplitLayoutPanel splitPanel = new SplitLayoutPanel(15);
                splitPanel.addSouth(resultsPresenter.getEntryWidget(), 350);
                splitPanel.add(selectionDockPanel);
                return splitPanel;
            }
        });
        
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel panel = new DockLayoutPanel(Unit.PX);
        panel.addNorth(header, 75);
        panel.add(authorizedContentDecorator);
        rootPanel.add(panel);
    }

}