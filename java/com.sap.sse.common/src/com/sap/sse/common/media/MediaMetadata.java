package com.sap.sse.common.media;

import java.net.URL;
import java.util.Date;

/**
 * A common media interface for all kinds of media like images or videos. 
 */
public interface MediaMetadata {
    public MimeType getMimeType();

    public URL getURL();

    public String getTitle();

    public Iterable<String> getTags();

    public String getSubtitle();

    public Date getCreatedAtDate();

    public String getCopyright();
}
