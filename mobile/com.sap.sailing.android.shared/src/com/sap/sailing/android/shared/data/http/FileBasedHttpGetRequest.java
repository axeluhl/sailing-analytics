package com.sap.sailing.android.shared.data.http;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;

public class FileBasedHttpGetRequest extends HttpGetRequest {

    private final File outputFile;

    public FileBasedHttpGetRequest(URL url, HttpRequestProgressListener listener, File outputFile, Context context) {
        super(url, listener, context);
        this.outputFile = outputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    @Override
    protected InputStream readAndCopyResponse(HttpURLConnection connection, BufferedInputStream inputStream)
            throws IOException {
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        try {
            if (!readResponse(connection, inputStream, outputStream)) {
                return null;
            }
        } finally {
            safeClose(outputStream);
        }
        return new FileInputStream(outputFile);
    }
}
