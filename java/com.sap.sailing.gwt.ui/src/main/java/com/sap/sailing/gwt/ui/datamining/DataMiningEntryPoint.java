package com.sap.sailing.gwt.ui.datamining;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.security.SailingPermissionsForRoleProvider;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.datamining.presentation.TabbedSailingResultsPresenter;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.UUIDDataMiningSession;
import com.sap.sse.datamining.ui.client.AnchorDataMiningSettingsControl;
import com.sap.sse.datamining.ui.client.CompositeResultsPresenter;
import com.sap.sse.datamining.ui.client.DataMiningService;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.DataMiningSettingsControl;
import com.sap.sse.datamining.ui.client.DataMiningSettingsInfoManager;
import com.sap.sse.datamining.ui.client.execution.SimpleQueryRunner;
import com.sap.sse.datamining.ui.client.selection.QueryDefinitionProviderWithControls;
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
        EntryPointHelper.registerASyncService((ServiceDefTarget) dataMiningService,
                RemoteServiceMappingConstants.dataMiningServiceRemotePath);
        createDataminingPanel();
    }

    private void createDataminingPanel() {
        SAPHeaderWithAuthentication header = new SAPSailingHeaderWithAuthentication(getStringMessages().dataMining());
        GenericAuthentication genericSailingAuthentication = new FixedSailingAuthentication(getUserService(),
                header.getAuthenticationMenuView());
        AuthorizedContentDecorator authorizedContentDecorator = new GenericAuthorizedContentDecorator(
                genericSailingAuthentication);
        authorizedContentDecorator.setPermissionToCheck(Permission.DATA_MINING,
                SailingPermissionsForRoleProvider.INSTANCE);
        authorizedContentDecorator.setContentWidgetFactory(new WidgetFactory() {
            
            private QueryDefinitionProviderWithControls queryDefinitionProvider;
            private SimpleQueryRunner queryRunner;
            private final DataMiningSettingsInfoManager settingsManager = new DataMiningSettingsInfoManagerImpl(
                    getStringMessages());

            @Override
            public Widget get() {
                DataMiningSettingsControl settingsControl = new AnchorDataMiningSettingsControl(null, null);
                CompositeResultsPresenter<?> resultsPresenter = new TabbedSailingResultsPresenter(/* parent */ null,
                        /* context */ null, /* drillDownCallback */ groupKey -> {
                            queryDefinitionProvider.drillDown(groupKey, () -> {
                                queryRunner.run(queryDefinitionProvider.getQueryDefinition());
                            });
                        }, getStringMessages());
                resultsPresenter.addCurrentPresenterChangedListener(presenterId -> {
                    StatisticQueryDefinitionDTO queryDefinition = resultsPresenter.getQueryDefinition(presenterId);
                    if (queryDefinition != null) {
                        queryDefinitionProvider.applyQueryDefinition(queryDefinition);
                    }
                });
                
                queryDefinitionProvider = new QueryDefinitionProviderWithControls(null, null, session,
                        dataMiningService, DataMiningEntryPoint.this, settingsControl, settingsManager,
                        queryDefinition -> queryRunner.run(queryDefinition));

                queryRunner = new SimpleQueryRunner(null, null, session, dataMiningService, DataMiningEntryPoint.this,
                        queryDefinitionProvider, resultsPresenter);
                queryDefinitionProvider.addControl(queryRunner.getEntryWidget());
                StoredDataMiningQueryDataProvider dataProvider = new StoredDataMiningQueryDataProvider(
                        queryDefinitionProvider, dataMiningService);
                queryDefinitionProvider.addControl(new StoredDataMiningQueryPanel(dataProvider));
                /*
                 * Running queries automatically when they've been changed is currently unnecessary, if not even
                 * counterproductive. This removes the query runner settings to prevent that the user can enable the
                 * automatic execution of queries. Re-enable this, when this functionality is desired again.
                 */
                // settingsControl.addSettingsComponent(queryRunner);
                
                SplitLayoutPanel splitPanel = new SplitLayoutPanel(10);
                splitPanel.addSouth(resultsPresenter.getEntryWidget(), 350);
                splitPanel.add(queryDefinitionProvider.getEntryWidget());
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
