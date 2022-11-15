package com.sap.sailing.android.shared.services.sending;

import java.io.InputStream;

import android.content.Context;
import android.content.Intent;

/**
 * This callback is used to attach an operation to the sending of a message, so that the server reply to the performed
 * POST request can be evaluated.
 * 
 * Important: As the callback needs to be passed around in an {@link Intent} and also written to a file, a default
 * public constructor without parameters is required.
 * 
 * @author Fredrik Teschke
 *
 */
public interface ServerReplyCallback {
    /**
     * The inputStream MUST be closed by the implementing class.
     */
    void processResponse(Context context, InputStream inputStream, String callbackPayload);
}
