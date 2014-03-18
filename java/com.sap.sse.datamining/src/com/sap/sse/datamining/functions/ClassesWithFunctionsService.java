package com.sap.sse.datamining.functions;

import java.util.Collection;

public interface ClassesWithFunctionsService {

    public boolean hasInternalClassesWithMarkedMethods();
    public Collection<Class<?>> getInternalClassesWithMarkedMethods();

    public boolean hasExternalLibraryClasses();
    public Collection<Class<?>> getExternalLibraryClasses();

}
