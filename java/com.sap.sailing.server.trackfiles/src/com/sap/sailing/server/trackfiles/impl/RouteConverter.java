package com.sap.sailing.server.trackfiles.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import slash.navigation.base.BaseRoute;
import slash.navigation.gpx.GpxRoute;

@SuppressWarnings("rawtypes")
abstract class RouteConverter {
    abstract BaseRoute convert(GpxRoute route);

    List<BaseRoute> convert(Collection<GpxRoute> routes) {
        List<BaseRoute> result = new ArrayList<BaseRoute>(routes.size());
        for (GpxRoute route : routes)
            result.add(convert(route));
        return result;
    }
}