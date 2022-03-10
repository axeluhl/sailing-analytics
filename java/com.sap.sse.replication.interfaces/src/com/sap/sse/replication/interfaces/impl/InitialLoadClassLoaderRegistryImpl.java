package com.sap.sse.replication.interfaces.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.MasterDataImportClassLoaderService;
import com.sap.sse.replication.InitialLoadClassLoaderRegistry;
import com.sap.sse.util.JoinedClassLoader;

public class InitialLoadClassLoaderRegistryImpl implements InitialLoadClassLoaderRegistry {
    /**
     * A synchronized set of the class loaders to use for importing master data. See also {@link MasterDataImportClassLoaderService},
     * {@link #addMasterDataClassLoader(ClassLoader)} and {@link #removeMasterDataClassLoader(ClassLoader)}. In order to loop over
     * these, synchronize on the object. See also {@link Collections#synchronizedSet(Set)}.
     */
    private final Set<ClassLoader> masterDataClassLoaders = Collections.synchronizedSet(new HashSet<>());
    
    @Override
    public void addMasterDataClassLoader(ClassLoader classLoader) {
        synchronized (masterDataClassLoaders) {
            masterDataClassLoaders.add(classLoader);
        }
    }

    @Override
    public void removeMasterDataClassLoader(ClassLoader classLoader) {
        synchronized (masterDataClassLoaders) {
            masterDataClassLoaders.remove(classLoader);
        }
    }
    
    @Override
    public ClassLoader getCombinedMasterDataClassLoader() {
        return new JoinedClassLoader(masterDataClassLoaders);
    }
}
