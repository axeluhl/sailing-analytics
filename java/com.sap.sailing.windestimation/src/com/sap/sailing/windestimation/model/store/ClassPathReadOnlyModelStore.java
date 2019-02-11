package com.sap.sailing.windestimation.model.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.TrainableModel;
import com.sap.sailing.windestimation.model.exception.ModelLoadingException;
import com.sap.sailing.windestimation.model.exception.ModelNotFoundException;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

public class ClassPathReadOnlyModelStore extends AbstractModelStore {

    private final String destinationFolder;
    private final ClassLoader classLoader;
    private final String[] modelFileNames;

    public ClassPathReadOnlyModelStore(String destinationFolder, ClassLoader classLoader, String[] modelFileNames) {
        this.destinationFolder = destinationFolder;
        this.classLoader = classLoader;
        this.modelFileNames = modelFileNames;
    }

    private List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();

        // does not work in OSGI environment

        // try (InputStream in = getResourceAsStream(path)) {
        // if (in != null) {
        // try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
        // String resource;
        // while ((resource = br.readLine()) != null) {
        // filenames.add(resource);
        // }
        // }
        // }
        // }

        // Thats why use provided modelFileNames
        for (String fileName : modelFileNames) {
            filenames.add(fileName);
        }
        return filenames;
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in = classLoader.getResourceAsStream(resource);
        return in;
    }

    private String getFilePath(String filename) {
        if (destinationFolder.isEmpty()) {
            return filename;
        }
        return destinationFolder + "/" + filename;
    }

    @Override
    public <InstanceType, T extends ModelContext<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> ModelType loadPersistedState(
            ModelType newModel) throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(newModel);
        String fileName = getFilename(newModel);
        String filePath = getFilePath(fileName);
        InputStream input = getResourceAsStream(filePath);
        if (input != null) {
            try {
                @SuppressWarnings("unchecked")
                ModelType loadedModel = (ModelType) persistenceSupport.loadFromStream(input);
                if (!newModel.getModelContext().equals(loadedModel.getModelContext())) {
                    throw new ModelPersistenceException(
                            "The configuration of the loaded model is: " + loadedModel.getModelContext()
                                    + ". \nExpected: " + newModel.getModelContext());
                }
                return loadedModel;
            } catch (IOException e) {
                throw new ModelPersistenceException(e);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        throw new ModelNotFoundException(newModel.getModelContext());
    }

    @Override
    public <T extends PersistableModel<?, ?>> void persistState(T trainedModel) throws ModelPersistenceException {
        throw new UnsupportedOperationException();
    }

    public <T extends PersistableModel<?, ?>> void delete(T newModel) throws ModelPersistenceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll(PersistenceContextType contextType) throws ModelPersistenceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, byte[]> exportAllPersistedModels(PersistenceContextType contextType)
            throws ModelPersistenceException {
        Map<String, byte[]> exportedModels = new HashMap<>();
        try {
            for (String fileName : getResourceFiles(destinationFolder)) {
                if (isFileBelongingToContextType(fileName, contextType)) {
                    String filePath = getFilePath(fileName);
                    byte[] exportedModel;
                    try (InputStream inputStream = getResourceAsStream(filePath)) {
                        exportedModel = IOUtils.toByteArray(inputStream);
                    } catch (IOException e) {
                        throw new ModelPersistenceException("Could not read model \"" + fileName + "\" from filesystem",
                                e);
                    }
                    exportedModels.put(fileName, exportedModel);
                }
            }
        } catch (IOException e) {
            throw new ModelPersistenceException(e);
        }
        return exportedModels;
    }

    @Override
    public void importPersistedModels(Map<String, byte[]> exportedPersistedModels, PersistenceContextType contextType)
            throws ModelPersistenceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PersistableModel<?, ?>> loadAllPersistedModels(PersistenceContextType contextType) {
        List<PersistableModel<?, ?>> loadedModels = new ArrayList<>();
        try {
            for (String fileName : getResourceFiles(destinationFolder)) {
                if (isFileBelongingToContextType(fileName, contextType)) {
                    String filePath = getFilePath(fileName);
                    try (InputStream inputStream = getResourceAsStream(filePath)) {
                        PersistenceSupport persistenceSupport = getPersistenceSupportFromFilename(fileName);
                        if (persistenceSupport == null) {
                            throw new ModelLoadingException(
                                    "Persistence support could not be determined due to invalid filename pattern: \""
                                            + fileName + "\"");
                        }
                        PersistableModel<?, ?> loadedModel;
                        try (InputStream input = getResourceAsStream(filePath)) {
                            loadedModel = persistenceSupport.loadFromStream(input);
                        } catch (Exception e) {
                            throw new ModelLoadingException("Could not read model \"" + fileName + "\" from filesystem",
                                    e);
                        }
                        loadedModels.add(loadedModel);
                    }
                }
            }
        } catch (IOException e) {
            throw new ModelLoadingException(e);
        }
        return loadedModels;
    }

}
