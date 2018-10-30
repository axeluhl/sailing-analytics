package com.sap.sse.datamining.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import com.sap.sse.common.Base64Utils;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

/** This serializer can (de-)serialize a {@link StatisticQueryDefinitionDTO} into a Base64-String. */
public final class DataMiningQuerySerializer {

    private static final Logger LOG = Logger.getLogger(DataMiningQuerySerializer.class.getName());
    private DataMiningQuerySerializer() {
    }

    /** @return the {@link StatisticQueryDefinitionDTO} as a base 64 string serialized with java serialization */
    public static String toBase64String(final StatisticQueryDefinitionDTO dto) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(stream)) {
            out.writeObject(dto);
            byte[] bytes = stream.toByteArray();
            return Base64Utils.toBase64(bytes);
        } catch (IOException e) {
            LOG.warning("Could not store query: " + e.getMessage());
        }
        return "";
    }

    /** @return the {@link StatisticQueryDefinitionDTO} from a base 64 string deserialized with java serialization */
    public static StatisticQueryDefinitionDTO fromBase64String(final String string) {
        byte[] bytes;
        try {
            bytes = Base64Utils.fromBase64(string);
        } catch (IllegalArgumentException e) {
            return null;
        }

        try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            ObjectInputStream in;
            in = new ObjectInputStream(stream);
            Object o = in.readObject();
            if (o instanceof StatisticQueryDefinitionDTO) {
                return (StatisticQueryDefinitionDTO) o;
            }
        } catch (IOException | ClassNotFoundException e) {
            LOG.warning("Could not load query: " + e.getMessage());
        }
        return null;
    }
}
