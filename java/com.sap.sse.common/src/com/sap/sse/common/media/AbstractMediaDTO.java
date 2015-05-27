package com.sap.sse.common.media;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;

/**
 * Common media data for media items
 * 
 * @author pgtaboada
 *
 */
public class AbstractMediaDTO implements Serializable, Comparable<AbstractMediaDTO> {
    private static final long serialVersionUID = 1L;

    /**
     * TODO: would "caption" be a better name?
     */
    private String title;

    /**
     * TODO: should we provide a subtitle?
     */
    protected String subtitle;

    /**
     * This information should be used for chronological ordering 
     * TODO: Must not be null. We need to find a way to
     * migrate data!
     */
    protected Date createdAtDate;

    /**
     * TODO: initially I though of "author", but I am not sure if we need the author name. But we probably will need
     * some place to store copyright information...
     */
    protected String copyright;

    private MimeType mimeType;

    /**
     * TODO: tags will give us some way to categorize medias. If we pass this information to the frontend, we need some
     * nice idea to show display this data. for now it should be something restricted to the backend domain modell
     */
    private HashSet<String> tags = new HashSet<String>();

    /**
     * URL pointing to the media data.
     */
    private String sourceRef;

    /**
     * Default constructor required for GWT serialization.
     */
    protected AbstractMediaDTO() {
    }

    /**
     * Media item with minimal set of information
     * 
     * @param sourceRef
     * @param mimeType
     */
    public AbstractMediaDTO(String sourceRef, MimeType mimeType, Date createdAtDate) {
        this.mimeType = mimeType;
        this.sourceRef = sourceRef;
        this.createdAtDate = createdAtDate;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public HashSet<String> getTags() {
        return tags;
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

    @Override
    public int compareTo(AbstractMediaDTO o) {
        return createdAtDate.compareTo(o.createdAtDate);
    }
}
