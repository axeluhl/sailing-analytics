package com.sap.sse.datamining.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ClassUtils {

    /**
     * Maps primitive <code>Class</code>es to their corresponding wrapper <code>Class</code>.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();
    static {
         primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
         primitiveWrapperMap.put(Byte.TYPE, Byte.class);
         primitiveWrapperMap.put(Character.TYPE, Character.class);
         primitiveWrapperMap.put(Short.TYPE, Short.class);
         primitiveWrapperMap.put(Integer.TYPE, Integer.class);
         primitiveWrapperMap.put(Long.TYPE, Long.class);
         primitiveWrapperMap.put(Double.TYPE, Double.class);
         primitiveWrapperMap.put(Float.TYPE, Float.class);
         primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }
    
    private static final Map<String, Class<?>> namePrimitiveMap = new HashMap<>();
    static {
        for (Class<?> primitive : primitiveWrapperMap.keySet()) {
            namePrimitiveMap.put(primitive.getName(), primitive);
        }
    }
    
    public static Class<?> primitiveTypeToWrapperClass(Class<?> type) {
        Class<?> convertedClass = type;
        if (type != null && type.isPrimitive()) {
            convertedClass = primitiveWrapperMap.get(type);
        }
        return convertedClass;
    }
    
    public static Class<?> getClassForName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
        if (namePrimitiveMap.containsKey(name)) {
            return namePrimitiveMap.get(name);
        }
        return Class.forName(name, initialize, loader);
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
    
    private ClassUtils() {}
    
}
