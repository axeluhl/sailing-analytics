package com.sap.sailing.gwt.ui.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.ServerInfo;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.StatisticQueryDefinition;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.data.ExtensibleQueryResult;
import com.sap.sse.datamining.data.QueryResult;
import com.sap.sse.datamining.factories.DataMiningDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.DataRetrieverLevel;
import com.sap.sse.datamining.impl.components.management.ReducedDimensions;
import com.sap.sse.datamining.impl.data.QueryResultImpl;
import com.sap.sse.datamining.shared.DataMiningQuerySerializer;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.SerializationDummy;
import com.sap.sse.datamining.shared.data.QueryResultState;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.PredefinedQueryIdentifier;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.shared.impl.dto.ReducedDimensionsDTO;
import com.sap.sse.datamining.shared.impl.dto.StoredDataMiningQueryDTOImpl;
import com.sap.sse.datamining.ui.client.DataMiningService;
import com.sap.sse.gwt.server.ProxiedRemoteServiceServlet;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;
import com.sap.sse.util.ServiceTrackerFactory;

public class DataMiningServiceImpl extends ProxiedRemoteServiceServlet implements DataMiningService {
    private static final long serialVersionUID = -7951930891674894528L;

    private final BundleContext context;

    private final ServiceTracker<DataMiningServer, DataMiningServer> dataMiningServerTracker;
    private final ServiceTracker<SecurityService, SecurityService> securityServiceTracker;

    private final StoredDataMiningQueryPersister storedDataMiningQueryPersistor;

    private final DataMiningDTOFactory dtoFactory;

    public DataMiningServiceImpl() {
        context = Activator.getDefault();
        dataMiningServerTracker = createAndOpenDataMiningServerTracker(context);
        securityServiceTracker = ServiceTrackerFactory.createAndOpen(context, SecurityService.class);

        storedDataMiningQueryPersistor = new StoredDataMiningQueryPersisterImpl(securityServiceTracker.getService());
        dtoFactory = new DataMiningDTOFactory();
    }

    private ServiceTracker<DataMiningServer, DataMiningServer> createAndOpenDataMiningServerTracker(
            BundleContext context) {
        ServiceTracker<DataMiningServer, DataMiningServer> result = new ServiceTracker<DataMiningServer, DataMiningServer>(
                context, DataMiningServer.class.getName(), null);
        result.open();
        return result;
    }

    private DataMiningServer getDataMiningServer() {
        return dataMiningServerTracker.getService();
    }

    @Override
    public Date getComponentsChangedTimepoint() {
        checkDataMiningPermission();
        return getDataMiningServer().getComponentsChangedTimepoint();
    }
    
    @Override
    public FunctionDTO getIdentityFunction(String localeInfoName) {
        checkDataMiningPermission();
        DataMiningServer server = getDataMiningServer();
        Locale locale = ResourceBundleStringMessages.Util.getLocaleFor(localeInfoName);
        return dtoFactory.createFunctionDTO(server.getIdentityFunction(), server.getStringMessages(), locale);
    }

    @Override
    public HashSet<FunctionDTO> getAllStatistics(String localeInfoName) {
        checkDataMiningPermission();
        Iterable<Function<?>> statistics = getDataMiningServer().getAllStatistics();
        return functionsAsDTOs(statistics, localeInfoName);
    }

    /**
     * @throws AuthorizationException
     *             if permission does not exist
     */
    private void checkDataMiningPermission() {
        // TODO: should be moved to one place (not the ServerInfoDTO) -> could not be moved to ServerInfo, since
        // sse.security is not available
        SecurityUtils.getSubject().checkPermission(new ServerInfoDTO(ServerInfo.getName(), ServerInfo.getBuildVersion())
                .getIdentifier().getStringPermission(ServerActions.DATA_MINING));
    }

    @Override
    public HashSet<FunctionDTO> getStatisticsFor(DataRetrieverChainDefinitionDTO retrieverChainDefinition,
            String localeInfoName) {
        checkDataMiningPermission();
        Class<?> retrievedDataType = getDataMiningServer()
                .getDataRetrieverChainDefinitionForDTO(retrieverChainDefinition).getRetrievedDataType();
        Iterable<Function<?>> statistics = getDataMiningServer().getStatisticsFor(retrievedDataType);
        return functionsAsDTOs(statistics, localeInfoName);
    }

    @Override
    public HashSet<AggregationProcessorDefinitionDTO> getAggregatorDefinitions(String localeInfoName) {
        checkDataMiningPermission();
        Iterable<AggregationProcessorDefinition<?, ?>> definitions = getDataMiningServer()
                .getAllAggregationProcessorDefinitions();
        return aggregatorDefinitionsAsDTOs(definitions, localeInfoName);
    }

    @Override
    public HashSet<AggregationProcessorDefinitionDTO> getAggregatorDefinitionsFor(FunctionDTO extractionFunction,
            String localeInfoName) {
        checkDataMiningPermission();
        Class<?> returnType = getReturnType(extractionFunction);
        @SuppressWarnings("unchecked")
        Iterable<AggregationProcessorDefinition<?, ?>> definitions = (Iterable<AggregationProcessorDefinition<?, ?>>) (Iterable<?>) getDataMiningServer()
                .getAggregationProcessorDefinitions(returnType);
        return aggregatorDefinitionsAsDTOs(definitions, localeInfoName);
    }

    private Class<?> getReturnType(FunctionDTO extractionFunction) {
        return getDataMiningServer().getFunctionForDTO(extractionFunction).getReturnType();
    }

    private HashSet<AggregationProcessorDefinitionDTO> aggregatorDefinitionsAsDTOs(
            Iterable<AggregationProcessorDefinition<?, ?>> definitions, String localeInfoName) {
        ResourceBundleStringMessages stringMessages = getDataMiningServer().getStringMessages();
        Locale locale = ResourceBundleStringMessages.Util.getLocaleFor(localeInfoName);

        HashSet<AggregationProcessorDefinitionDTO> definitionDTOs = new HashSet<>();
        for (AggregationProcessorDefinition<?, ?> definition : definitions) {
            definitionDTOs.add(dtoFactory.createAggregationProcessorDefinitionDTO(definition, stringMessages, locale));
        }
        return definitionDTOs;
    }

    @Override
    public HashSet<FunctionDTO> getDimensionsFor(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO,
            String localeInfoName) {
        checkDataMiningPermission();
        Class<?> retrievedType = getDataMiningServer()
                .getDataRetrieverChainDefinitionForDTO(dataRetrieverChainDefinitionDTO).getRetrievedDataType();
        Iterable<Function<?>> dimensions = getDataMiningServer().getDimensionsFor(retrievedType);
        return functionsAsDTOs(dimensions, localeInfoName);
    }

    @Override
    public ReducedDimensionsDTO getReducedDimensionsMappedByLevelFor(
            DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, String localeInfoName) {
        checkDataMiningPermission();
        DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition = getDataMiningServer()
                .getDataRetrieverChainDefinitionForDTO(dataRetrieverChainDefinitionDTO);
        ReducedDimensions reducedDimensions = getDataMiningServer()
                .getReducedDimensionsMappedByLevelFor(dataRetrieverChainDefinition);
        return reducedDimensionsAsDTO(reducedDimensions, localeInfoName);
    }

    private ReducedDimensionsDTO reducedDimensionsAsDTO(ReducedDimensions dimensions, String localeInfoName) {
        HashMap<DataRetrieverLevelDTO, HashSet<FunctionDTO>> dimensionDTOs = new HashMap<>();
        ResourceBundleStringMessages stringMessages = getDataMiningServer().getStringMessages();
        Locale locale = ResourceBundleStringMessages.Util.getLocaleFor(localeInfoName);
        for (final Entry<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> dimensionsEntry : dimensions
                .getReducedDimensions().entrySet()) {
            dimensionDTOs.put(dtoFactory.createDataRetrieverLevelDTO(dimensionsEntry.getKey(), stringMessages, locale),
                    functionsAsDTOs(dimensionsEntry.getValue(), localeInfoName));
        }
        HashMap<FunctionDTO, FunctionDTO> fromOriginalToReducedDTO = new HashMap<>();
        for (final Entry<Function<?>, Function<?>> fromOriginalToReduced : dimensions
                .getFromOriginalDimensionToReducedDimension().entrySet()) {
            fromOriginalToReducedDTO.put(
                    dtoFactory.createFunctionDTO(fromOriginalToReduced.getKey(), stringMessages, locale),
                    dtoFactory.createFunctionDTO(fromOriginalToReduced.getValue(), stringMessages, locale));
        }
        return new ReducedDimensionsDTO(dimensionDTOs, fromOriginalToReducedDTO);
    }

    private HashSet<FunctionDTO> functionsAsDTOs(Iterable<Function<?>> functions, String localeInfoName) {
        Locale locale = ResourceBundleStringMessages.Util.getLocaleFor(localeInfoName);
        ResourceBundleStringMessages stringMessages = getDataMiningServer().getStringMessages();

        HashSet<FunctionDTO> functionDTOs = new HashSet<FunctionDTO>();
        for (Function<?> function : functions) {
            functionDTOs.add(dtoFactory.createFunctionDTO(function, stringMessages, locale));
        }
        return functionDTOs;
    }

    @Override
    public ArrayList<DataRetrieverChainDefinitionDTO> getDataRetrieverChainDefinitions(String localeInfoName) {
        checkDataMiningPermission();
        Iterable<DataRetrieverChainDefinition<?, ?>> dataRetrieverChainDefinitions = getDataMiningServer()
                .getDataRetrieverChainDefinitions();
        return dataRetrieverChainDefinitionsAsDTOs(dataRetrieverChainDefinitions, localeInfoName);
    }

    @Override
    public ArrayList<DataRetrieverChainDefinitionDTO> getDataRetrieverChainDefinitionsFor(
            FunctionDTO statisticToCalculate, String localeInfoName) {
        checkDataMiningPermission();
        Class<?> baseDataType = getBaseDataType(statisticToCalculate);
        @SuppressWarnings("unchecked")
        Iterable<DataRetrieverChainDefinition<?, ?>> dataRetrieverChainDefinitions = (Iterable<DataRetrieverChainDefinition<?, ?>>) (Iterable<?>) getDataMiningServer()
                .getDataRetrieverChainDefinitionsByDataType(baseDataType);
        return dataRetrieverChainDefinitionsAsDTOs(dataRetrieverChainDefinitions, localeInfoName);
    }

    private Class<?> getBaseDataType(FunctionDTO statisticToCalculate) {
        Function<?> function = getDataMiningServer().getFunctionForDTO(statisticToCalculate);
        return function.getDeclaringType();
    }

    private ArrayList<DataRetrieverChainDefinitionDTO> dataRetrieverChainDefinitionsAsDTOs(
            Iterable<DataRetrieverChainDefinition<?, ?>> dataRetrieverChainDefinitions, String localeInfoName) {
        ResourceBundleStringMessages serverStringMessages = getDataMiningServer().getStringMessages();
        Locale locale = ResourceBundleStringMessages.Util.getLocaleFor(localeInfoName);

        ArrayList<DataRetrieverChainDefinitionDTO> DTOs = new ArrayList<>();
        for (DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition : dataRetrieverChainDefinitions) {
            DTOs.add(dtoFactory.createDataRetrieverChainDefinitionDTO(dataRetrieverChainDefinition,
                    serverStringMessages, locale));
        }
        return DTOs;
    }

    @Override
    public QueryResultDTO<HashSet<Object>> getDimensionValuesFor(DataMiningSession session,
            DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, DataRetrieverLevelDTO retrieverLevelDTO,
            HashSet<FunctionDTO> dimensionDTOs,
            HashMap<DataRetrieverLevelDTO, SerializableSettings> retrieverSettingsDTO,
            HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelectionDTO,
            String localeInfoName) {
        checkDataMiningPermission();
        DataMiningServer dataMiningServer = getDataMiningServer();
        DataRetrieverChainDefinition<RacingEventService, ?> retrieverChainDefinition = dataMiningServer
                .getDataRetrieverChainDefinitionForDTO(dataRetrieverChainDefinitionDTO);
        DataRetrieverLevel<?, ?> retrieverLevel = retrieverChainDefinition
                .getDataRetrieverLevel(retrieverLevelDTO.getLevel());
        Iterable<Function<?>> dimensions = functionDTOsAsFunctions(dimensionDTOs);
        // split up between boolean/enum dimensions and others; for the bool/enums we'll
        // simply enumerate all values instead of retrieving all objects
        List<Function<?>> nonBoolNonEnumResultDimensions = new ArrayList<>();
        LinkedHashMap<Function<?>, List<?>> boolOrEnumResultDimensionsWithValues = new LinkedHashMap<>();
        for (final Function<?> dimensionFunction : dimensions) {
            if (dimensionFunction.getReturnType() == boolean.class
                    || dimensionFunction.getReturnType() == Boolean.class) {
                boolOrEnumResultDimensionsWithValues.put(dimensionFunction, Arrays.asList(true, false));
            } else if (dimensionFunction.getReturnType().isEnum()) {
                boolOrEnumResultDimensionsWithValues.put(dimensionFunction,
                        Arrays.asList(dimensionFunction.getReturnType().getEnumConstants()));
            } else {
                nonBoolNonEnumResultDimensions.add(dimensionFunction);
            }
        }
        final ExtensibleQueryResult<HashSet<Object>> result;
        final Locale locale = ResourceBundleStringMessages.Util.getLocaleFor(localeInfoName);
        if (!nonBoolNonEnumResultDimensions.isEmpty()) {
            Map<DataRetrieverLevel<?, ?>, SerializableSettings> retrieverSettings = retrieverSettingsDTOAsRetrieverSettings(
                    retrieverSettingsDTO, retrieverChainDefinition);
            Map<DataRetrieverLevel<?, ?>, Map<Function<?>, Collection<?>>> filterSelection = filterSelectionDTOAsFilterSelection(
                    filterSelectionDTO, retrieverChainDefinition);
            Query<HashSet<Object>> dimensionValuesQuery = dataMiningServer.createDimensionValuesQuery(
                    retrieverChainDefinition, retrieverLevel, nonBoolNonEnumResultDimensions, retrieverSettings,
                    filterSelection, locale);
            result = (ExtensibleQueryResult<HashSet<Object>>) dataMiningServer
                    .runNewQueryAndAbortPreviousQueries(session, dimensionValuesQuery);
        } else {
            @SuppressWarnings("unchecked")
            final Class<HashSet<Object>> clazz = (Class<HashSet<Object>>) new HashSet<Object>().getClass();
            result = new QueryResultImpl<HashSet<Object>>(QueryResultState.NORMAL, clazz,
                    new HashMap<GroupKey, HashSet<Object>>());
        }
        for (final Entry<Function<?>, List<?>> e : boolOrEnumResultDimensionsWithValues.entrySet()) {
            result.addResult(new GenericGroupKey<>(
                    dtoFactory.createFunctionDTO(e.getKey(), getDataMiningServer().getStringMessages(), locale)),
                    new LinkedHashSet<>(e.getValue()));
        }
        return dtoFactory.createResultDTO(result);
    }

    private Collection<Function<?>> functionDTOsAsFunctions(Iterable<FunctionDTO> functionDTOs) {
        List<Function<?>> functions = new ArrayList<>();
        DataMiningServer dataMiningServer = getDataMiningServer();
        for (FunctionDTO functionDTO : functionDTOs) {
            Function<?> function = dataMiningServer.getFunctionForDTO(functionDTO);
            if (function != null) {
                functions.add(function);
            }
        }
        return functions;
    }

    private Map<DataRetrieverLevel<?, ?>, SerializableSettings> retrieverSettingsDTOAsRetrieverSettings(
            HashMap<DataRetrieverLevelDTO, SerializableSettings> retrieverSettingsDTO,
            DataRetrieverChainDefinition<RacingEventService, ?> retrieverChainDefinition) {
        Map<DataRetrieverLevel<?, ?>, SerializableSettings> retrieverSettings = new HashMap<>();
        for (DataRetrieverLevelDTO retrieverLevelDTO : retrieverSettingsDTO.keySet()) {
            DataRetrieverLevel<?, ?> retrieverLevel = retrieverChainDefinition
                    .getDataRetrieverLevel(retrieverLevelDTO.getLevel());
            retrieverSettings.put(retrieverLevel, retrieverSettingsDTO.get(retrieverLevelDTO));
        }
        return retrieverSettings;
    }

    private Map<DataRetrieverLevel<?, ?>, Map<Function<?>, Collection<?>>> filterSelectionDTOAsFilterSelection(
            HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelectionDTO,
            DataRetrieverChainDefinition<?, ?> retrieverChainDefinition) {
        Map<DataRetrieverLevel<?, ?>, Map<Function<?>, Collection<?>>> filterSelection = new HashMap<>();
        for (DataRetrieverLevelDTO retrieverLevelDTO : filterSelectionDTO.keySet()) {
            HashMap<FunctionDTO, HashSet<? extends Serializable>> retrievalLevelSelection = filterSelectionDTO
                    .get(retrieverLevelDTO);
            for (FunctionDTO dimensionDTO : retrievalLevelSelection.keySet()) {
                if (!retrievalLevelSelection.get(dimensionDTO).isEmpty()) {
                    Function<?> function = getDataMiningServer().getFunctionForDTO(dimensionDTO);
                    DataRetrieverLevel<?, ?> retrieverLevel = retrieverChainDefinition
                            .getDataRetrieverLevel(retrieverLevelDTO.getLevel());
                    if (function != null) {
                        if (!filterSelection.containsKey(retrieverLevel)) {
                            filterSelection.put(retrieverLevel, new HashMap<Function<?>, Collection<?>>());
                        }
                        filterSelection.get(retrieverLevel).put(function, retrievalLevelSelection.get(dimensionDTO));
                    }
                }
            }
        }
        return filterSelection;
    }

    @Override
    public <ResultType extends Serializable> QueryResultDTO<ResultType> runQuery(DataMiningSession session,
            ModifiableStatisticQueryDefinitionDTO queryDefinitionDTO) {
        checkDataMiningPermission();
        DataMiningServer dataMiningServer = getDataMiningServer();
        StatisticQueryDefinition<RacingEventService, ?, ?, ResultType> queryDefinition = dataMiningServer
                .getQueryDefinitionForDTO(queryDefinitionDTO);
        Query<ResultType> query = dataMiningServer.createQuery(queryDefinition);
        QueryResult<ResultType> result = dataMiningServer.runNewQueryAndAbortPreviousQueries(session, query);
        return dtoFactory.createResultDTO(result);
    }

    @Override
    public HashSet<PredefinedQueryIdentifier> getPredefinedQueryIdentifiers() {
        checkDataMiningPermission();
        HashSet<PredefinedQueryIdentifier> predefinedQueryNames = new HashSet<PredefinedQueryIdentifier>();
        for (PredefinedQueryIdentifier predefinedQueryName : getDataMiningServer().getPredefinedQueryIdentifiers()) {
            predefinedQueryNames.add(predefinedQueryName);
        }
        return predefinedQueryNames;
    }
    
    @Override
    public ModifiableStatisticQueryDefinitionDTO getPredefinedQueryDefinition(PredefinedQueryIdentifier identifier,
            String localeInfoName) {
        checkDataMiningPermission();
        return (ModifiableStatisticQueryDefinitionDTO) localize(
                getDataMiningServer().getPredefinedQueryDefinitionDTO(identifier), localeInfoName);
    }

    @Override
    public <ResultType extends Serializable> QueryResultDTO<ResultType> runPredefinedQuery(DataMiningSession session,
            PredefinedQueryIdentifier identifier, String localeInfoName) {
        checkDataMiningPermission();
        DataMiningServer dataMiningServer = getDataMiningServer();
        ModifiableStatisticQueryDefinitionDTO queryDefinitionDTO = dataMiningServer
                .getPredefinedQueryDefinitionDTO(identifier);
        if (queryDefinitionDTO != null) {
            queryDefinitionDTO.setLocaleInfoName(localeInfoName);
            return runQuery(session, queryDefinitionDTO);
        }
        return null;
    }
    
    @Override
    public ModifiableStatisticQueryDefinitionDTO localize(ModifiableStatisticQueryDefinitionDTO queryDefinitionDTO,
            String localeInfoName) {
        checkDataMiningPermission();
        DataMiningServer dataMiningServer = getDataMiningServer();
        StatisticQueryDefinition<?, ?, ?, ?> queryDefinition = dataMiningServer.getQueryDefinitionForDTO(queryDefinitionDTO);
        Locale locale = ResourceBundleStringMessages.Util.getLocaleFor(localeInfoName);
        return (ModifiableStatisticQueryDefinitionDTO) dtoFactory.createQueryDefinitionDTO(queryDefinition,
                dataMiningServer.getStringMessages(), locale, localeInfoName);
    }

    @Override
    public SerializationDummy pseudoMethodSoThatSomeClassesAreAddedToTheGWTSerializationPolicy() {
        return null;
    }

    @Override
    public ArrayList<StoredDataMiningQueryDTOImpl> retrieveStoredQueries() {
        checkDataMiningPermission();
        return storedDataMiningQueryPersistor.retrieveStoredQueries();
    }

    @Override
    public StoredDataMiningQueryDTOImpl updateOrCreateStoredQuery(StoredDataMiningQueryDTOImpl query) {
        checkDataMiningPermission();
        return (StoredDataMiningQueryDTOImpl) storedDataMiningQueryPersistor.updateOrCreateStoredQuery(query);
    }

    @Override
    public StoredDataMiningQueryDTOImpl removeStoredQuery(StoredDataMiningQueryDTOImpl query) {
        checkDataMiningPermission();
        return (StoredDataMiningQueryDTOImpl) storedDataMiningQueryPersistor.removeStoredQuery(query);
    }

    @Override
    public ModifiableStatisticQueryDefinitionDTO getDeserializedQuery(String serializedQuery) {
        return (ModifiableStatisticQueryDefinitionDTO) DataMiningQuerySerializer.fromBase64String(serializedQuery);
    }
}
