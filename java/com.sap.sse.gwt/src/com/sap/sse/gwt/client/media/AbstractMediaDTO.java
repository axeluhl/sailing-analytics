package com.sap.sse.gwt.client.media;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.media.MimeType;

public abstract class AbstractMediaDTO implements IsSerializable, Comparable<AbstractMediaDTO> {
    protected String title;

    protected String subtitle;

    protected Date createdAtDate;

    protected String copyright;

    protected MimeType mimeType;

    protected List<String> tags = new ArrayList<String>();

    protected String sourceRef;
    
    protected String locale;

    /** for GWT */
    @Deprecated
    protected AbstractMediaDTO() {
    }

    public AbstractMediaDTO(String sourceRef, MimeType mimeType, Date createdAtDate) {
        this.sourceRef = sourceRef;
        this.mimeType = mimeType;
        this.createdAtDate = createdAtDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Date getCreatedAtDate() {
        return createdAtDate;
    }

    public void setCreatedAtDate(Date createdAtDate) {
        this.createdAtDate = createdAtDate;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
    }

    public List<String> getTags() {
        return tags;
    }

    public boolean hasTag(String tagName) {
        return tags.contains(tagName);
    }

    public void setTags(Iterable<String> tags) {
        for(String tag: tags) {
            this.tags.add(tag);
        }
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    @Override
    public int compareTo(AbstractMediaDTO o) {
        if(createdAtDate == o.createdAtDate) {
            return 0;
        }
        if(createdAtDate == null) {
            return 1;
        }
        if(o.createdAtDate == null) {
            return -1;
        }
        return -createdAtDate.compareTo(o.createdAtDate);
    }
}
