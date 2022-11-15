package com.sap.sailing.domain.orc.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

import com.sap.sailing.domain.orc.ORCCertificatesImporter;

public abstract class AbstractORCCertificatesImporter implements ORCCertificatesImporter {
    protected BufferedReader getReaderForInputStream(InputStream inputStream) throws IOException, UnsupportedEncodingException {
        String defaultEncoding = "UTF-8";
        BOMInputStream bomInputStream = new BOMInputStream(inputStream);
        ByteOrderMark bom = bomInputStream.getBOM();
        String charsetName = bom == null ? defaultEncoding : bom.getCharsetName();
        InputStreamReader reader = new InputStreamReader(new BufferedInputStream(bomInputStream), charsetName);
        BufferedReader br = new BufferedReader(reader);
        return br;
    }
}
