package com.sap.sailing.server.impl;

import java.util.Set;

public class JoinedClassLoader extends ClassLoader {
    
    private final Set<ClassLoader> classLoaders;
    
    public JoinedClassLoader(Set<ClassLoader> classLoaders) {
        this.classLoaders = classLoaders;
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> result = null;
        for (ClassLoader classLoader : classLoaders) {
            try {
                result = classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                //Not found, but may be found in other class loader.
            }
            if (result != null) {
                break;
            }
        }
        if (result == null) {
            throw new ClassNotFoundException(name);
        }
        return result;
    }

}
