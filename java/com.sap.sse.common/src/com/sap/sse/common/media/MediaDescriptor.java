package com.sap.sse.common.media;

import java.io.Serializable;
import java.net.URL;
import java.util.Locale;

import com.sap.sse.common.TimePoint;

/**
 * A common media interface for all kinds of media like images or videos. 
 */
public interface MediaDescriptor extends Serializable {
    MimeType getMimeType();
    URL getURL();

    String getTitle();
    void setTitle(String title);

    Iterable<String> getTags();
    void setTags(Iterable<String> tags);
    boolean addTag(String tagName); 
    boolean removeTag(String tagName);
    boolean hasTag(String tagName);

    String getSubtitle();
    void setSubtitle(String subtitle);

    TimePoint getCreatedAtDate();
    void setCreatedAtDate(TimePoint createdAtDate);

    String getCopyright();
    void setCopyright(String copyright);
    
    Locale getLocale();
    void setLocale(Locale locale);
}
