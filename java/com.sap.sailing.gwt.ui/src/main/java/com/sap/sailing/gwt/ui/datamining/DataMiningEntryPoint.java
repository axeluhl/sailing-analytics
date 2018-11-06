package com.sap.sailing.gwt.ui.datamining;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.presentation.TabbedSailingResultsPresenter;
import com.sap.sailing.gwt.ui.shared.settings.SailingSettingsConstants;
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
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.gwt.client.Storage;
import com.sap.sse.gwt.client.shared.components.ComponentResources;
import com.sap.sse.gwt.resources.Highcharts;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.authentication.decorator.AuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.decorator.WidgetFactory;
import com.sap.sse.security.ui.authentication.generic.GenericAuthentication;
import com.sap.sse.security.ui.authentication.generic.GenericAuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.generic.sapheader.SAPHeaderWithAuthentication;

public class DataMiningEntryPoint extends AbstractSailingEntryPoint {
    private static final Logger LOG = Logger.getLogger(DataMiningEntryPoint.class.getName());
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
        runWithServerInfo(serverInfo -> createDataminingPanel(serverInfo, Window.Location.getParameter("q")));
    }

    private void createDataminingPanel(ServerInfoDTO serverInfo, String queryIdentifier) {
        removeUrlParameter();
        SAPHeaderWithAuthentication header = new SAPSailingHeaderWithAuthentication(getStringMessages().dataMining());
        GenericAuthentication genericSailingAuthentication = new FixedSailingAuthentication(getUserService(),
                header.getAuthenticationMenuView());
        AuthorizedContentDecorator authorizedContentDecorator = new GenericAuthorizedContentDecorator(
                genericSailingAuthentication);
        authorizedContentDecorator.setPermissionToCheck(
                SecuredDomainType.DATA_MINING.getPermissionForObjects(DefaultActions.READ, serverInfo.getServerName()));
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
                if (getUserService().hasAllPermissions(SecuredDomainType.DATA_MINING
                        .getPermissionsForObjects(DefaultActions.READ_AND_WRITE_ACTIONS, serverInfo.getServerName()))) {
                    StoredDataMiningQueryDataProvider dataProvider = new StoredDataMiningQueryDataProvider(
                            queryDefinitionProvider, dataMiningService, queryRunner);
                    queryDefinitionProvider.addControl(new StoredDataMiningQueryPanel(dataProvider));
                }
                /*
                 * Running queries automatically when they've been changed is currently unnecessary, if not even
                 * counterproductive. This removes the query runner settings to prevent that the user can enable the
                 * automatic execution of queries. Re-enable this, when this functionality is desired again.
                 */
                // settingsControl.addSettingsComponent(queryRunner);

                SplitLayoutPanel splitPanel = new SplitLayoutPanel(10);
                splitPanel.addSouth(resultsPresenter.getEntryWidget(), 350);
                splitPanel.add(queryDefinitionProvider.getEntryWidget());

                if (queryIdentifier != null) {
                    if (Storage.isLocalStorageSupported()) {
                        Storage store = Storage.getLocalStorageIfSupported();
                        String storedElem = store.getItem(SailingSettingsConstants.DATAMINING_QUERY);
                        JSONArray arr = JSONParser.parseStrict(storedElem).isArray();
                        for (int i = 0; i < arr.size(); i++) {
                            JSONObject json = arr.get(i).isObject();
                            if (queryIdentifier.equals(json.get("uuid").isString().stringValue())) {
                                String serializedQuery = json.get("payload").isString().stringValue();
                                dataMiningService.getDeserializedQuery(serializedQuery,
                                        new AsyncCallback<StatisticQueryDefinitionDTO>() {
                                            @Override
                                            public void onSuccess(StatisticQueryDefinitionDTO result) {
                                                queryDefinitionProvider.applyQueryDefinition(result);
                                                queryRunner.run(result);
                                            }

                                            @Override
                                            public void onFailure(Throwable caught) {
                                                LOG.log(Level.SEVERE, caught.getMessage(), caught);
                                            }
                                        });
                                break;
                            }
                        }
                    } else {
                        Notification.notify(StringMessages.INSTANCE.warningBrowserUnsupported(),
                                NotificationType.ERROR);
                    }
                }
                return splitPanel;
            }
        });

        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel panel = new DockLayoutPanel(Unit.PX);
        panel.addNorth(header, 75);
        panel.add(authorizedContentDecorator);
        rootPanel.add(panel);
    }

    private void removeUrlParameter() {
        String newUrl = Window.Location.createUrlBuilder().setHost(Window.Location.getHost())
                .setPort(Integer.parseInt(Window.Location.getPort())).setPath(Window.Location.getPath())
                .setProtocol(Window.Location.getProtocol()).buildString();
        newUrl = newUrl.replaceAll("\\?.*$", "");
        updateUrl(Window.getTitle(), newUrl);
    }

    private native void updateUrl(String title, String newUrl)/*-{
        $wnd.history.replaceState(null, title, newUrl);
    }-*/;

}
