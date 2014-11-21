package com.sap.sailing.gwt.ui.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.datamining.factories.SailingDataMiningFactory;
import com.sap.sailing.datamining.shared.SailingDataMiningSerializationDummy;
import com.sap.sailing.gwt.ui.datamining.DataMiningService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.DataRetrieverTypeWithInformation;
import com.sap.sse.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.SSEDataMiningSerializationDummy;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;

public class DataMiningServiceImpl extends RemoteServiceServlet implements DataMiningService {
    private static final long serialVersionUID = -7951930891674894528L;

    private final BundleContext context;

    private final SailingDataMiningFactory sailingDataMiningFactory;
    private final FunctionDTOFactory functionDTOFactory;

    private final ServiceTracker<DataMiningServer, DataMiningServer> dataMiningServerTracker;
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    public DataMiningServiceImpl() {
        context = Activator.getDefault();
        
        dataMiningServerTracker = createAndOpenDataMiningServerTracker(context);
        racingEventServiceTracker = createAndOpenRacingEventServiceTracker(context);
        
        sailingDataMiningFactory = new SailingDataMiningFactory(getDataMiningServer().getExecutorService(),
                                                                getDataMiningServer().getStringMessages(),
                                                                getDataMiningServer().getFunctionProvider(),
                                                                getDataMiningServer().getDataRetrieverChainDefinitionProvider());
        functionDTOFactory = new FunctionDTOFactory();
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

    private ServiceTracker<RacingEventService, RacingEventService> createAndOpenRacingEventServiceTracker(BundleContext context) {
        ServiceTracker<RacingEventService, RacingEventService> result = new ServiceTracker<RacingEventService, RacingEventService>(
                context, RacingEventService.class.getName(), null);
        result.open();
        return result;
    }

    private RacingEventService getRacingEventService() {
        return racingEventServiceTracker.getService();
    }
    
    @Override
    public Iterable<FunctionDTO> getAllStatistics(String localeInfoName) {
        Iterable<Function<?>> statistics = getDataMiningServer().getAllStatistics();
        return functionsAsFunctionDTOs(statistics, localeInfoName);
    }

    @Override
    public Iterable<FunctionDTO> getDimensionsFor(FunctionDTO statisticToCalculate, String localeInfoName) {
        Class<?> baseDataType = getBaseDataType(statisticToCalculate);
        Iterable<Function<?>> dimensions = getDataMiningServer().getDimensionsFor(baseDataType);
        return functionsAsFunctionDTOs(dimensions, localeInfoName);
    }
    
    @Override
    public Iterable<FunctionDTO> getDimensionsFor(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, String localeInfoName) {
        DataRetrieverChainDefinition<?> dataRetrieverChainDefinition = getDataMiningServer().getDataRetrieverChainDefinition(dataRetrieverChainDefinitionDTO.getId());
        Iterable<Function<?>> dimensions = getDataMiningServer().getDimensionsFor(dataRetrieverChainDefinition);
        return functionsAsFunctionDTOs(dimensions, localeInfoName);
    }

    private Class<?> getBaseDataType(FunctionDTO statisticToCalculate) {
        Function<?> function = getDataMiningServer().getFunctionForDTO(statisticToCalculate);
        return function.getDeclaringType();
    }
    
    private Collection<FunctionDTO> functionsAsFunctionDTOs(Iterable<Function<?>> functions, String localeInfoName) {
        Locale locale = DataMiningStringMessages.Util.getLocaleFor(localeInfoName);
        DataMiningStringMessages dataMiningStringMessages = getDataMiningServer().getStringMessages();
        
        Collection<FunctionDTO> functionDTOs = new ArrayList<FunctionDTO>();
        for (Function<?> function : functions) {
            functionDTOs.add(functionDTOFactory.createFunctionDTO(function, dataMiningStringMessages, locale));
        }
        return functionDTOs;
    }
    
    @Override
    public Iterable<DataRetrieverChainDefinitionDTO> getDataRetrieverChainDefinitionsFor(FunctionDTO statisticToCalculate, String localeInfoName) {
        Class<?> baseDataType = getBaseDataType(statisticToCalculate);
        return dataRetrieverChainDefinitionsAsDTOs(getDataMiningServer().getDataRetrieverChainDefinitions(RacingEventService.class, baseDataType), localeInfoName);
    }
    
    private Collection<DataRetrieverChainDefinitionDTO> dataRetrieverChainDefinitionsAsDTOs(
            Iterable<DataRetrieverChainDefinition<RacingEventService>> dataRetrieverChainDefinitions, String localeInfoName) {
        Locale locale = DataMiningStringMessages.Util.getLocaleFor(localeInfoName);
        DataMiningStringMessages dataMiningStringMessages = getDataMiningServer().getStringMessages();
        
        Collection<DataRetrieverChainDefinitionDTO> DTOs = new ArrayList<>();
        for (DataRetrieverChainDefinition<RacingEventService> dataRetrieverChainDefinition : dataRetrieverChainDefinitions) {
            Collection<LocalizedTypeDTO> retrievedDataTypesChain = new ArrayList<>();
            for (DataRetrieverTypeWithInformation<?, ?> retrieverTypeWithInformation : dataRetrieverChainDefinition.getDataRetrieverTypesWithInformation()) {
                String typeName = retrieverTypeWithInformation.getRetrievedDataType().getSimpleName();
                String displayName = retrieverTypeWithInformation.getRetrievedDataTypeMessageKey() != null && !retrieverTypeWithInformation.getRetrievedDataTypeMessageKey().isEmpty() ?
                                        dataMiningStringMessages.get(locale, retrieverTypeWithInformation.getRetrievedDataTypeMessageKey()) : 
                                        typeName;
                LocalizedTypeDTO localizedRetrievedDataType = new LocalizedTypeDTO(typeName, displayName);
                retrievedDataTypesChain.add(localizedRetrievedDataType);
            }
            DTOs.add(new DataRetrieverChainDefinitionDTO(dataRetrieverChainDefinition.getID(), dataRetrieverChainDefinition.getLocalizedName(locale, dataMiningStringMessages),
                                                         dataRetrieverChainDefinition.getDataSourceType().getSimpleName(), retrievedDataTypesChain));
        }
        return DTOs;
    }

    @Override
    public QueryResult<Set<Object>> getDimensionValuesFor(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinition, int retrieverLevel, Collection<FunctionDTO> dimensions, String localeInfoName) throws Exception {
        Query<Set<Object>> dimensionValuesQuery = sailingDataMiningFactory.createDimensionValuesQuery(getRacingEventService(), dataRetrieverChainDefinition, retrieverLevel, dimensions, localeInfoName);
        return dimensionValuesQuery.run();
    }

    @Override
    public <ResultType extends Number> QueryResult<ResultType> runQuery(QueryDefinition queryDefinition) throws Exception {
        @SuppressWarnings("unchecked") // TODO Fix after the data mining has been cleaned
        Query<ResultType> query = (Query<ResultType>) sailingDataMiningFactory.createQuery(getRacingEventService(), queryDefinition);
        return query.run();
    }
    
    @Override
    public SSEDataMiningSerializationDummy pseudoMethodSoThatSomeSSEDataMiningClassesAreAddedToTheGWTSerializationPolicy() {
        return null;
    }
    
    @Override
    public SailingDataMiningSerializationDummy pseudoMethodSoThatSomeSailingDataMiningClassesAreAddedToTheGWTSerializationPolicy() {
        return null;
    }

}
