package com.sap.sailing.windestimation.model.store;

public abstract class AbstractModelStore implements ModelStore {

    private static final String NAME_PART_DELIMITER = ".";
    protected static final String CONTEXT_NAME_PREFIX = "modelFor";
    protected static final String FILE_EXT = ".clf";

    public AbstractModelStore() {
        for (PersistenceSupportType persistenceSupportType : PersistenceSupportType.values()) {
            if (persistenceSupportType != PersistenceSupportType.NONE
                    && persistenceSupportType.getPersistenceSupport().getId().contains(NAME_PART_DELIMITER)) {
                throw new IllegalStateException("PersistenceSupport implementation "
                        + persistenceSupportType.getPersistenceSupport().getClass().getName()
                        + " has invalid characters in its getId(): \"" + NAME_PART_DELIMITER + "\"");
            }
        }
    }

    protected String getFilename(PersistableModel<?, ?> persistableModel) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(getPersistenceSupportTypePrefix(persistableModel.getPersistenceSupportType()));
        PersistenceContextType contextType = persistableModel.getContextSpecificModelMetadata().getContextType();
        fileName.append(getContextPrefix(contextType));
        String persistenceKey = persistableModel.getPersistenceSupportType().getPersistenceSupport()
                .getPersistenceKey(persistableModel);
        fileName.append(persistenceKey);
        fileName.append(FILE_EXT);
        String finalFileName = replaceSystemChars(fileName.toString());
        return finalFileName;
    }

    protected String replaceSystemChars(String str) {
        return str.replaceAll("[\\\\\\/\\\"\\:\\|\\<\\>\\*\\?]", "__");
    }

    protected String getContextPrefix(PersistenceContextType contextType) {
        return CONTEXT_NAME_PREFIX + contextType.getContextName() + NAME_PART_DELIMITER;
    }

    protected String getPersistenceSupportTypePrefix(PersistenceSupportType persistenceSupportType) {
        return persistenceSupportType.getPersistenceSupport().getId() + NAME_PART_DELIMITER;
    }

    protected PersistenceSupport getPersistenceSupportFromFilename(String filename) {
        for (PersistenceSupportType persistenceSupportType : PersistenceSupportType.values()) {
            if (persistenceSupportType != PersistenceSupportType.NONE
                    && filename.startsWith(getPersistenceSupportTypePrefix(persistenceSupportType))) {
                return persistenceSupportType.getPersistenceSupport();
            }
        }
        return null;
    }

    protected boolean isFileBelongingToContextType(String filename, PersistenceContextType contextType) {
        return filename.endsWith(FILE_EXT)
                && filename.substring(filename.indexOf(NAME_PART_DELIMITER)).startsWith(NAME_PART_DELIMITER + getContextPrefix(contextType));
    }

}
