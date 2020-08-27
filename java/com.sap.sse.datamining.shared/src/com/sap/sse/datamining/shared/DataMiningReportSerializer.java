package com.sap.sse.datamining.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Base64Utils;
import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.util.JoinedClassLoader;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;

/** Static class to (de)serialize {@link DataMiningReportDTO} from/to base64 strings. */
public final class DataMiningReportSerializer {

    private static final Logger LOG = Logger.getLogger(DataMiningReportSerializer.class.getName());
    private DataMiningReportSerializer() {
    }

    /** @return the {@link DataMiningReportDTO} as a base64 string serialized with java serialization */
    public static String reportToBase64(final DataMiningReportDTO dto) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(stream)) {
            out.writeObject(dto);
            byte[] bytes = stream.toByteArray();
            return Base64Utils.toBase64(bytes);
        } catch (IOException e) {
            LOG.warning("Could not serialize report: " + e.getMessage());
        }
        return "";
    }

    /** @return the {@link DataMiningReportDTO} from a base 64 string deserialized with java serialization */
    public static DataMiningReportDTO reportFromBase64(final String data, final JoinedClassLoader classLoader) {
        byte[] bytes;
        try {
            bytes = Base64Utils.fromBase64(data);
        } catch (IllegalArgumentException e) {
            return null;
        }
        final ClassLoader oldThreadContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try (final ObjectInputStream in = new ObjectInputStreamResolvingAgainstCache<Object>(
                new ByteArrayInputStream(bytes), /* dummy "cache" */ new Object(), /* resolve listener */ null) {}) {
            Object object = in.readObject();
            if (object instanceof DataMiningReportDTO) {
                return (DataMiningReportDTO) object;
            }
        } catch (IOException | ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "Could not deserialize report", e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldThreadContextClassLoader);
        }
        return null;
    }
}
