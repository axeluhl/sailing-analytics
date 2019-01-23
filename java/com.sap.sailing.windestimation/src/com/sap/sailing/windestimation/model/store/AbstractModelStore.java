package com.sap.sailing.windestimation.model.store;

public abstract class AbstractModelStore implements ModelStore {

    protected static final String CONTEXT_NAME_PREFIX = "modelFor";
    protected static final String FILE_EXT = ".clf";

    protected String getFilename(PersistenceSupport trainedModel, PersistenceContextType contextType) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(getContextPrefix(contextType));
        fileName.append(trainedModel.getPersistenceKey());
        fileName.append(FILE_EXT);
        String finalFileName = replaceSystemChars(fileName.toString());
        return finalFileName;
    }

    protected String replaceSystemChars(String str) {
        return str.replaceAll("[\\\\\\/\\\"\\:\\|\\<\\>\\*\\?]", "__");
    }

    protected String getContextPrefix(PersistenceContextType contextType) {
        return CONTEXT_NAME_PREFIX + contextType.getContextName() + ".";
    }

}
