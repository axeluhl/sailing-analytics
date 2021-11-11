package com.sap.sailing.server.gateway.impl;

import java.io.Serializable;

public class ProgressListener implements org.apache.commons.fileupload.ProgressListener, Serializable {
    private static final long serialVersionUID = 6154127000928850893L;
    private long num100Ks = 0;
    private long theBytesRead = 0;
    private long theContentLength = -1;
    private int percentDone = 0;
    private boolean contentLengthKnown = false;

    @Override
    public void update(final long bytesRead, final long contentLength, final int items) {
        if (contentLength > -1) {
            contentLengthKnown = true;
        }
        theBytesRead = bytesRead;
        theContentLength = contentLength;
        final long nowNum100Ks = bytesRead / 100000;
        // Only run this code once every 100K
        if (nowNum100Ks > num100Ks || theBytesRead == theContentLength) {
            num100Ks = nowNum100Ks;
            if (contentLengthKnown) {
                percentDone = (int) Math.round(100.00 * bytesRead / contentLength);
            }
        }
    }

    public String getMessage() {
        if (theContentLength == -1) {
            return "" + theBytesRead + " of Unknown-Total bytes have been read.";
        } else {
            return "" + theBytesRead + " of " + theContentLength + " bytes have been read (" + percentDone + "% done).";
        }
    }

    public long getTheBytesRead() {
        return theBytesRead;
    }

    public long getTheContentLength() {
        return theContentLength;
    }

    public int getPercentDone() {
        return percentDone;
    }
}
