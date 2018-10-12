package com.sap.sse.datamining.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public final class DataMiningQuerySerializer {

    private DataMiningQuerySerializer() {
    }

    public static String toBase64String(StatisticQueryDefinitionDTO dto) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(dto);
            out.close();
            stream.close();
            byte[] bytes = stream.toByteArray();
            return new String(Base64.getEncoder().encode(bytes), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static StatisticQueryDefinitionDTO fromBase64String(String string) {
        try {
            byte[] bytes = Base64.getDecoder().decode(string);
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            ObjectInputStream in;
            in = new ObjectInputStream(stream);
            Object o = in.readObject();
            if (o instanceof StatisticQueryDefinitionDTO) {
                return (StatisticQueryDefinitionDTO) o;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
