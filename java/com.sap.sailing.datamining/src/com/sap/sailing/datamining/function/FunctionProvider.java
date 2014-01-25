package com.sap.sailing.datamining.function;

import java.util.Collection;

public interface FunctionProvider {

    public Collection<Function> getDimenionsFor(Class<?> dataType);

}
