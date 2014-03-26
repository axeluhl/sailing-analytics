package com.sap.sailing.gwt.ui.server;

import java.util.ArrayList;
import java.util.Collection;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.factories.DataMiningFactory;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.gwt.ui.datamining.client.DataMiningService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.DataMiningSerializationDummy;
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
        ServiceTracker<com.sap.sse.datamining.DataMiningServer, com.sap.sse.datamining.DataMiningServer> result = 
                new ServiceTracker<com.sap.sse.datamining.DataMiningServer, com.sap.sse.datamining.DataMiningServer>(
                context, com.sap.sse.datamining.DataMiningServer.class.getName(), null);
        result.open();
        return result;
    }
    
    private com.sap.sse.datamining.DataMiningServer getDataMiningServer() {
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
    public Collection<FunctionDTO> getDimensionsFor(DataTypes dataType) {
        Class<?> dataTypeBaseClass = getBaseClassFor(dataType);
        Collection<Function<?>> dimensions = getDataMiningServer().getFunctionProvider().getDimensionsFor(dataTypeBaseClass);
        return functionsAsFunctionDTOs(dimensions);
    }
    
    private Class<?> getBaseClassFor(DataTypes dataType) {
        switch (dataType) {
        case GPSFix:
            return GPSFixWithContext.class;
        case TrackedLegOfCompetitor:
            return TrackedLegOfCompetitorWithContext.class;
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
    public <ResultType extends Number> QueryResult<ResultType> runQuery(QueryDefinition queryDefinition) throws Exception {
        Query<ResultType> query = DataMiningFactory.createQuery(queryDefinition, getRacingEventService());
        return query.run();
    }
    
    @Override
    public DataMiningSerializationDummy pseudoMethodSoThatSomeDataMiningClassesAreAddedToTheGWTSerializationPolicy() {
        return null;
    }

}
