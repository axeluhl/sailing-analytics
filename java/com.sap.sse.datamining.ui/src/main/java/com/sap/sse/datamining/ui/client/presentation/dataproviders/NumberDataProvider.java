package com.sap.sse.datamining.ui.client.presentation.dataproviders;

import java.util.LinkedHashMap;
import java.util.function.Function;

import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.StringMessages;

public class NumberDataProvider extends AbstractNumericDataProviderWithStaticMappings<Number> {
    private final static String FLOAT = "Float";
    private final static String INTEGER = "Integer";

    public NumberDataProvider() {
        super(Number.class, getMappings());
    }

    private static LinkedHashMap<String, Function<Number, Number>> getMappings() {
        LinkedHashMap<String, Function<Number, Number>> mappings = new LinkedHashMap<>();
        mappings.put(FLOAT, number -> number.doubleValue());
        mappings.put(INTEGER, number -> number.intValue());
        return mappings;
    }

    @Override
    public boolean acceptsResultsOfType(String type) {
        return type.equals(Number.class.getName()) || type.equals(Double.class.getName())
                || type.equals(Float.class.getName()) || type.equals(Long.class.getName())
                || type.equals(Integer.class.getName()) || type.equals(Short.class.getName())
                || type.equals(Byte.class.getName()) || type.equals(double.class.getName())
                || type.equals(float.class.getName()) || type.equals(long.class.getName())
                || type.equals(int.class.getName()) || type.equals(short.class.getName())
                || type.equals(byte.class.getName());
    }

    @Override
    public String getDefaultDataKeyFor(QueryResultDTO<?> result) {
        return result.getValueDecimals() == 0 ? INTEGER : FLOAT;
    }

    @Override
    public String getLocalizedNameForDataKey(QueryResultDTO<?> result, StringMessages stringMessages, String dataKey) {
        switch (dataKey) {
        case FLOAT:
            return stringMessages.floatNumber(); // TODO extract into com.sap.sse.gwt.client.StringMessages
        case INTEGER:
            return stringMessages.integer();
        }
        throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
    }
}
