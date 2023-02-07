package com.sap.sailing.gwt.ui.datamining;

import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER;
import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.common.client.help.HelpButton;
import com.sap.sailing.gwt.ui.client.AbstractSailingReadEntryPoint;
import com.sap.sailing.gwt.ui.datamining.presentation.TabbedSailingResultsPresenter;
import com.sap.sailing.gwt.ui.shared.settings.SailingSettingsConstants;
import com.sap.sailing.landscape.common.RemoteServiceMappingConstants;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.UUIDDataMiningSession;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.AnchorDataMiningSettingsControl;
import com.sap.sse.datamining.ui.client.CompositeResultsPresenter;
import com.sap.sse.datamining.ui.client.DataMiningService;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.DataMiningSettingsControl;
import com.sap.sse.datamining.ui.client.DataMiningSettingsInfoManager;
import com.sap.sse.datamining.ui.client.DataMiningWriteService;
import com.sap.sse.datamining.ui.client.DataMiningWriteServiceAsync;
import com.sap.sse.datamining.ui.client.QueryDefinitionProvider;
import com.sap.sse.datamining.ui.client.execution.SimpleQueryRunner;
import com.sap.sse.datamining.ui.client.selection.QueryDefinitionProviderWithControls;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.gwt.client.shared.components.ComponentResources;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorage;
import com.sap.sse.gwt.resources.Highcharts;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;
import com.sap.sse.security.ui.authentication.decorator.AuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.decorator.WidgetFactory;
import com.sap.sse.security.ui.authentication.generic.GenericAuthentication;
import com.sap.sse.security.ui.authentication.generic.GenericAuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.generic.sapheader.SAPHeaderWithAuthentication;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

/**
 * UI Entry point for the "Data Mining" module, requiring the user to have the {@link ServerActions#DATA_MINING}
 * permission for the current server in order to see and use it.
 * <p>
 * 
 * The main panel is divided into two key parts: one to view and edit {@link StatisticQueryDefinitionDTO query},
 * implemented by the {@link QueryDefinitionProviderWithControls} class; and another to present the
 * {@link QueryResultDTO query results}, implemented by a {@link TabbedSailingResultsPresenter} class. The result
 * presenter starts out with a single empty tab. The tabs in the result presenter each have a unique
 * {@link TabbedSailingResultsPresenter#getPresenterIds() ID string}.
 * <p>
 * 
 * Three further controls are added to the {@link QueryDefinitionProviderWithControls}: a {@link SimpleQueryRunner query
 * runner} that asks the {@link QueryDefinitionProviderWithControls query definition provider} to produce a new query
 * from the current query editor state and then triggers the server-side execution of the query produced; furthermore
 * load/store/remove controls for {@link StoredDataMiningQueryDataProvider queries} and
 * {@link StoredDataMiningReportsProvider reports}.
 * <p>
 * 
 * Running a query successfully will present the results in the current tab of the result presenter which will link the
 * query and its results to the result presenter's tab ID (see
 * {@link CompositeResultsPresenter#getQueryDefinition(String)} and
 * {@link CompositeResultsPresenter#getResult(String)}).
 * <p>
 * 
 * The result presenter allows the user to create more tabs. A new tab starts out empty and has neither a query nor a
 * result associated with it. The {@link QueryDefinitionProvider query editor}'s contents will remain unchanged by
 * creating a new tab. As the user switches to a result presenter tab that has a query/result combination associated
 * with it, the query will be {@link QueryDefinitionProvider#applyQueryDefinition(StatisticQueryDefinitionDTO)
 * "applied"} to the query editor so that the editor shows the query again that led to the results shown in the now
 * selected tab. If the user has changed anything in the editor before switching tabs the query editor---depending on
 * user-configurable settings---may ask how the user would like to deal with these changes: forget them and replace the
 * query editor's contents with the query associated with the now selected tab; or keep those changes and thus keep
 * showing the modifications which now do not necessarily match the results from the now selected tab anymore.
 * <p>
 * 
 * When {@link StoredDataMiningQueryDataProvider#applyQuery(String) "loading"} a query using the
 * {@link StoredDataMiningQueryDataProvider}, the query selected by the user is "applied" to the query editor, again
 * using the same change handling/warning as when switching tabs. The query is not run automatically, and no tab switch
 * in the result presenter is performed. It is up to the user to run the query and to decide in which tab the results
 * will be displayed (always the one currently selected).
 * <p>
 * 
 * <b>Reports:</b> The combination of all queries stored at any point in time in the {@link CompositeResultsPresenter}
 * are considered forming a <em>report</em>. In addition to just being a collection of queries, a
 * {@link DataMiningReportDTO report} can additionally have {@link DataMiningReportDTO#getParameters() parameters}. A
 * parameter captures the values selected in one or more dimension filters that filter the objects traversed along a
 * retriever chain. Using parameters allows a user to simultaneously change filter criteria in several queries that have
 * dimension filters bound to the same parameter, such as the regatta to filter for if a report combines a set of useful
 * queries for a single regatta.
 * <p>
 * 
 * Like single queries, reports can also be stored, loaded and removed. Loading a report discards all result presenter
 * tabs and their queries and results and replaces them by a new tab for each query contained in the report. All of the
 * queries of the report loaded are run, and the results are filled into a result presenter tab each, leading again to
 * the linking of query and result to the tab.
 * <p>
 * 
 * <b>Report Parameter Handling:</b> When using the query editor, each dimension filter activated for a query can
 * optionally be <em>bound</em> to a {@link FilterDimensionParameter report parameter}. Parameters are named, have a
 * stable ID are specific to an object type (identified by the type's name). When choosing to bind a dimension filter to
 * a parameter, the user may create a new parameter or can choose an existing one whose type matches that of the
 * dimension to which the filter applies. When choosing an existing one, the parameter's
 * {@link FilterDimensionParameter#getValues() values} are intersected with the values that are available at the stage
 * of the dimension filter to which the parameter is being bound, and the intersection becomes the new filter's
 * selection. If the user has chosen to create a new parameter instead, the current filter's selection defines the
 * initial values for the new parameter. If not taken yet, the dimension's name will be used as as default for the new
 * parameter's name.
 * <p>
 * 
 * When changing the selection of a dimension filter that is bound to a parameter, the parameter's values are updated
 * accordingly. TODO bug4789: This does not count as a change in the sense that switching tabs another time would need
 * to ask whether or not to preserve those changes because the change is already stored in the current report's
 * parameter model. All queries also using the parameter are then remembered as "to also run" when the query through
 * which the parameter was edited will be run. The query currently shown in the editor will run first, followed in the
 * background by those dependent "to also run" queries whose results then update their respective result presenter tab
 * in the background such that when the user switches to those tabs then the query will either still be running or the
 * new results will be shown already.
 * <p>
 * 
 * When switching to a result presenter tab that has remembered a query that is then
 * {@link QueryDefinitionProvider#applyQueryDefinition(StatisticQueryDefinitionDTO) filled into the query editor}, the
 * selections in all dimension filters bound to a parameter are updated to reflect their parameter's current
 * {@link FilterDimensionParameter#getValues() value}. This update again is not considered a change in the sense that
 * switching tabs another time would need to ask whether or not to preserve those changes.<p>
 * 
 * Storing a report stores the parameters with their values and bindings to dimension filters together with the report.<p>
 * 
 * The parameter names within a report must be unique and are the key by which a user selects them. The UUID should
 * never appear in the user interface.<p>
 */
public class DataMiningEntryPoint extends AbstractSailingReadEntryPoint {

    public static final ComponentResources resources = GWT.create(ComponentResources.class);
    private static final Logger LOG = Logger.getLogger(DataMiningEntryPoint.class.getName());

    private static final DataMiningResources dataMiningResources = GWT.create(DataMiningResources.class);
    
    private final DataMiningServiceAsync dataMiningService = GWT.create(DataMiningService.class);
    private final DataMiningWriteServiceAsync dataMiningWriteService = GWT.create(DataMiningWriteService.class);
    private DataMiningSession session;

    private QueryDefinitionProviderWithControls queryDefinitionProvider;
    private CompositeResultsPresenter<?> resultsPresenter;
    private Integer resultsPresenterSouthHeight = 350;
    private Integer resultsPresenterEastWidth = 750;

    private Panel mainPanel;
    private SplitLayoutPanel queryAndResultSplitPanel;
    private boolean queryAndResultAreVertical = true;

    @Override
    protected void doOnModuleLoad() {
        Highcharts.ensureInjectedWithMore();
        super.doOnModuleLoad();
        session = new UUIDDataMiningSession(UUID.randomUUID());
        EntryPointHelper.registerASyncService((ServiceDefTarget) dataMiningService,
                RemoteServiceMappingConstants.dataMiningServiceRemotePath, HEADER_FORWARD_TO_REPLICA);
        EntryPointHelper.registerASyncService((ServiceDefTarget) dataMiningWriteService,
                RemoteServiceMappingConstants.dataMiningServiceRemotePath, HEADER_FORWARD_TO_MASTER);
        getUserService().executeWithServerInfo(s -> createDataminingPanel(s, Window.Location.getParameter("q")));
    }

    private void createDataminingPanel(ServerInfoDTO serverInfo, final String queryIdentifier) {
        removeUrlParameter();
        final SAPHeaderWithAuthentication header = new SAPSailingHeaderWithAuthentication(getStringMessages().dataMining());
        final PaywallResolver paywallResolver = new PaywallResolver(getUserService(), getSubscriptionServiceFactory());
        final GenericAuthentication genericSailingAuthentication = new FixedSailingAuthentication(getUserService(), paywallResolver,
                header.getAuthenticationMenuView());
        final AuthorizedContentDecorator authorizedContentDecorator = new GenericAuthorizedContentDecorator(
                genericSailingAuthentication);
        authorizedContentDecorator.setPermissionToCheck(serverInfo, ServerActions.DATA_MINING);
        authorizedContentDecorator.setContentWidgetFactory(new WidgetFactory() {
            private SimpleQueryRunner queryRunner;
            private final DataMiningSettingsInfoManager settingsManager = new DataMiningSettingsInfoManagerImpl(
                    getStringMessages());

            @Override
            public Widget get() {
                mainPanel = new LayoutPanel();
                DataMiningSettingsControl settingsControl = new AnchorDataMiningSettingsControl(null, null);
                resultsPresenter = new TabbedSailingResultsPresenter(/* parent */ null, /* context */ null,
                        /* drillDownCallback */ groupKey -> {
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
                if (getUserService().hasServerPermission(ServerActions.DATA_MINING)) {
                    StoredDataMiningQueryDataProvider queryProvider = new StoredDataMiningQueryDataProvider(
                            queryDefinitionProvider, dataMiningService, dataMiningWriteService, getStringMessages());
                    queryDefinitionProvider.addControl(new StoredDataMiningQueryPanel(queryProvider));
                    StoredDataMiningReportsProvider reportsProvider = new StoredDataMiningReportsProvider(
                            dataMiningService, dataMiningWriteService);
                    queryDefinitionProvider.addControl(new DataMiningReportStoreControls(DataMiningEntryPoint.this,
                            session, dataMiningService, reportsProvider, mainPanel, queryDefinitionProvider.getRetrieverChainProvider(), resultsPresenter));
                }
                final Anchor orientationAnchor = new Anchor(
                        AbstractImagePrototype.create(dataMiningResources.orientationIcon()).getSafeHtml());
                orientationAnchor.addStyleName("orientationAnchor");
                orientationAnchor.setTitle(getStringMessages().changeOrientation());
                orientationAnchor.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        setQueryAndResultOrientation();
                    }
                });
                queryDefinitionProvider.addControl(orientationAnchor);
                queryDefinitionProvider.addControl(new HelpButton(DataMiningHelpButtonResources.INSTANCE,
                        getStringMessages().videoGuide(), "https://support.sapsailing.com/hc/en-us/articles/360019913740-An-Introduction-to-the-SAP-Sailing-Analytics-Data-Mining-Tool"));
                /*
                 * Running queries automatically when they've been changed is currently unnecessary, if not even
                 * counterproductive. This removes the query runner settings to prevent that the user can enable the
                 * automatic execution of queries. Re-enable this, when this functionality is desired again.
                 */
                // settingsControl.addSettingsComponent(queryRunner);
                queryAndResultSplitPanel = new SplitLayoutPanel(10);
                mainPanel.add(queryAndResultSplitPanel);
                addDefinitionProviderAndResultPresenter();
                if (queryIdentifier != null) {
                    CrossDomainStorage store = getUserService().getStorage();
                    store.getItem(SailingSettingsConstants.DATAMINING_QUERY, storedElem -> {
                        JSONArray arr = JSONParser.parseStrict(storedElem).isArray();
                        for (int i = 0; i < arr.size(); i++) {
                            JSONObject json = arr.get(i).isObject();
                            if (queryIdentifier.equals(json.get("uuid").isString().stringValue())) {
                                String serializedQuery = json.get("payload").isString().stringValue();
                                dataMiningService.getDeserializedQuery(serializedQuery,
                                        new AsyncCallback<ModifiableStatisticQueryDefinitionDTO>() {
                                            @Override
                                            public void onSuccess(ModifiableStatisticQueryDefinitionDTO result) {
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
                    });
                }
                return mainPanel;
            }
        });
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel panel = new DockLayoutPanel(Unit.PX);
        panel.addNorth(header, 75);
        panel.add(authorizedContentDecorator);
        panel.ensureDebugId("DataMiningPanel");
        rootPanel.add(panel);
    }

    /**
     * Toggles the position of the {@link #resultsPresenter} from south to east and vice versa.
     */
    public void setQueryAndResultOrientation() {
        setQueryAndResultOrientation(!queryAndResultAreVertical);
    }

    /**
     * Sets the position of the {@link #resultsPresenter}.
     * 
     * @param vertical
     *            {@code boolean} {@code true} places it in the south position and {@code false} in the east position.
     */
    public void setQueryAndResultOrientation(boolean vertical) {
        if (vertical != queryAndResultAreVertical) {
            // Store current size for the next orientation change
            Double size = queryAndResultSplitPanel.getWidgetSize(resultsPresenter.getEntryWidget());
            if (size != null) {
                if (queryAndResultAreVertical) {
                    resultsPresenterSouthHeight = size.intValue();
                } else {
                    resultsPresenterEastWidth = size.intValue();
                }
            }

            queryAndResultAreVertical = vertical;

            queryAndResultSplitPanel.remove(resultsPresenter.getEntryWidget());
            // You can't add panels as long as there is a center panel so temporarily remove it
            queryAndResultSplitPanel.remove(queryDefinitionProvider.getEntryWidget());

            // Add the center panel back once the changes are made
            addDefinitionProviderAndResultPresenter();
        }
    }

    private void addDefinitionProviderAndResultPresenter() {
        if (queryAndResultAreVertical) {
            queryAndResultSplitPanel.addSouth(resultsPresenter.getEntryWidget(), resultsPresenterSouthHeight);
        } else {
            queryAndResultSplitPanel.addEast(resultsPresenter.getEntryWidget(), resultsPresenterEastWidth);
        }
        queryAndResultSplitPanel.add(queryDefinitionProvider.getEntryWidget());
    }

    private void removeUrlParameter() {
        try {
            UrlBuilder builder = Window.Location.createUrlBuilder().setHost(Window.Location.getHost())
                    .setPath(Window.Location.getPath()).setProtocol(Window.Location.getProtocol());

            String port = Window.Location.getPort();
            if (port != null && !"".equals(port.trim()) && !"0".equals(port)) {
                builder.setPort(Integer.parseInt(port));
            }
            String newUrl = builder.buildString();
            newUrl = newUrl.replaceAll("\\?.*$", "");

            updateUrl(Window.getTitle(), newUrl);
        } catch (Exception e) {
            LOG.severe("Could not update URL: " + e.getMessage());
            // In the worst case, the URL is not updated. This should not impact the user experience of
            // data mining.
        }
    }

    /*
     * using JSNI because GWT-History-Mapper does currently not support updating an URL in the history without reloading
     * the page
     */
    private native void updateUrl(String title, String newUrl)/*-{
        $wnd.history.replaceState(null, title, newUrl);
    }-*/;

}
