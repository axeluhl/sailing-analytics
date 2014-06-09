package com.sap.sse.datamining;

import java.util.Set;

public interface ClassesWithFunctionsProvider {

    public Set<Class<?>> getInternalClassesWithMarkedMethods();
    public Set<Class<?>> getExternalLibraryClasses();

}
