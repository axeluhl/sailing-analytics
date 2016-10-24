package com.sap.sse.datamining.exceptions;

public class DataMiningComponentAlreadyRegisteredForKeyException extends RuntimeException {
    private static final long serialVersionUID = 3675356451959822039L;

    public DataMiningComponentAlreadyRegisteredForKeyException(Object key, Object newComponent, Object registeredComponent) {
        super(buildErrorMessage(key, newComponent, registeredComponent));
    }

    private static String buildErrorMessage(Object key, Object newComponent, Object registeredComponent) {
        StringBuilder messageBuilder = new StringBuilder("There's already a componente registered for the key " + key + ":\n");
        messageBuilder.append("Registered Component: " + registeredComponent + "\n");
        messageBuilder.append("New Component: " + newComponent);
        return messageBuilder.toString();
    }

}
