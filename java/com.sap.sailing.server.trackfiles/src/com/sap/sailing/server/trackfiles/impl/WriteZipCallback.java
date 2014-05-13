package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;
import java.util.zip.ZipEntry;

interface WriteZipCallback {
    void write(ZipEntry entry, byte[] data) throws IOException;
}