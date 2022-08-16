package com.sap.sailing.windestimation.model.store;

import java.io.InputStream;
import java.io.OutputStream;

import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

/**
 * Defines strategies for serialization/deserialization of {@link PersistableModel}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ModelSerializationStrategy {

    /**
     * Serializes provided model and writes the serialization bytes to the provided output stream.
     * 
     * @param model
     *            Model to serialize
     * @param output
     *            The stream where serialized bytes of the model must be written to
     * @throws ModelPersistenceException
     *             If something goes wrong
     */
    void serializeToStream(PersistableModel<?, ?> model, OutputStream output) throws ModelPersistenceException;

    /**
     * Deserializes a model from provided input stream.
     * 
     * @param input
     *            The input stream containing the serialized model which was previously serialized using the same
     *            serialization strategy as represented by this instance
     * @return Deserialized model
     * @throws ModelPersistenceException
     *             If something goes wrong
     */
    PersistableModel<?, ?> deserializeFromStream(InputStream input) throws ModelPersistenceException;
}
