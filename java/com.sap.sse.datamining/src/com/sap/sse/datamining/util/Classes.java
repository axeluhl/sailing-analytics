package com.sap.sse.datamining.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class Classes {
    
    public static Class<?> primitiveToWrapperType(Class<?> type) {
        if (byte.class.equals(type)) {
            return Byte.class;
        }
        if (short.class.equals(type)) {
            return Short.class;
        }
        if (int.class.equals(type)) {
            return Integer.class;
        }
        if (long.class.equals(type)) {
            return Long.class;
        }
        if (float.class.equals(type)) {
            return Float.class;
        }
        if (double.class.equals(type)) {
            return Double.class;
        }
        if (boolean.class.equals(type)) {
            return Boolean.class;
        }
        if (char.class.equals(type)) {
            return Character.class;
        }
        return type;
    }

    public static Collection<Class<?>> getSupertypesOf(Class<?> type) {
        Collection<Class<?>> supertypes = new HashSet<>();
        
        supertypes.addAll(getInterfacesOf(type));
        Class<?> superclass = type.getSuperclass();
        if (superclass != null) {
            supertypes.add(type.getSuperclass());
        }
        
        supertypes.addAll(getSupertypesOf(supertypes));
        return supertypes;
    }

    private static Collection<Class<?>> getInterfacesOf(Class<?> type) {
        return Arrays.asList(type.getInterfaces());
    }

    private static Collection<Class<?>> getSupertypesOf(Collection<Class<?>> types) {
        Collection<Class<?>> supertypes = new HashSet<>();
        boolean supertypeAdded;
        do {
            Collection<Class<?>> supertypesToAdd = getSupertypesToAdd(types);
            supertypeAdded = supertypes.addAll(supertypesToAdd);
        } while (supertypeAdded);
        return supertypes;
    }

    private static Collection<Class<?>> getSupertypesToAdd(Collection<Class<?>> types) {
        Collection<Class<?>> supertypesToAdd = new HashSet<>();
        for (Class<?> supertype : types) {
            supertypesToAdd.addAll(getSupertypesOf(supertype));
        }
        return supertypesToAdd;
    }
    
    private Classes() {}
    
}
