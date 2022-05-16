package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.base.MasterDataImportClassLoaderService;

public class MasterDataImportClassLoaderServiceImpl implements MasterDataImportClassLoaderService {
    @Override
    public ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }
}
