package com.sap.sse.datamining;

import java.util.Set;

public interface ClassesWithFunctionsRegistrationService {

    public void registerInternalClassesWithMarkedMethods(Set<Class<?>> classesToScan);
    public void registerExternalLibraryClasses(Set<Class<?>> externalClassesToScan);
    
    public void unregisterAllFunctionsOf(Set<Class<?>> classesToUnregister);

}
