package com.sap.sse.shared.classloading.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.shared.classloading.ClassLoaderRegistry;
import com.sap.sse.shared.classloading.JoinedClassLoader;

public class ClassLoaderRegistryImpl implements ClassLoaderRegistry {
    /**
     * A synchronized set of the class loaders to use for importing master data. See also {@link MasterDataImportClassLoaderService},
     * {@link #addClassLoader(ClassLoader)} and {@link #removeClassLoader(ClassLoader)}. In order to loop over
     * these, synchronize on the object. See also {@link Collections#synchronizedSet(Set)}.
     */
    private final Set<ClassLoader> masterDataClassLoaders = Collections.synchronizedSet(new HashSet<>());
    
    @Override
    public void addClassLoader(ClassLoader classLoader) {
        synchronized (masterDataClassLoaders) {
            masterDataClassLoaders.add(classLoader);
        }
    }

    @Override
    public void removeClassLoader(ClassLoader classLoader) {
        synchronized (masterDataClassLoaders) {
            masterDataClassLoaders.remove(classLoader);
        }
    }
    
    @Override
    public ClassLoader getCombinedMasterDataClassLoader() {
        return new JoinedClassLoader(masterDataClassLoaders);
    }
}
