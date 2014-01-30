package com.sap.sailing.datamining.function;

import java.util.Collection;

import com.sap.sailing.datamining.shared.dto.FunctionDTO;

public interface FunctionProvider {

    public Collection<Function<?>> getDimenionsFor(Class<?> dataType);

    public Collection<Function<?>> getFunctionsFor(Class<?> sourceType);

    public Function<?> getFunctionFor(FunctionDTO functionDTO);

}
