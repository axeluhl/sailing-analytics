package com.sap.sse.datamining;

import java.util.Set;

public interface ClassesWithFunctionsService {

    public Set<Class<?>> getInternalClassesWithMarkedMethods();
    public Set<Class<?>> getExternalLibraryClasses();

}
