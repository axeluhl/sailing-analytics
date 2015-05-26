package com.sap.sse.common.media;

import java.io.Serializable;
import java.util.HashSet;

public class AbstractMediaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;

    private MimeType mimeType;

    private HashSet<String> tags = new HashSet<String>();

    private String sourceRef;

    protected AbstractMediaDTO() {
    }

    
    public AbstractMediaDTO(String sourceRef, MimeType mimeType, String title) {
        this.title = title;
        this.mimeType = mimeType;
        this.sourceRef = sourceRef;
    }

    public String getTitle() {
        return title;
    }

    public MimeType getMimeType() {
        return mimeType;
    }


    public HashSet<String> getTags() {
        return tags;
    }

    public String getSourceRef() {
        return sourceRef;
    }



}
