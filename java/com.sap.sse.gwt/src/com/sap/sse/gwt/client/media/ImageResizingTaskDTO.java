package com.sap.sse.gwt.client.media;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.MimeType;

/**
 * Used to transfer an ImageDTO and a list of MediaTagConstants which store information on how to resize the image
 * 
 * 
 * @author Robin Fleige (D067799)
 *
 */
public class ImageResizingTaskDTO implements IsSerializable {

    private ImageDTO image;
    //contains the list of resizing tasks, can not be null, if this is empty the ImageResizingTaskDTO is only an upload task
    private List<MediaTagConstants> resizingTask;

    /** for GWT */
    @Deprecated
    protected ImageResizingTaskDTO() {
    }

    /**
     * Creates an ImageDTO from the parameters
     * 
     * @param imageRef
     *            needed for creating ImageDTO
     * @param createdAtDate
     *            needed for creating ImageDTO
     * @param resizingTask
     *            stored Information about resizing
     */
    public ImageResizingTaskDTO(String imageRef, Date createdAtDate, List<MediaTagConstants> resizingTask) {
        this(new ImageDTO(imageRef, createdAtDate), resizingTask);
    }

    /**
     * 
     * @param image
     *            the stored ImageDTO
     * @param resizingTask
     *            stored Information about resizing
     */
    public ImageResizingTaskDTO(ImageDTO image, List<MediaTagConstants> resizingTask) {
        this.image = image;
        this.resizingTask = resizingTask;
    }

    /**
     * @returns the ImageDTO
     */
    public ImageDTO getImage() {
        return image;
    }

    /**
     * @returns the List of MediaTagConstants
     */
    public List<MediaTagConstants> getResizingTask() {
        return resizingTask;
    }

    /**
     * Creates a clone of the ImageDTo with new references, so they do not interfere Used for resizing to edit
     * afterwards, to easily copy all data from the original ImageDTO
     * 
     * @returns a clone of the ImageDTO
     */
    public ImageDTO cloneImageDTO() {
        final ImageDTO toReturn = new ImageDTO(image.getSourceRef(), image.getCreatedAtDate());
        toReturn.setTitle(image.getTitle());
        toReturn.setCopyright(image.getCopyright());
        toReturn.setLocale(image.getLocale());
        // creating a new mimetype object so they do not use the same reference
        toReturn.setMimeType(MimeType.byName(image.getMimeType().name()));
        toReturn.setSizeInPx(image.getWidthInPx(), image.getHeightInPx());
        toReturn.setSubtitle(image.getSubtitle());
        // Creating a new list, so they have the same tags, but not use the same reference to these tags, otherwise they
        // could affect each other by editing this list
        final List<String> tags = new ArrayList<>();
        tags.addAll(image.getTags());
        toReturn.setTags(tags);
        return toReturn;
    }
}