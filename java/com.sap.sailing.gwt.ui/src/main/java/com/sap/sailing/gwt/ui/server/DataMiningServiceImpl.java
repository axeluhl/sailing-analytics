package com.sap.sailing.gwt.ui.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.datamining.shared.SailingDataMiningSerializationDummy;
import com.sap.sailing.gwt.ui.datamining.DataMiningService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.QueryDefinition;
import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.DataRetrieverTypeWithInformation;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.SSEDataMiningSerializationDummy;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class DataMiningServiceImpl extends RemoteServiceServlet implements DataMiningService {
    private static final long serialVersionUID = -7951930891674894528L;

    private final BundleContext context;
    
    private final ServiceTracker<DataMiningServer, DataMiningServer> dataMiningServerTracker;

    private final FunctionDTOFactory functionDTOFactory;
    
    public DataMiningServiceImpl() {
        context = Activator.getDefault();
        dataMiningServerTracker = createAndOpenDataMiningServerTracker(context);
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
        DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition = getDataMiningServer().getDataRetrieverChainDefinition(dataRetrieverChainDefinitionDTO.getId());
        Iterable<Function<?>> dimensions = getDataMiningServer().getDimensionsFor(dataRetrieverChainDefinition);
        return functionsAsFunctionDTOs(dimensions, localeInfoName);
    }

    private Class<?> getBaseDataType(FunctionDTO statisticToCalculate) {
        Function<?> function = getDataMiningServer().getFunctionForDTO(statisticToCalculate);
        return function.getDeclaringType();
    }
    
    private Collection<FunctionDTO> functionsAsFunctionDTOs(Iterable<Function<?>> functions, String localeInfoName) {
        Locale locale = ResourceBundleStringMessages.Util.getLocaleFor(localeInfoName);
        ResourceBundleStringMessages ServerStringMessages = getDataMiningServer().getStringMessages();
        
        Collection<FunctionDTO> functionDTOs = new ArrayList<FunctionDTO>();
        for (Function<?> function : functions) {
            functionDTOs.add(functionDTOFactory.createFunctionDTO(function, ServerStringMessages, locale));
        }
        return functionDTOs;
    }
    
    @Override
    public Iterable<DataRetrieverChainDefinitionDTO> getDataRetrieverChainDefinitionsFor(FunctionDTO statisticToCalculate, String localeInfoName) {
        Class<?> baseDataType = getBaseDataType(statisticToCalculate);
        @SuppressWarnings("unchecked")
        Iterable<DataRetrieverChainDefinition<?, ?>> dataRetrieverChainDefinitions = (Iterable<DataRetrieverChainDefinition<?, ?>>)(Iterable<?>)  getDataMiningServer().getDataRetrieverChainDefinitionsByDataType(baseDataType);
        return dataRetrieverChainDefinitionsAsDTOs(dataRetrieverChainDefinitions, localeInfoName);
    }
    
    private Collection<DataRetrieverChainDefinitionDTO> dataRetrieverChainDefinitionsAsDTOs(
            Iterable<DataRetrieverChainDefinition<?, ?>> dataRetrieverChainDefinitions, String localeInfoName) {
        Locale locale = ResourceBundleStringMessages.Util.getLocaleFor(localeInfoName);
        ResourceBundleStringMessages serverStringMessages = getDataMiningServer().getStringMessages();
        
        Collection<DataRetrieverChainDefinitionDTO> DTOs = new ArrayList<>();
        for (DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition : dataRetrieverChainDefinitions) {
            Collection<LocalizedTypeDTO> retrievedDataTypesChain = new ArrayList<>();
            for (DataRetrieverTypeWithInformation<?, ?> retrieverTypeWithInformation : dataRetrieverChainDefinition.getDataRetrieverTypesWithInformation()) {
                String typeName = retrieverTypeWithInformation.getRetrievedDataType().getSimpleName();
                String displayName = retrieverTypeWithInformation.getRetrievedDataTypeMessageKey() != null && !retrieverTypeWithInformation.getRetrievedDataTypeMessageKey().isEmpty() ?
                                        serverStringMessages.get(locale, retrieverTypeWithInformation.getRetrievedDataTypeMessageKey()) : 
                                        typeName;
                LocalizedTypeDTO localizedRetrievedDataType = new LocalizedTypeDTO(typeName, displayName);
                retrievedDataTypesChain.add(localizedRetrievedDataType);
            }
            DTOs.add(new DataRetrieverChainDefinitionDTO(dataRetrieverChainDefinition.getID(), dataRetrieverChainDefinition.getLocalizedName(locale, serverStringMessages),
                                                         dataRetrieverChainDefinition.getDataSourceType().getSimpleName(), retrievedDataTypesChain));
        }
        return DTOs;
    }

    @Override
    public QueryResult<Set<Object>> getDimensionValuesFor(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO,
            int retrieverLevel, Iterable<FunctionDTO> dimensionDTOs, String localeInfoName) throws Exception {
        DataRetrieverChainDefinition<RacingEventService, ?> retrieverChainDefinition = getDataMiningServer().getDataRetrieverChainDefinition(dataRetrieverChainDefinitionDTO.getId());
        Iterable<Function<?>> dimensions = functionDTOsAsFunctions(dimensionDTOs);
        Locale locale = ResourceBundleStringMessages.Util.getLocaleFor(localeInfoName);
        Query<Set<Object>> dimensionValuesQuery = getDataMiningServer().createDimensionValuesQuery(retrieverChainDefinition, retrieverLevel, dimensions, locale);
        QueryResult<Set<Object>> result = dimensionValuesQuery.run();
        return result;
    }

    private Collection<Function<?>> functionDTOsAsFunctions(Iterable<FunctionDTO> functionDTOs) {
        List<Function<?>> functions = new ArrayList<>();
        DataMiningServer dataMiningServer = getDataMiningServer();
        for (FunctionDTO functionDTO : functionDTOs) {
            functions.add(dataMiningServer.getFunctionForDTO(functionDTO));
        }
        return functions;
    }

    @Override
    public <ResultType extends Number> QueryResult<ResultType> runQuery(QueryDefinitionDTO queryDefinitionDTO) throws Exception {
        QueryDefinition<RacingEventService, ?, ResultType> queryDefinition = getDataMiningServer().getQueryDefinitionForDTO(queryDefinitionDTO);
        Query<ResultType> query = getDataMiningServer().createQuery(queryDefinition);
        QueryResult<ResultType> result = query.run();
        return result;
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
