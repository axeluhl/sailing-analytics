package com.sap.sse.datamining.exceptions;

public class MultipleDataMiningComponentsFoundForDTOException extends RuntimeException {
    private static final long serialVersionUID = 8369550688604565448L;
    
    public MultipleDataMiningComponentsFoundForDTOException(Object dto, Iterable<?> componentsFound) {
        super(buildErrorMessage(dto, componentsFound));
    }

    private static String buildErrorMessage(Object dto, Iterable<?> componentsFound) {
        StringBuilder messageBuilder = new StringBuilder("Multiple components found for " + dto.toString() + ". The found components are:");
        boolean first = true;
        for (Object component : componentsFound) {
            if (first) {
                messageBuilder.append("\n");
            }
            messageBuilder.append(component.toString());
        }
        return messageBuilder.toString();
    }

}
