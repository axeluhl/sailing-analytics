package com.sap.sailing.util;

import java.io.ByteArrayOutputStream;

public class ByteArrayOutputStreamWithVisibleBuffer extends ByteArrayOutputStream {
    public byte[] getBuffer() {
        return buf;
    }
}