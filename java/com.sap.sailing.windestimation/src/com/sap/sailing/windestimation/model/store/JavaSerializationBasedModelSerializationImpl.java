package com.sap.sailing.windestimation.model.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

/**
 * Serialization strategy which makes use of default Java serialization/deserialization.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class JavaSerializationBasedModelSerializationImpl implements ModelSerializationStrategy {

    @Override
    public void serializeToStream(PersistableModel<?, ?> model, OutputStream output) throws ModelPersistenceException {
        try (ObjectOutputStream serializer = new ObjectOutputStream(output)) {
            serializer.writeObject(model);
        } catch (IOException e) {
            throw new ModelPersistenceException(e);
        }
    }

    @Override
    public PersistableModel<?, ?> deserializeFromStream(InputStream input) throws ModelPersistenceException {
        try (ObjectInputStream deserializer = new ObjectInputStream(input)) {
            PersistableModel<?, ?> loadedModel = (PersistableModel<?, ?>) deserializer.readObject();
            return loadedModel;
        } catch (ClassNotFoundException | IOException e) {
            throw new ModelPersistenceException(e);
        }
    };
}