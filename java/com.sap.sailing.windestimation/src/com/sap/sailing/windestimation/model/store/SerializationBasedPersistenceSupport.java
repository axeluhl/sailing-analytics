package com.sap.sailing.windestimation.model.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

public class SerializationBasedPersistenceSupport implements PersistenceSupport {

    private final PersistableModel<?, ?> model;

    public SerializationBasedPersistenceSupport(PersistableModel<?, ?> model) {
        this.model = model;
    }

    @Override
    public String getPersistenceKey() {
        StringBuilder key = new StringBuilder();
        key.append(model.getClass().getSimpleName());
        key.append("-");
        key.append(model.getContextSpecificModelMetadata().getId());
        return key.toString();
    }

    @Override
    public void saveToStream(OutputStream output) throws ModelPersistenceException {
        try (ObjectOutputStream serializer = new ObjectOutputStream(output)) {
            serializer.writeObject(model);
        } catch (IOException e) {
            throw new ModelPersistenceException(e);
        }
    }

    @Override
    public PersistableModel<?, ?> loadFromStream(InputStream input) throws ModelPersistenceException {
        try (ObjectInputStream deserializer = DomainFactory.INSTANCE
                .createObjectInputStreamResolvingAgainstThisFactory(input)) {
            PersistableModel<?, ?> loadedModel = (PersistableModel<?, ?>) deserializer.readObject();
            return loadedModel;
        } catch (ClassNotFoundException | IOException e) {
            throw new ModelPersistenceException(e);
        }
    };
}