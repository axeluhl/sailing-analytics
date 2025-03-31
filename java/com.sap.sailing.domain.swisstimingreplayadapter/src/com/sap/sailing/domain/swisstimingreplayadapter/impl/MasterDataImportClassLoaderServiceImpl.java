package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import com.sap.sailing.domain.base.MasterDataImportClassLoaderService;

public class MasterDataImportClassLoaderServiceImpl implements MasterDataImportClassLoaderService {
    @Override
    public ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }
}
