package com.sap.sailing.polars.mining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;

public class PolarDataDimensionCollectionFactory {

    public static Collection<Function<?>> getClusterKeyDimensions() throws NoSuchMethodException {
        Collection<Function<?>> dimensions = new ArrayList<>();
        FunctionFactory functionFactory = new FunctionFactory();
        Function<RoundedTrueWindAngle> angleFunction = functionFactory
                .createMethodWrappingFunction(PolarClusterKey.class.getMethod("getRoundedTrueWindAngle",
                        new Class<?>[0]));
        Function<WindSpeedLevel> windSpeedFunction = functionFactory.createMethodWrappingFunction(PolarClusterKey.class
                .getMethod("getWindSpeedCluster", new Class<?>[0]));
        Function<BoatClass> boatClassFunction = functionFactory.createMethodWrappingFunction(PolarClusterKey.class
                .getMethod("getBoatClass", new Class<?>[0]));

        dimensions.add(boatClassFunction);
        dimensions.add(angleFunction);
        dimensions.add(windSpeedFunction);
        return dimensions;
    }

}
