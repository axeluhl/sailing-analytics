package com.sap.sailing.polars.mining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;

public class PolarDataDimensionCollectionFactory {

    public static Collection<Function<?>> getMovingAverageClusterKeyDimensions() throws NoSuchMethodException {
        Collection<Function<?>> dimensions = new ArrayList<>();
        FunctionFactory functionFactory = new FunctionFactory();
        addTackAndLegTypeDimensions(dimensions, functionFactory);

        addPolarBaseDimension(dimensions, functionFactory);


        
        Function<WindSpeedLevel> windSpeedFunction = functionFactory.createMethodWrappingFunction(MovingAveragePolarClusterKey.class
                .getMethod("getWindSpeedCluster", new Class<?>[0]));
        dimensions.add(windSpeedFunction);
        return dimensions;
    }

    private static void addTackAndLegTypeDimensions(Collection<Function<?>> dimensions, FunctionFactory functionFactory)
            throws NoSuchMethodException {
        Function<Tack> tackFunction = functionFactory
                .createMethodWrappingFunction(MovingAveragePolarClusterKey.class.getMethod("getTack",
                        new Class<?>[0]));
        Function<LegType> legTypeFunction = functionFactory
                .createMethodWrappingFunction(MovingAveragePolarClusterKey.class.getMethod("getLegType",
                        new Class<?>[0]));
        dimensions.add(tackFunction);
        dimensions.add(legTypeFunction);
    }

    private static void addPolarBaseDimension(Collection<Function<?>> dimensions, FunctionFactory functionFactory)
            throws NoSuchMethodException {
        Function<BoatClass> boatClassFunction = functionFactory.createMethodWrappingFunction(MovingAveragePolarClusterKey.class
                .getMethod("getBoatClass", new Class<?>[0]));
        dimensions.add(boatClassFunction);
    }

    public static Collection<Function<?>> getCubicRegressionPerCourseClusterKeyDimensions() throws NoSuchMethodException {
        Collection<Function<?>> dimensions = new ArrayList<>();
        FunctionFactory functionFactory = new FunctionFactory();
        addTackAndLegTypeDimensions(dimensions, functionFactory);

        addPolarBaseDimension(dimensions, functionFactory);
        return dimensions;
    }
    
    public static Collection<Function<?>> getSpeedRegressionPerAngleClusterClusterKeyDimensions() throws NoSuchMethodException {
        Collection<Function<?>> dimensions = new ArrayList<>();
        FunctionFactory functionFactory = new FunctionFactory();

        addPolarBaseDimension(dimensions, functionFactory);
        
        Function<Cluster<Bearing>> angleDiffTrueWindToBoatFunction = functionFactory.createMethodWrappingFunction(AngleClusterPolarClusterKey.class
                .getMethod("getAngleCluster", new Class<?>[0]));
        dimensions.add(angleDiffTrueWindToBoatFunction);
        
        return dimensions;
    }

}
