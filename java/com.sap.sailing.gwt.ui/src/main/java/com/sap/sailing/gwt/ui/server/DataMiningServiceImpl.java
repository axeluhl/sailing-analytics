package com.sap.sailing.gwt.ui.server;

import java.util.ArrayList;
import java.util.Collection;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.factories.DataMiningFactory;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.datamining.shared.SailingDataMiningSerializationDummy;
import com.sap.sailing.gwt.ui.datamining.DataMiningService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.SSEDataMiningSerializationDummy;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class DataMiningServiceImpl extends RemoteServiceServlet implements DataMiningService {
    private static final long serialVersionUID = -7951930891674894528L;

    private final BundleContext context;

    private final ServiceTracker<DataMiningServer, DataMiningServer> dataMiningServerTracker;
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    public DataMiningServiceImpl() {
        context = Activator.getDefault();
        dataMiningServerTracker = createAndOpenDataMiningServerTracker(context);
        racingEventServiceTracker = createAndOpenRacingEventServiceTracker(context);
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
    public Collection<FunctionDTO> getAllStatistics() {
        Collection<Function<?>> statistics = getDataMiningServer().getFunctionProvider().getAllStatistics();
        return functionsAsFunctionDTOs(statistics);
    }

    @Override
    public Collection<FunctionDTO> getDimensionsFor(DataTypes dataType) {
        Class<?> dataTypeBaseClass = getBaseClassFor(dataType);
        Collection<Function<?>> dimensions = getDataMiningServer().getFunctionProvider().getDimensionsFor(dataTypeBaseClass);
        return functionsAsFunctionDTOs(dimensions);
    }
    
    private Class<?> getBaseClassFor(DataTypes dataType) {
        switch (dataType) {
        case GPSFix:
            return HasGPSFixContext.class;
        case TrackedLegOfCompetitor:
            return HasTrackedLegOfCompetitorContext.class;
        }
        throw new IllegalArgumentException("No base class for data type " + dataType);
    }

    private Collection<FunctionDTO> functionsAsFunctionDTOs(Collection<Function<?>> functions) {
        Collection<FunctionDTO> functionDTOs = new ArrayList<FunctionDTO>();
        for (Function<?> function : functions) {
            functionDTOs.add(FunctionDTOFactory.createFunctionDTO(function));
        }
        return functionDTOs;
    }

    @Override
    public <ResultType extends Number> QueryResult<ResultType> runQuery(QueryDefinitionDeprecated queryDefinition) throws Exception {
        @SuppressWarnings("unchecked") // TODO Fix after the data mining has been cleaned
        Query<ResultType> query = (Query<ResultType>) DataMiningFactory.createQuery(getRacingEventService(), queryDefinition, getDataMiningServer().getFunctionProvider());
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
