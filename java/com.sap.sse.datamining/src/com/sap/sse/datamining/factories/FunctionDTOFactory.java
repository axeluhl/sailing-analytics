package com.sap.sse.datamining.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class FunctionDTOFactory {

    /**
     * Creates the corresponding DTO for the given function, with the functions simple name as display name.
     */
    public FunctionDTO createFunctionDTO(Function<?> function) {
        return createFunctionDTO(function, function.getSimpleName());
    }
    
    /**
     * Creates the corresponding DTO for the given function, with the retrieved string message for the given locale and the
     * contained message key as display name. The message key is provided with the {@link Dimension} or {@link Connector}
     * annotation.<br>
     * If the function has no message key, the function name is used as display name.
     */
    public FunctionDTO createFunctionDTO(Function<?> function, ResourceBundleStringMessages stringMessages, Locale locale) {
        return createFunctionDTO(function, function.getLocalizedName(locale, stringMessages));
    }
    
    private FunctionDTO createFunctionDTO(Function<?> function, String displayName) {
        String functionName = function.getSimpleName();
        String sourceTypeName = function.getDeclaringType().getSimpleName();
        String returnTypeName = function.getReturnType().getSimpleName();
        List<String> parameterTypeNames = getParameterTypeNames(function);
        return new FunctionDTO(function.isDimension(), functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, function.getOrdinal());
    }

    private List<String> getParameterTypeNames(Function<?> function) {
        List<String> parameterTypeNames = new ArrayList<>();
        for (Class<?> parameterType : function.getParameters()) {
            parameterTypeNames.add(parameterType.getSimpleName());
        }
        return parameterTypeNames;
    }

}
