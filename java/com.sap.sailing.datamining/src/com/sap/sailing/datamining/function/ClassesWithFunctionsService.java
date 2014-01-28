package com.sap.sailing.datamining.function;

import java.util.Collection;

public interface ClassesWithFunctionsService {

    public boolean hasInternalClassesWithMarkedMethods();
    public Collection<Class<?>> getInternalClassesWithMarkedMethods();

    public boolean hasExternalLibraryClasses();
    public Collection<Class<?>> getExternalLibraryClasses();

}
