package com.sap.sse.datamining.functions;

import java.util.Set;

public interface ClassesWithFunctionsService {

    public Set<Class<?>> getInternalClassesWithMarkedMethods();
    public Set<Class<?>> getExternalLibraryClasses();

}
