package com.sap.sse.filestorage.impl;

import java.io.IOException;
import java.io.InputStream;

import com.sap.sse.filestorage.FileStorageServiceResolver;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;

/**
 * @see ObjectInputStreamResolvingAgainstCache for reason why subclass is necessary
 * @author Fredrik Teschke
 *
 */
public class ObjectInputStreamResolvingAgainstFileStorageServiceResolver extends
        ObjectInputStreamResolvingAgainstCache<FileStorageServiceResolver> {

    protected ObjectInputStreamResolvingAgainstFileStorageServiceResolver(InputStream in,
            FileStorageServiceResolver cache, ResolveListener resolveListener) throws IOException {
        super(in, cache, resolveListener);
    }

}
