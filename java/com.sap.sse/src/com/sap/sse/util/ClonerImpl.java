package com.sap.sse.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.sap.sse.common.Cloner;

public class ClonerImpl implements Cloner {
    public void clone(Object from, Object to) {
        try {
            Class<?> c = from.getClass();
            while (c != null) {
                for (Field field : c.getDeclaredFields()) {
                    if ((field.getModifiers() & Modifier.FINAL) == 0) {
                        field.setAccessible(true);
                        Object value;
                        value = field.get(from);
                        field.set(to, value);
                    }
                }
                c = c.getSuperclass();
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
