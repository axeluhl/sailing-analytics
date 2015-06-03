package com.sap.sse.common.media;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;

/**
 * Common media data for media items
 * 
 * @author pgtaboada
 *
 */
public abstract class AbstractMediaDescriptor implements MediaDescriptor, Serializable {
    private static final long serialVersionUID = -6671425870632517274L;

    protected String title;

    protected String subtitle;

    protected Date createdAtDate;

    protected String copyright;

    protected MimeType mimeType;

    protected HashSet<String> tags = new HashSet<String>();

    protected URL url;

    /**
     * Media item with minimal set of information
     * @param url
     * @param mimeType
     */
    public AbstractMediaDescriptor(URL url, MimeType mimeType, Date createdAtDate) {
        this.mimeType = mimeType;
        this.url = url;
        this.createdAtDate = createdAtDate;
    }

    @Override
    public MimeType getMimeType() {
        return mimeType;
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public HashSet<String> getTags() {
        return tags;
    }

    @Override
    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public Date getCreatedAtDate() {
        return createdAtDate;
    }

    public void setCreatedAtDate(Date createdAtDate) {
        this.createdAtDate = createdAtDate;
    }

    @Override
    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
}
